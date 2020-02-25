(ns client.navigation
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [client.pages.home :as home]
            [client.state :refer [state update-state!]]
            [client.utils :as utils :refer [index-by]]
            [goog.events :as events]
            [reagent.core :as reagent]
            [secretary.core :as secretary]
            [client.routes :as routes])
  (:import goog.History
           goog.history.EventType))

(declare set-page!)

;; Routing handlers for when the application jumps directly to a URL.
(defn configure-routes! []
  (secretary/set-config! :prefix "#")

  (secretary/defroute root-path "/" []
    (set-page! (routes/pages) "/home" {}))

  (secretary/defroute home-path "/home" []
    (set-page! (routes/pages) "/home" {}))

  (secretary/defroute summary-path "/visualise" []
    (set-page! (routes/pages) "/visualise" {})))

;; Infrastructure
;; --------------------------------------------------

(defn- set-page! [pages page params]
  (let [pages-by-id (index-by :id pages)]
    (js/console.log "secretary: Entering page" page "with params" params)
    (update-state! #(-> %
                      (assoc-in [:router :page] page)
                      (assoc-in [:router :params] params)))
    (when-let [on-entry-fn (get-in pages-by-id [page :on-entry-fn])] (on-entry-fn))))

(defn- hook-browser-navigation!
  "Add hooks to browser navigation. This must be called after routes are
  defined, and must not be called on a reload to avoid breaking
  Figwheel's hot-reload functionality."
  []
  (doto (History.)
    (events/listen EventType.NAVIGATE #(secretary/dispatch! (.-token %)))
    (.setEnabled true)))

(defn- configure-accountant! []
  (accountant/configure-navigation! {:nav-handler       (fn [path] #(secretary/dispatch! path))
                                     :path-exists?      (fn [path] #(secretary/locate-route path))
                                     :reload-same-path? true}))

(defn configure-navigation!
  "Configure browser navigation appropriately depending on whether this
  is a new load or a hot-reload of the page."
  [is-reload]
  (configure-routes!)
  (when (not is-reload) (hook-browser-navigation!))
  (configure-accountant!))

(defn navigate-to
  "Navigate to the target page and provide it with the specified params."
  [target-id params]
  (let [source-id   (get-in @state [:router :page])
        pages-by-id (index-by :id (routes/pages))
        source      (get pages-by-id source-id)
        target      (get pages-by-id target-id)]
    (when-let [exit-fn (:on-exit-fn source)] (exit-fn))
    (when-let [entry-fn (:on-entry-fn target)] (entry-fn))
    (update-state! #(-> %
                        (assoc-in [:router :page] target-id)
                        (assoc-in [:router :params] params)))
    (accountant/navigate! (str "/#" target-id))))

;; View
;; --------------------------------------------------

(defn- navigate-to-clicked-page [event]
  (let [page (:key (js->clj event :keywordize-keys true))] 
    (navigate-to page {})))

(defn- not-found-view [page-id params]
  [:div
   [:h3 "Page not found"]
   [:p (str "Unable to locate page '" page-id "'")]])

(defn- current-page-view [pages]
  (let [{:keys [page params]} (get-in @state [:router])
        not-found   (partial not-found-view page)
        pages-by-id (index-by :id pages)
        view-fn     (get-in pages-by-id [page :view-fn] not-found)]
    (view-fn params)))

(defn- menu-item [id icon label]
  [ant/menu-item {:key id} (reagent/as-element [:span [ant/icon {:type icon}] label])])

(defn view [pages] 
  [ant/layout
   [ant/affix
    [ant/menu {:mode "horizontal" :theme "dark" :selected-keys [(get-in @state [:router :page])]
               :on-click navigate-to-clicked-page}
     (for [{:keys [id icon label]} pages] (menu-item id icon label))]]
   [ant/layout-content {:style {:padding "10px"}}
    [:div
     (current-page-view pages)]]])
