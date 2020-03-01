(ns lumberjack.parsers.sbt
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
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
  (into {} (for [[k vs] tree] [(select-keys k [:org :pkg :ver])
                               (into #{} (map (fn [x] (select-keys x [:org :pkg :ver])) vs))])))

;; Public

(defn parse-dependency-tree
  "Parse the dependency tree into a graph."
  [txt]
  (->> (string/split txt #"\n")
       (map (fn [s] (string/replace-first s #"\[info\] " "")))
       (map parse-sbt-dependency)
       (remove-non-dependencies)
       (remove-evictions)
       (build-dependency-tree)
       (clean-dependencies)))

(defn canonical [{:keys [org pkg ver]}]
  (str org ":" pkg ":" ver))

(defn- build-graph [deps]
  (loop [deps  deps
         path  []
         graph {}]
    (if (empty? deps)
      graph
      (let [[d & ds]  deps
            d-lvl     (get d :level)
            new-path  (into [] (take (inc d-lvl) (assoc path d-lvl (canonical d))))]
        (recur ds new-path
               (assoc-in graph new-path {}))))))

(defn- mktree [coll]
  (->> coll
       (map (partial str "[info] "))
       (string/join "\n")))

(defn get-graph
  "Parse the dependency tree into a graph."
  [txt]
  (->> (string/split txt #"\n")
       (map (fn [s] (string/replace-first s #"\[info\] " "")))
       (map parse-sbt-dependency)
       (remove-non-dependencies)
       (remove-evictions)
       (build-graph)))

;; TODO: Rationalise this
