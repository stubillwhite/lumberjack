(defproject lumberjack "0.1.0-SNAPSHOT"

  :description "TODO"

  :url "TODO"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repl-options {:port 4555}

  :plugins []

  :main lumberjack.app
  
  :dependencies [[nrepl "0.3.1"]
                 [org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/tools.trace "0.7.10"]
                 [com.rpl/specter "1.1.3"]
                 [mount "0.1.16"]
                 [com.taoensso/nippy "2.14.0"]]
  
  :profiles {:uberjar {:aot :all}

             :dev {:dependencies [[org.clojure/tools.namespace "1.0.0"]]
                   :source-paths ["dev"]}})
