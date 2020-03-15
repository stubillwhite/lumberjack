(ns client.pages.home
  (:require [antizer.reagent :as ant]
            [client.api :as api]
            [reagent.core :as reagent]
            [client.components :refer [page-description]]
            [client.state :refer [state update-state!]]
            [client.utils :as utils]))

(defn on-entry [])
(defn on-exit [])

(def page-state (reagent/atom {:display-non-clashing true
                               :display-clashing     true
                               :display-project-only false
                               :excluded-projects    #{}}))

(defn update-page-state! [f & args]
  (apply swap! page-state f args)
  (js/console.log @page-state))

;; TODO: Move to utils, handle nested maps
(defn decode-response [body]
  (->> (.parse js/JSON body)
       (utils/to-clj)))

;; Loading project data

(declare load-extracted-data!)

(defn- load-project-data! []
  (api/load-project-data
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (load-extracted-data!)
         (ant/message-error "Unable to reload data."))))))

(defn- load-project-names! []
  (api/get-project-names
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:project-names] (decode-response body)))
         (ant/message-error "Unable to load projects."))))))

(defn- load-all-dependencies! []
  (api/get-all-dependencies
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:all-dependencies] (decode-response body)))
         (ant/message-error "Unable to load dependencies."))))))

(defn- load-clashes! []
  (api/get-clashes
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:clashes] (into #{} (decode-response body))))
         (ant/message-error "Unable to load clashes."))))))

(defn- get-dependencies-for-project! [name]
  (api/get-dependencies-for-project name
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:selected-dependencies] (into #{} (decode-response body))))
         (ant/message-error "Unable to load dependencies for project."))))))

(defn- load-extracted-data! []
  (load-project-names!)
  (load-clashes!)
  (load-all-dependencies!))

(defn convert-nodes [m]
  (into [] (apply concat (for [[k v] m] [{:id (str k) :children (convert-nodes v)}]))))

(defn- get-graph-for-project! [name]
  (api/get-graph-for-project name
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:selected-graph] (into [] (convert-nodes (decode-response body)))))
         (ant/message-error "Unable to load graph for project."))))))

;; View

(defn- refresh-button []
  [ant/form-item {}
   [ant/button {:type "primary" :on-click load-project-data!} "Load project data"]])

(defn- select-project! [name]
  (update-state! assoc :selected-project name)
  (get-dependencies-for-project! name)
  (get-graph-for-project! name))

(defn project-excluded-status [key row idx]
  (let [id (:id (js->clj row :keywordize-keys true))]
    (reagent.core/as-element [ant/checkbox])))

(defn project-table [data]
  [ant/table 
   {:columns    [{:title ""     :dataIndex :name :key :name :render project-excluded-status}
                 {:title "Name" :dataIndex :name :key :name}]
    :dataSource data
    :pagination false
    :row-key    :name
    :on-row     (fn [row] (js-obj "onClick" #(let [r (js->clj row :keywordize-keys true)]
                                              (select-project! (:name r)))))
    :rowClassName (fn [row idx] (let [r (js->clj row :keywordize-keys true)]
                                 (when (= (:name r) (:selected-project @state)) "selected-row")))
    :size       :small
    }])

(defn subsection [title]
  [:div
   [:hr]
   [:h2 title]])

(defn clash-status [key row idx]
  (let [id       (:id (js->clj row :keywordize-keys true))
        clashing (:clashes @state)]
    (if (contains? clashing id)
      (reagent.core/as-element [ant/tag {:style {:background "DarkRed" :color "White"}} "clashing"])
      (reagent.core/as-element [ant/tag {:style {:background "DarkGreen" :color "White"}} "non-clashing"]))))
  
(defn dependencies-table [data]
  [ant/table 
   {:columns       [{:title "Dependency" :dataIndex :id :key :id}
                    {:title "Status"     :dataIndex :id :key (fn [row] (str (:id row) "-status")) :render clash-status}] 
    :dataSource    (map (fn [x] {:id x :key x}) data)
    :pagination    false
    :row-key       :id
    :rowClassName  (fn [row idx] (let [r (js->clj row :keywordize-keys true)]
                                  (when (contains? (:selected-dependencies @state) (:id r)) "selected-row")))
    :size          "small"
    :scroll        {:y 500}}])

(defn- set-state-flag [ks value]
  (update-page-state! #(assoc-in % ks value)))

(defn- switch-control [label flag]
  [ant/row
   [ant/col {:span 1} [ant/switch {:default-checked (flag @page-state) :on-click (partial set-state-flag [flag])}]]
   [ant/col {:span 4} [:p label]]])

(defn- page-controls []
  [:div
   (refresh-button)
   [:div
    (switch-control "Display clashing"      :display-clashing)
    (switch-control "Display non-clashing"  :display-non-clashing)
    (switch-control "Selected project-only" :display-project-only)]])

(defn- conditional-filter [active? f coll]
  (if active? (filter f coll) coll))

(defn- selected-dependencies []
  (let [clashing?            (fn [x] (contains? (:clashes @state) x))
        in-selected-project? (fn [x] (contains? (:selected-dependencies @state) x))]
    (->> (:all-dependencies @state)
         (conditional-filter (not (:display-clashing @page-state))     (complement clashing?))
         (conditional-filter (not (:display-non-clashing @page-state)) clashing?)
         (conditional-filter (:display-project-only @page-state)       in-selected-project?))))

(defn view []
  [:div
   (page-description "Project dependencies" "Dependencies for all projects.")
   (page-controls)
   [:div
    [ant/col {:span 12}
     (subsection "Projects")
     (project-table (for [name (get-in @state [:project-names])] {:name name}))]]
   [:div
    [ant/col {:span 12}
     (subsection "Dependencies")
     (dependencies-table (sort-by :id (selected-dependencies)))]]])
