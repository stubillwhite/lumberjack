(ns lumberjack.analysis
  (:require [lumberjack.utils :refer [from-canonical-name to-canonical-name]]
            [taoensso.timbre :as timbre]
            [clojure.string :as string]))

(timbre/refer-timbre)

(defn- flatten-dependencies [m]
  (into [] (flatten (for [[k v] m] (flatten (cons k (flatten-dependencies v)))))))

(defn- map-vals [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn- artifact-to-version-map [projects]
  (->> (flatten-dependencies projects)
       (map from-canonical-name)
       (group-by (fn [{:keys [org pkg]}] {:org org :pkg pkg}))
       (map-vals (fn [x] (into #{} (map #(select-keys % [:ver]) x))))))

;; Public

(defn dependencies-for-projects
  "Returns a set of the dependencies in the specified projects."
  [projects names]
  (->> (select-keys projects names)
       (flatten-dependencies)
       (into #{})))

(defn all-dependencies
  "Return a set of all dependencies."
  [projects]
  (into #{} (dependencies-for-projects projects (keys projects))))

(defn clashes
  "Return a set of all clashing dependencies."
  [projects]
  (->> (for [[k vs] (artifact-to-version-map projects) :when (> (count vs) 1)]
         (for [v vs] (merge k v)))
   (flatten)
   (map to-canonical-name)
   (into #{})))

(defn projects-referencing
  "Return a set of the projects which reference the dependency."
  [projects dep]
  (->> (for [prj (keys projects)
             :when (contains? (dependencies-for-projects projects [prj]) dep)] prj)
       (into #{})))

(defn dependency-graph-for-project
  "Return a graph of the project dependencies."
  [projects name]
  (get projects name))

(defn- paths-to-matching-nodes
  "Returns the paths from the root of graph g to the nodes matching the predicate."
  [pred g]
  (defn iter [paths ks g]
    (reduce-kv
     (fn [paths k v]
       (if (pred k)
         (conj paths (conj ks k))
         (iter paths (conj ks k) v)))
     paths
     g))
  (iter [] [] g))

(defn paths-to-dependency
  "Returns the paths from the root of graph g to the nodes matching the predicate."
  [name g]
  (paths-to-matching-nodes #(string/includes? % name) g))

