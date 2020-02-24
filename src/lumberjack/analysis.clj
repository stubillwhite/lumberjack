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
       (map-vals (fn [x] (into #{} (map :prj x))))
       ))

;; Public

(defn dependency-clashes
  "Return a map of {:org org :pkg pkg} to {:ver version} of all clashing dependencies in the projects."
  [projects]
  (into {} (filter
            (fn [[k vs]] (> (count vs) 1))
            (artifact-to-version-map projects))))

(defn projects-referencing
  "Return a set of the projects which reference the dependency."
  [projects dep]
  (get (artifact-to-project-map projects) dep))
