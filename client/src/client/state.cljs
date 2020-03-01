(ns client.state
  (:require
   [reagent.core :as reagent]))

;; Application state
(defonce state (reagent/atom {:project-names        []
                              :all-dependencies     []
                              :clashes              []
                              :router               {:page "home"}}))

(defn update-state! [f & args]
  (apply swap! state f args)
  (js/console.log @state))

