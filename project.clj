(defproject lumberjack "0.1.0-SNAPSHOT"

  :description "TODO"

  :url "TODO"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repl-options {:port 4555}

  :plugins [[lein-ring "0.8.11"]]

  :main lumberjack.app

  :ring { :handler lumberjack.rest/app
          :nrepl   { :start? true
                     :port   4555 } }

  :dependencies [;; Core
                 [nrepl "0.3.1"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.trace "0.7.10"]
                 [com.taoensso/nippy "2.14.0"]

                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]

                 ;; Components
                 [mount "0.1.16"]

                 ;; REST
                 [cheshire "5.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.8.0"]
                 [ring/ring-json "0.5.0"]]
  
  :profiles {:uberjar {:aot :all}

             :dev {:dependencies [[org.clojure/tools.namespace "1.0.0"]]
                   :source-paths ["dev"]}})
