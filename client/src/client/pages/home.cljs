(ns client.pages.home
  (:require [antizer.reagent :as ant]
            [client.api :as api]
            [client.components :refer [page-description]]
            [client.state :refer [state update-state!]]
            [client.utils :as utils]))

(defn on-entry [])
(defn on-exit [])

;; TODO: Move to utils, handle nested maps
(defn decode-response [body]
  (->> (.parse js/JSON body)
       (utils/to-clj)))

(defn- load-projects! []
  (api/get-projects
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:projects] (decode-response body)))
         (ant/message-error "Unable to load projects."))))))

(defn- load-dependencies! []
  (api/get-dependencies
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:dependencies] (decode-response body)))
         (ant/message-error "Unable to load dependencies."))))))

(defn- load-clashes! []
  (api/get-clashes
   (fn [response]
     (let [{:keys [status body]} response]
       (case status
         200 (update-state! #(assoc-in % [:clashes] (into #{} (map :id (decode-response body)))))
         (ant/message-error "Unable to load clashes."))))))

(defn- reload-data []
  (load-projects!)
  (load-clashes!)
  (load-dependencies!))

(defn- refresh-button []
  [ant/form-item {}
   [ant/button {:type "primary" :on-click reload-data} "Reload"]])

(defn project-table [data]
  [ant/table 
   {:columns    [{:title "Name" :dataIndex :name :key :name}]
    :dataSource data
    :pagination false
    :row-key    :name
    :size       :small
    }])

(defn subsection [title]
  [:div
   [:hr]
   [:h2 title]])

(defn dependency-canonical-name [{:keys [org pkg ver]}]
  (str org ":" pkg ":" ver))

(defn clashing-tag [key row idx]
  (let [id       (:id (js->clj row :keywordize-keys true))
        clashing (:clashes @state)]
    (if (contains? clashing id)
      (reagent.core/as-element [ant/tag {:style {:background "DarkRed" :color "White"}} "clashing"])
      (reagent.core/as-element [ant/tag {:style {:background "DarkGreen" :color "White"}} "non-clashing"]))))
  
(defn dependencies-table [data]
  [ant/table 
   {:columns    [{:title "Dependency" :dataIndex :id :key :id}
                 {:title "Status"     :dataIndex :id :key (fn [row] (str (:id row) "-status")) :render clashing-tag}] 
    :dataSource    data
    :pagination    false
    :row-key       :id
    :row-selection {:on-change (fn [idx row]
                                 (let [selected (js->clj row :keywordize-keys true)]
                                   (ant/message-info (str "You have selected: " (map :id selected)))))}
    :onRow        (fn [row idx] {:on-click (fn [event] (ant/message-info (str "Clicked on " idx)))})
    :size          "small"
    :scroll        {:y 500}}])

(defn- set-state-flag [ks value]
  (update-state! #(assoc-in % ks value)))

(defn- page-controls []
  [:div
   (refresh-button)
   [:div
    [ant/row
     [ant/col {:span 1} [ant/switch {:default-checked true :on-click (partial set-state-flag [:display-clashing])}]]
     [ant/col {:span 4} [:p "Display clashing"]]]
    [ant/row
     [ant/col {:span 1} [ant/switch {:default-checked true :on-click (partial set-state-flag [:display-non-clashing])}]]
     [ant/col {:span 4} [:p "Display non-clashing"]]]
    ]])

(defn- selected-dependencies []
  (let [disp-clashing     (:display-clashing @state)
        disp-non-clashing (:display-non-clashing @state)
        clashing?         (fn [x] (contains? (:clashes @state) (:id x)))]
    (cond
      (and disp-clashing disp-non-clashing) (:dependencies @state)
      disp-clashing                         (filter clashing? (:dependencies @state))
      disp-non-clashing                     (filter (complement clashing?) (:dependencies @state))
      :else                                 [])))

(defn view []
  [:div
   (page-description "Lumberjack" "")
   (page-controls)
   [:div
    [ant/col {:span 12}
     (subsection "Projects")
     (project-table (for [name (get-in @state [:projects])] {:name name}))]]
   [:div
    [ant/col {:span 12}
     (subsection "Dependencies")
     (dependencies-table (sort-by :id (selected-dependencies)))]]
   ])
