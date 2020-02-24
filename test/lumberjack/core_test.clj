(ns lumberjack.core-test
  (:require
    [clojure.test :refer :all]
    [lumberjack.core :refer :all]))

(deftest add-one-given-value-then-increments
  (is (= 24 (add-one 23))))

