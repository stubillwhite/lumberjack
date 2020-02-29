(ns lumberjack.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [lumberjack.analysis :as analysis]
            [lumberjack.config :refer [config]]
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

(defn load-project-data! [config]
  (swap! project-data
         (fn [x] (into {} (for [p (:projects config)] [p (load-project-dependencies p)])))))

(load-project-data! config)

(defn- to-canonical-dependency-name [{:keys [org pkg ver] :as dep}]
  {:id (str org ":" pkg ":" ver)})

;; Public

(defn projects
  "Return a list of the project names."
  []
  (analysis/projects @project-data))

(defn dependencies
  "Return a list of the dependencies."
  []
  (map to-canonical-dependency-name (analysis/dependencies @project-data)))

(defn clashes
  "Return a list of the dependency clashes."
  []
  (map to-canonical-dependency-name (analysis/clashes @project-data)))

(defn dependencies-for-project
  "Return a list of the dependencies."
  [name]
  (map to-canonical-dependency-name (analysis/dependencies-for-project @project-data name)))
