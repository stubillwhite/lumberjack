(ns lumberjack.parsers.sbt
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

;; Internal

(defn parse-sbt-dependency [s]
  (let [level           (/ (or (string/index-of s "+") 0) 2)
        [_ org pkg ver] (re-find #"^[-\|+ ]*([^:]+):([^:]+):([^:]+)" s)]
    {:org org :pkg pkg :ver ver :level level}))

(defn- build-dependency-tree [deps]
  (loop [deps  deps
         path  []
         graph {}]
    (if (empty? deps)
      graph
      (let [[d & ds]  deps
            d-lvl     (get d :level)
            parent    (get path (dec d-lvl))
            new-path  (assoc path d-lvl d)
            with-node (merge {d #{}} graph)
            new-graph (if parent 
                        (update with-node parent (partial cons d))
                        with-node)]
        (recur ds new-path new-graph)))))

(defn- clean-dependencies [tree]
  (into {} (for [[k vs] tree] [(dissoc k :level)
                               (into #{} (map (fn [x] (dissoc x :level)) vs))])))

;; Public

(defn parse-dependency-tree
  "Parse the dependency tree into a graph."
  [txt]
  (->> (string/split txt #"\n")
       (map (fn [s] (string/replace-first s #"\[info\] " "")))
       (map parse-sbt-dependency)
       (filter (fn [dep] (not (nil? (:org dep)))))
       (build-dependency-tree)
       (clean-dependencies)))


