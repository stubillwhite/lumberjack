(ns client.test-runner
  (:require client.pages.coffee-test
            client.utils-test
            [figwheel.main.testing :refer [run-tests-async]]))

(defn -main [& args]
  (run-tests-async 5000))
