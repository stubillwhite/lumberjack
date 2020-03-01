(ns lumberjack.parsers.sbt-test
  (:require
    [clojure.test :refer :all]
    [lumberjack.parsers.sbt :refer :all]
    [clojure.string :as string]
    [lumberjack.utils :refer [def-]]))

(deftest parse-sbt-dependency-then-extracts
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 0 :evicted nil}    (parse-sbt-dependency "org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 1 :evicted nil}    (parse-sbt-dependency "  +-org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 2 :evicted nil}    (parse-sbt-dependency "  | +-org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 3 :evicted nil}    (parse-sbt-dependency "  | | +-org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 3 :evicted "ver2"} (parse-sbt-dependency "  | | +-org:pkg:ver (evicted by: ver2)"))))

(defn- mktree [coll]
  (->> coll
       (map (partial str "[info] "))
       (string/join "\n")))

(deftest parse-dependency-tree-given-nested-dependencies-then-parses-structure
  (let [tree (mktree ["org:pkg:ver"
                      "  +-a-org:a-pkg:a-ver"
                      "  | +-b-org:b-pkg:b-ver"
                      "  |   +-c-org:c-pkg:c-ver"
                      "  |"
                      "  +-d-org:d-pkg:d-ver"
                      "    +-e-org:e-pkg:e-ver"
                      "      +-f-org:f-pkg:f-ver"])]
    (is (= {"org:pkg:ver"
            {"a-org:a-pkg:a-ver"
             {"b-org:b-pkg:b-ver"
              {"c-org:c-pkg:c-ver" {}}}
             "d-org:d-pkg:d-ver"
             {"e-org:e-pkg:e-ver"
              {"f-org:f-pkg:f-ver" {}}}}}
           (parse-dependency-tree tree)))))

(deftest parse-dependency-tree-given-evicted-dependencies-then-ignores-evicted-entries
  (let [tree (mktree ["org:pkg:ver"
                      "  +-a-org:a-pkg:a-ver (evicted by: a-ver2)"
                      "  | +-b-org:b-pkg:b-ver"
                      "  |   +-c-org:c-pkg:c-ver"
                      "  |"
                      "  +-a-org:a-pkg:a-ver2"
                      "    +-b-org:b-pkg:b-ver2"
                      "      +-c-org:c-pkg:c-ver2"])]
    (is (= {"org:pkg:ver"
            {"a-org:a-pkg:a-ver2"
             {"b-org:b-pkg:b-ver2"
              {"c-org:c-pkg:c-ver2" {}}}}}
           (parse-dependency-tree tree)))))
