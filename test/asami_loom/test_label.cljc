(ns asami-loom.test-label
  "Tests Loom labels"
  (:require [loom.graph :as loom]
            [loom.label :as lbl]
            [asami-loom.index :as aloom :refer [graph]]
            [asami-loom.multi-graph :as amloom :refer [weighted-graph]]
            [asami-loom.label]
            #?(:clj  [clojure.test :refer [deftest is are use-fixtures testing]]
               :cljs [clojure.test :refer-macros [deftest is are run-tests use-fixtures testing]])))

(defn labeled-graph [gr]
  (let [g (gr [1 2] [2 3] [2 4] [3 5] [4 5])
        lg1 (-> g
               (lbl/add-label 1 "node label")
               (lbl/add-label 2 3 "edge label"))
        lg2 (-> (gr)
                (lbl/add-labeled-nodes
                 1 "node label 1"
                 2 "node label 2")
                (lbl/add-labeled-edges
                 [1 2] "edge label 1"
                 [2 3] "edge label 2"))]
    (is (= "node label" (lbl/label lg1 1)))
    (is (= "edge label" (lbl/label lg1 2 3)))
    (is (= #{1 2 3} (set (loom/nodes lg2))))
    (is (= #{[1 2] [2 3]} (set (loom/edges lg2))))
    (is (= "node label 1" (lbl/label lg2 1)))
    (is (= "node label 2" (lbl/label lg2 2)))
    (is (= "edge label 1" (lbl/label lg2 1 2)))
    (is (= "edge label 2" (lbl/label lg2 2 3)))))

(deftest labeled-graph-test
  (labeled-graph graph)
  (labeled-graph weighted-graph))

#?(:cljs (run-tests))
