(ns lumberjack.core
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]
   [lumberjack.config :refer [config]]
   [lumberjack.parsers.sbt :as sbt]
   [clojure.string :as string]
   [clojure.java.io :as io]
   [lumberjack.analysis :as analysis]))

(timbre/refer-timbre)

(timbre/merge-config! {:appenders {:println {:enabled? false}}})   

(timbre/merge-config!
  {:appenders {:spit (appenders/spit-appender {:fname "lumberjack.log"})}})

(defn- load-project-dependencies [path]
  (->> (io/file path)
       (slurp)
       (string/trim)
       (sbt/parse-dependency-tree)))

(defn load-projects [config]
  (into {} (for [p (:projects config)] [p (load-project-dependencies p)])))

(def projects (load-projects config))

(defn- add-dependency-canonical-name [{:keys [org pkg ver] :as dep}]
  (assoc dep :id (str org ":" pkg ":" ver)))

;; Public

(defn project-names
  "Return a list of the project names."
  []
  (keys projects))

(defn dependencies
  "Return a list of the dependencies."
  []
  (map add-dependency-canonical-name (analysis/dependencies projects)))

(defn clashes
  "Return a list of the dependency clashes."
  []
  (map add-dependency-canonical-name (analysis/clashes projects)))



