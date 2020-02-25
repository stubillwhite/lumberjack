(ns client.pages.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [client.utils :refer [to-clj]]))

(deftest stub-test-one
  (is (= 2 (+ 1 1))))
