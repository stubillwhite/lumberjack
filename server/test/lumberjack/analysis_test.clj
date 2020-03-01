(ns lumberjack.analysis-test
  (:require
    [clojure.test :refer :all]
    [lumberjack.analysis :refer :all]
    [lumberjack.utils :refer [def- to-canonical-name]]))

(def- project-a      "project-a")
(def- project-b      "project-b")
(def- project-c      "project-c")
(def- non-clashing-1 "org-a:pkg-a:non-clashing-1")
(def- non-clashing-2 "org-a:pkg-b:non-clashing-2")
(def- clashing-1     "org-b:pkg-b:clashing-1")
(def- clashing-2     "org-b:pkg-b:clashing-2")

(def- stub-project-data {project-a {non-clashing-1
                                    {non-clashing-2 {}}}

                         project-b {non-clashing-1
                                    {non-clashing-2 {}}
                                    clashing-1     {}}

                         project-c {non-clashing-2 {}
                                    clashing-2     {}}})

(deftest project-names-given-stub-data-then-returns-projects
  (is (= [project-a project-b project-c] (project-names stub-project-data))))

(deftest dependencies-for-project-given-stub-data-then-returns-dependencies-for-project
  (is (= #{non-clashing-1 non-clashing-2}            (dependencies-for-project stub-project-data project-a)))
  (is (= #{non-clashing-1 non-clashing-2 clashing-1} (dependencies-for-project stub-project-data project-b)))
  (is (= #{non-clashing-2 clashing-2}                (dependencies-for-project stub-project-data project-c))))

(deftest all-dependencies-given-stub-data-then-returns-all-dependencies
  (is (= #{non-clashing-1 non-clashing-2 clashing-1 clashing-2}
         (all-dependencies stub-project-data))))

(deftest clashes-given-clashing-dependencies-then-returns-clashing-dependencies
  (is (= #{clashing-1 clashing-2} (clashes stub-project-data))))

(deftest projects-referencing-given-common-dependency-then-returns-all-referencing-projects
  (is (= #{project-a project-b} (projects-referencing stub-project-data non-clashing-1))))

(deftest -referencing-given-common-dependency-then-returns-all-referencing-projects
  (is (= {non-clashing-1
          {non-clashing-2 {}}} (dependency-graph-for-project stub-project-data project-a)))
  (is (= {non-clashing-1
          {non-clashing-2 {}}
          clashing-1 {}} (dependency-graph-for-project stub-project-data project-b)))
  (is (= {non-clashing-2 {}
          clashing-2 {}}
       (dependency-graph-for-project stub-project-data project-c))))
