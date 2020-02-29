(ns lumberjack.analysis
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [lumberjack.config :refer [config]]
   [lumberjack.parsers.sbt :as sbt]
   [clojure.string :as string]
   [clojure.java.io :as io]))

(timbre/refer-timbre)

(defn- map-vals [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn- flattened-dependencies [projects]
  (for [[prj deps] projects
        dep        (keys deps)]
    (assoc dep :prj prj)))

(defn- artifact-to-version-map [projects]
  (->> (flattened-dependencies projects)
       (group-by (fn [{:keys [org pkg]}] {:org org :pkg pkg}))
       (map-vals (fn [x] (into #{} (map #(select-keys % [:ver]) x))))))

(defn artifact-to-project-map [projects]
  (->> (flattened-dependencies projects)
       (group-by (fn [x] (select-keys x [:org :pkg :ver])))
       (map-vals (fn [x] (into #{} (map :prj x))))))

;; Public

(defn projects
  "Return a list of all projects."
  [projects]
  (keys projects))

(defn dependencies
  "Return a list of all dependencies."
  [projects]
  (->> projects
       (vals)
       (map keys)
       (flatten)
       (filter identity)
       (into #{})))

(defn clashes
  "Return a list of all clashing dependencies."
  [projects]
  (->>
   (for [[k vs] (artifact-to-version-map projects) :when (> (count vs) 1)]
     (for [v vs] (merge k v)))
   (flatten)))

(defn dependencies-for-project
  "Returns a set of the dependencies in the project."
  [projects name]
  (->> (get projects name)
       (keys)
       (flatten)
       (into #{})))

(defn projects-referencing
  "Return a set of the projects which reference the dependency."
  [projects dep]
  (get (artifact-to-project-map projects) dep))
