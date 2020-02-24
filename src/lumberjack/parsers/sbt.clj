(ns lumberjack.parsers.sbt
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

;; (def text
;;   (string/trim (slurp (io/resource "example-data.txt"))))

(defn parse-sbt-dependency [s]
  (let [level           (/ (or (string/index-of s "+") 0) 2)
        [_ org pkg ver] (re-find #"^[-\|+ ]*([^:]+):([^:]+):([^:]+)" s)]
    {:org org :pkg pkg :ver ver :level level}
    {:org org :level level}
    ))

(defn- build-dependency-tree [deps]
  (loop [deps  deps
         stack nil 
         graph {}]
    (println "---")
    (println (str "dep:   " (first deps)))
    (println (str "stack: " stack))
    (println (str "graph: " graph))
    
    (if (empty? deps)
      graph
      (let [[d & ds] deps
            d-lvl     (get d :level)
            stack-lvl (get (first stack) :level 0)
            new-stack (cond
                        (= d-lvl stack-lvl) (cons d (rest stack))
                        (< d-lvl stack-lvl) (rest stack)
                        (> d-lvl stack-lvl) (cons d stack))]
        (recur ds
               new-stack
               (let [new-graph (merge {d #{}} graph)]
                 (if (> d-lvl stack-lvl)
                   (update new-graph (first stack) (partial cons d))
                   new-graph)))))))

(defn- clean-dependencies [tree]
  (into {} (for [[k vs] tree] [(dissoc k :level)
                               (into #{} (map (fn [x] (dissoc x :level)) vs))])))

(defn parse-dependency-tree [txt]
  (->> (string/split txt #"\n")
       (map (fn [s] (string/replace-first s #"\[info\] " "")))
       (map parse-sbt-dependency)
       (filter (fn [dep] (not (nil? (:org dep)))))
       (build-dependency-tree)
       (clean-dependencies)))



