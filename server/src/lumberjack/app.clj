(ns lumberjack.app
  (:gen-class)
  (:require [mount.core :as mount :refer [defstate]]
            [lumberjack.config :refer [config]]))

(defn -main [& args]
  (mount/start))
