(ns lumberjack.parsers.sbt-test
  (:require
    [clojure.test :refer :all]
    [lumberjack.parsers.sbt :refer :all]
    [clojure.string :as string]
    [lumberjack.utils :refer [def-]]))

(deftest parse-sbt-dependency-then-extracts
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 0} (parse-sbt-dependency "org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 1} (parse-sbt-dependency "  +-org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 2} (parse-sbt-dependency "  | +-org:pkg:ver")))
  (is (= {:org "org" :pkg "pkg" :ver "ver" :level 3} (parse-sbt-dependency "  | | +-org:pkg:ver"))))

(defn- mktree [coll]
  (->> coll
       (map (partial str "[info] "))
       (string/join "\n")))

(defn- dep [org pkg ver]
  {:org org :pkg pkg :ver ver})

(deftest parse-dependency-tree-given-empty-tree-then-extracts
  (is (= {(dep "org" "pkg" "ver") #{}} (parse-dependency-tree (mktree ["org:pkg:ver"])))))

(deftest parse-dependency-tree-given-basic-tree-then-extracts
  (let [tree (mktree ["org:pkg:ver"
                      "  +-a-org:a-pkg:a-ver"
                      "  +-b-org:b-pkg:b-ver"
                      "  | +-c-org:c-pkg:c-ver"])
        root (dep "org" "pkg" "ver")
        a    (dep "a-org" "a-pkg" "a-ver")
        b    (dep "b-org" "b-pkg" "b-ver")
        c    (dep "c-org" "c-pkg" "c-ver")]
    (is (= {root #{a b}
            a    #{}
            b    #{c}
            c    #{}}
           (parse-dependency-tree tree)))))

(deftest parse-dependency-tree-given-complex-tree-then-extracts
  (let [tree (mktree ["org:pkg:ver"
                      "  +-a-org:a-pkg:a-ver"
                      "  +-b-org:b-pkg:b-ver"
                      "  | +-c-org:c-pkg:c-ver"
                      "  |   +-d-org:d-pkg:d-ver"
                      "  |"
                      "  +-e-org:e-pkg:e-ver"
                      "  | +-f-org:f-pkg:f-ver"])
        root (dep "org" "pkg" "ver")
        a    (dep "a-org" "a-pkg" "a-ver")
        b    (dep "b-org" "b-pkg" "b-ver")
        c    (dep "c-org" "c-pkg" "c-ver")
        d    (dep "d-org" "d-pkg" "d-ver")
        e    (dep "e-org" "e-pkg" "e-ver")
        f    (dep "f-org" "f-pkg" "f-ver")]
    (is (= {root #{a b e}
            a    #{}
            b    #{c}
            c    #{d}
            d    #{}
            e    #{f}
            f    #{}}
           (parse-dependency-tree tree)))))






