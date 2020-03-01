(ns lumberjack.analysis
  (:require [lumberjack.utils :refer [from-canonical-name to-canonical-name]]
            [taoensso.timbre :as timbre]))

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

(defn project-names
  "Return a list of the names of all projects."
  [projects]
  (keys projects))

(defn dependencies-for-project
  "Returns a set of the dependencies in the project."
  [projects name]
  (into #{} (flatten-dependencies (get projects name))))

(defn all-dependencies
  "Return a set of all dependencies."
  [projects]
  (->> (project-names projects)
       (mapcat (partial dependencies-for-project projects))
       (into #{})))

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
  (->> (for [prj (project-names projects)
             :when (contains? (dependencies-for-project projects prj) dep)] prj)
       (into #{})))

(defn dependency-graph-for-project
  "Return a graph of the project dependencies."
  [projects name]
  (get projects name))
