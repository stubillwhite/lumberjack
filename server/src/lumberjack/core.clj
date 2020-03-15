(ns lumberjack.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [lumberjack.analysis :as analysis]
            [lumberjack.config :as cfg :refer [config]]
            [lumberjack.parsers.sbt :as sbt]
            [lumberjack.utils :refer [def-]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(timbre/refer-timbre)

(timbre/merge-config! {:appenders {:println {:enabled? false}}})   

(timbre/merge-config!
  {:appenders {:spit (appenders/spit-appender {:fname "lumberjack.log"})}})

(defn- load-project-dependencies [path]
  (info (str "Loading '" path "'"))
  (->> (io/file path)
       (slurp)
       (string/trim)
       (sbt/parse-dependency-tree)))

(def- project-data (atom nil))

;; Public

(defn load-project-data!
  "Load the project data from the files listed in the configuration file."
  []
  (let [project-config       (cfg/load-config (:project-config config))
        project-dependencies (into {} (for [p (:projects project-config)] [p (load-project-dependencies p)]))
        project-names        (into [] (:projects project-config))]
    
    (swap! project-data
           (fn [_] {:project-dependencies project-dependencies
                   :project-names        project-names}))
    nil))

(defn project-names
  "Return a list of the project names."
  []
  (:project-names @project-data))

(defn all-dependencies
  "Return a list of the dependencies."
  []
  (->> (analysis/all-dependencies (:project-dependencies @project-data))
       (into [])
       (sort)))

(defn clashes
  "Return a list of the dependency clashes."
  []
  (->> (analysis/clashes (:project-dependencies @project-data))
       (into [])
       (sort)))

(defn dependencies-for-projects
  "Return a list of the dependencies for the specified projects."
  [names]
  (->> (analysis/dependencies-for-projects (:project-dependencies @project-data) names)
       (into [])
       (sort)))

(defn dependency-graph-for-project
  "Return a graph of the dependencies."
  [name]
  (analysis/dependency-graph-for-project (:project-dependencies @project-data) name))
