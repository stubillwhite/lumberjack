(ns lumberjack.app
  (:gen-class)
  (:require [mount.core :as mount :refer [defstate]]
            [lumberjack.rest :refer [server]]
            [lumberjack.config :refer [config]]))

(defn -main [& args]
  (mount/start))
