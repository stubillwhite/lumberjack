(ns client.pages.visualise
  (:require [antizer.reagent :as ant]
            [client.components :refer [page-description]]
            [clojure.string :as string]
            [goog.object :refer [getValueByKeys]]
            [reagent.core :as reagent]))

(defn on-entry [])
(defn on-exit [])

(def page-state (reagent/atom {:search-value  ""
                               :expanded-keys []}))

(defn update-page-state! [f & args]
  (apply swap! page-state f args)
  (js/console.log @page-state))

(def data [{:id "Functional languages"
            :children [{:id "CLR"
                        :children [{:id "Clojure CLR" :children []}]}]}
           {:id "Haskell"
            :children []}
           {:id "JVM"
            :children [{:id "Clojure"
                        :children [{:id "Clojurescript" :children []}]}
                       {:id "Scala"
                        :children []}
                       {:id "Frege"
                        :children []}]}])

(defn- path-to-node [pred nodes path]
  (mapcat
   (fn [n] (if (pred n)
            (conj path (:id n))
            (path-to-node pred (:children n) (conj path (:id n)))))
   nodes))

(defn- calculate-expanded-keys [value data]
  (if (= value "")
    []
    (path-to-node #(string/includes? (:id %) value) data [])))

(defn search-field []
  [ant/input {:on-change (fn [e]
                           (let [value (-> e .-target .-value)]
                             (update-page-state! assoc :search-value value)
                             (update-page-state! assoc :expanded-keys (calculate-expanded-keys value data))
                             (update-page-state! assoc :override-expanded-keys true)))}])
(defn- create-tree [nodes]
  (map (fn [n] [ant/tree-tree-node {:title (:id n) :key (:id n)} (create-tree (:children n))]) nodes))



(defn tree []
  [:div
   [ant/tree {:expanded-keys (:expanded-keys @page-state)
              :on-expand     (fn [expanded-keys]
                               (update-page-state! assoc :expanded-keys expanded-keys)
                               (update-page-state! assoc :override-expanded-keys false))}
    (create-tree data)]])

(defn view []
  [:div
   (page-description "Visualise" "Sort this out sometime")
   (search-field)
   (tree)])
