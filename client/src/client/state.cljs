(ns client.state
  (:require
   [reagent.core :as reagent]))

;; Application state
(defonce state (reagent/atom {:projects             []
                              :dependencies         []
                              :clashes              []
                              :display-clashing     true
                              :display-non-clashing true
                              :router               {:page "home"}}))

(defn update-state! [f & args]
  (apply swap! state f args)
  (js/console.log @state))

