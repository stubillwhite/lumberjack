(ns client.pages.visualise
  (:require [antizer.reagent :as ant]
            [client.components :refer [page-description]]
            [clojure.string :as string]
            [client.state :refer [state update-state!]]
            [goog.object :refer [getValueByKeys]]
            [reagent.core :as reagent]))

(defn on-entry [])
(defn on-exit [])

(def page-state (reagent/atom {:search-value  ""
                               :expanded-keys []}))

(defn update-page-state! [f & args]
  (apply swap! page-state f args)
  ;; (js/console.log @page-state)
  )

(def data2 [{:id "Functional languages"
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

(def data [{:id "a"
            :children [{:id "b"
                        :children []}]}
           {:id "c"
            :children [{:id "d"
                        :children []}
                       ]}])

(defn- new-data []
  (let [graph-data (:selected-graph @state)]
    graph-data))

;;


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
  [ant/input {:placeholder "Search"
              :on-change   (fn [e]
                             (let [value (-> e .-target .-value)]
                               (update-page-state! assoc :search-value value)
                               (update-page-state! assoc :expanded-keys (calculate-expanded-keys value (new-data)))
                               (update-page-state! assoc :override-expanded-keys true)))}])

(defn highlight-match [s match]
  (if (and (not (= "" match)) (string/includes? s match))
    (let [non-matches (string/split s (re-pattern match))]
      (reagent.core/as-element
       (into [] (concat [:span] (drop-last (interleave non-matches (repeat [:span {:style {:color "Red"}} match])))))))
    s))

(defn- create-tree [nodes match]
  (map (fn [n] [ant/tree-tree-node {:title (highlight-match (:id n) match) :key (:id n)}
               (create-tree (:children n) match)]) nodes))

(defn tree []
  [:div
   [ant/tree {:expanded-keys (:expanded-keys @page-state)
              :on-expand     (fn [expanded-keys]
                               (update-page-state! assoc :expanded-keys expanded-keys)
                               (update-page-state! assoc :override-expanded-keys false))}
    (create-tree (new-data) (:search-value @page-state))]])

(defn view []
  [:div
   (page-description "Dependency tree" (str "Dependency tree for '" (:selected-project @state) "'"))
   (search-field)
   (tree)])
