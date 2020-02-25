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
         200 (update-state! #(assoc-in % [:clashes] (decode-response body)))
         (ant/message-error "Unable to load clashes."))))))

(defn- reload-data []
  (load-projects!)
  (load-clashes!)
  (load-dependencies!))

(defn- refresh-button []
  [ant/form-item {}
   [ant/button {:type "primary" :on-click reload-data}
     "Reload"]])

(defn project-table [data]
  [ant/table 
   {:columns    [{:title "Name" :dataIndex :name :key :name}]
    :dataSource data
    :pagination false
    :row-key    :name
    }])

(defn subsection [title]
  [:div
   [:hr]
   [:h2 title]])

(defn dependency-canonical-name [{:keys [org pkg ver]}]
  (str org ":" pkg ":" ver))


(defn clashing-tag [key row idx]
  (let [id       (.-id row)
        clashing (into #{} (map :id (:clashes @state)))]
    (if (contains? clashing id)
      (reagent.core/as-element [ant/tag {:style {:background "DarkRed" :color "White"}} "clash"]))))
  
(defn dependencies-table [data]
  [ant/table 
   {:columns    [{:title "Organisation" :dataIndex :org :key :org}
                 {:title "Package"      :dataIndex :pkg :key :pkg}
                 {:title "Version"      :dataIndex :ver :key :ver}
                 {:title "Status"       :dataIndex :org :key :id :render clashing-tag}] 
    :dataSource data
    :pagination false
    :row-key    :id
    }])

(defn view []
  [:div
   (page-description "Lumberjack" "")
   (refresh-button)
   (subsection "Projects")
   (project-table (for [name (get-in @state [:projects :projects])] {:name name}))
   (subsection "Clashes")
   (dependencies-table (sort-by :id (:clashes @state)))
   (subsection "Dependencies")
   (dependencies-table (sort-by :id (:dependencies @state)))
   ])
