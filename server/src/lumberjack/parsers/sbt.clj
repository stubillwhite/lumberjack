(ns lumberjack.parsers.sbt
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [lumberjack.utils :refer [to-canonical-name def-]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; Internal

(defn parse-sbt-dependency [s]
  (let [level                   (/ (or (string/index-of s "+") 0) 2)
        [_ org pkg ver evicted] (re-find #"^[-\|+ ]*([^:]+):([^:]+):([^ ]+)(?: \(evicted by: (.+)\))?" s)]
    {:org org :pkg pkg :ver ver :level level :evicted evicted}))

(defn- remove-non-dependencies [deps]
  (filter (fn [d] (not (nil? (:org d)))) deps))

(defn- remove-evictions [deps]
  (loop [non-evicted []
         deps        deps]
    (if (empty? deps)
      non-evicted
      (let [[d & ds] deps]
        (if (:evicted d)
          (recur non-evicted (drop-while (fn [{:keys [level]}] (> level (:level d))) ds))
          (recur (conj non-evicted d) ds))))))

(defn- build-graph [deps]
  (loop [deps  deps
         path  []
         graph {}]
    (if (empty? deps)
      graph
      (let [[d & ds]  deps
            d-lvl     (get d :level)
            new-path  (into [] (take (inc d-lvl) (assoc path d-lvl (to-canonical-name d))))]
        (recur ds new-path
               (assoc-in graph new-path {}))))))

;; Public

(defn parse-dependency-tree
  "Parse the dependency tree."
  [txt]
  (->> (string/split txt #"\n")
       (map (fn [s] (string/replace-first s #"\[info\] " "")))
       (map parse-sbt-dependency)
       (remove-non-dependencies)
       (remove-evictions)
       (build-graph)))
