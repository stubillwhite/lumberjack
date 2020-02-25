(ns client.pages.coffee
  (:require
   [antizer.reagent :as ant]
   [client.state :refer [state update-state!]]
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]))

(defn increment-count []
  (update-state! #(update-in % [:coffee :count] inc)))

(defn decrement-count []
  (update-state! #(update-in % [:coffee :count] dec)))

(defn on-entry []
  (js/console.log "on-entry hook fired for coffee page"))

(defn on-exit []
  (js/console.log "on-exit hook fired for coffee page"))

;; View
;; --------------------------------------------------

(defn view [params]
  [:div
   [:h3 "Drink some coffee!"]
   [:p {:style {:font-style "italic"}} "This page stores state in the global state atom, so it retains state when navigated away from."]
   [:p "How many cups?"]
   [ant/form {:layout "inline"}
    [ant/form-item {}
     [ant/button {:id "dec" :type "primary" :on-click decrement-count} "Less"]]
    [ant/form-item {}
     [:h1 {:id "count"} (get-in @state [:coffee :count])]]
    [ant/form-item {}
     [ant/button {:id "inc" :type "primary" :on-click increment-count} "More"]]]])
