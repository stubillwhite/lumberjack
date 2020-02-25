(ns lumberjack.analysis-test
  (:require
    [clojure.test :refer :all]
    [lumberjack.analysis :refer :all]
    [lumberjack.utils :refer [def-]]))

(defn- dep [org pkg ver]
  {:org org :pkg pkg :ver ver})

(def- non-clashing-1 (dep "org-a" "pkg-a" "non-clashing-1"))
(def- non-clashing-2 (dep "org-a" "pkg-b" "non-clashing-2"))
(def- clashing-1     (dep "org-b" "pkg-b" "clashing-1"))
(def- clashing-2     (dep "org-b" "pkg-b" "clashing-2"))

(def- projects {"project-a" {non-clashing-1 nil
                             non-clashing-2 nil}

                "project-b" {non-clashing-1 nil
                             non-clashing-2 nil
                             clashing-1     nil}

                "project-c" {non-clashing-2 nil
                             clashing-2     nil}})

(deftest clashes-given-some-dependency-clashes-then-identifies
  (let [expected [{:org "org-b" :pkg "pkg-b" :ver "clashing-1"}
                  {:org "org-b" :pkg "pkg-b" :ver "clashing-2"}]]
    (is (= expected (clashes projects)))))

(deftest projects-referencing-given-common-dependency-then-returns-all-referencing-projects
  (is (= #{"project-a" "project-b"} (projects-referencing projects non-clashing-1))))
