(ns asami-loom.test-index-loom
  "Tests basic Loom functions"
  (:require [loom.graph :as loom :refer [nodes edges has-node? has-edge?
                                         transpose]]
            [asami-loom.index :as aloom :refer [graph]]
            [asami-loom.multi-graph :as amloom :refer [weighted-graph]]
            #?(:clj  [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])
            #?(:clj  [clojure.test :refer [is are use-fixtures testing]]
               :cljs [clojure.test :refer-macros [is are run-tests use-fixtures testing]])
            #?(:clj  [schema.test :as st :refer [deftest]]
               :cljs [schema.test :as st :refer-macros [deftest]])))

(defn build-graph [graph-fn]
  (let [g1 (graph-fn [1 2] [1 3] [2 3] 4)
        g2 (graph-fn {1 [2 3] 2 [3] 4 []})
        g3 (graph-fn g1)
        g4 (graph-fn g3 (graph-fn [5 6]) [7 8] 9)
        g5 (graph-fn)]
    (testing "Construction, nodes, edges"
      (are [expected got] (= expected got)
           #{1 2 3 4} (set (nodes g1))
           #{[1 2] [1 3] [2 3]} (set (edges g1))
           (set (nodes g2)) (set (nodes g1))
           (set (edges g2)) (set (edges g1))
           (set (nodes g3)) (set (nodes g1))
           (set (nodes g3)) (set (nodes g1))
           #{1 2 3 4 5 6 7 8 9} (set (nodes g4))
           #{[1 2] [1 3] [2 3] [5 6] [7 8]} (set (edges g4))
             #{} (set (nodes g5))
             #{} (set (edges g5))
             true (has-edge? g1 1 2)
             false (has-node? g1 5)
             false (has-edge? g1 4 1)))))

(deftest build-graph-test
  (build-graph graph)
  (build-graph weighted-graph))

(defn simple-digraph [graph-fn]
  (let [g1 (graph-fn [1 2] [1 3] [2 3] 4)
        g2 (graph-fn {1 [2 3] 2 [3] 4 []})
        g3 (graph-fn g1)
        g4 (graph-fn g3 (graph-fn [5 6]) [7 8] 9)
        g5 (graph-fn)
        g6 (transpose g1)]
    (testing "Construction, nodes, edges"
      (are [expected got] (= expected got)
           #{1 2 3 4} (set (nodes g1))
           #{1 2 3 4} (set (nodes g6))
           #{[1 2] [1 3] [2 3]} (set (edges g1))
           #{[2 1] [3 1] [3 2]} (set (edges g6))
           (set (nodes g2)) (set (nodes g1))
           (set (edges g2)) (set (edges g1))
           (set (nodes g3)) (set (nodes g1))
           (set (nodes g3)) (set (nodes g1))
           #{1 2 3 4 5 6 7 8 9} (set (nodes g4))
           #{[1 2] [1 3] [2 3] [5 6] [7 8]} (set (edges g4))
           #{} (set (nodes g5))
           #{} (set (edges g5))
           true (has-node? g1 4)
           true (has-edge? g1 1 2)
           false (has-node? g1 5)
           false (has-edge? g1 2 1)))))

(deftest simple-digraph-test
  (simple-digraph graph)
  (simple-digraph weighted-graph))

(deftest simple-weighted-digraph-test
  (let [g1 (weighted-graph [1 2 77] [1 3 88] [2 3 99] 4)
        g2 (weighted-graph {1 {2 77 3 88} 2 {3 99} 4 []})
        g3 (weighted-graph g1)
        g4 (weighted-graph g3 (weighted-graph [5 6 88]) [7 8] 9)
        g5 (weighted-graph)
        g6 (transpose g1)]
    (testing "Construction, nodes, edges"
      (are [expected got] (= expected got)
           #{1 2 3 4} (set (nodes g1))
           #{1 2 3 4} (set (nodes g6))
           #{[1 2] [1 3] [2 3]} (set (edges g1))
           #{[2 1] [3 1] [3 2]} (set (edges g6))
           (set (nodes g2)) (set (nodes g1))
           (set (edges g2)) (set (edges g1))
           (set (nodes g3)) (set (nodes g1))
           (set (nodes g3)) (set (nodes g1))
           #{1 2 3 4 5 6 7 8 9} (set (nodes g4))
           #{[1 2] [1 3] [2 3] [5 6] [7 8]} (set (edges g4))
           #{} (set (nodes g5))
           #{} (set (edges g5))
           true (has-node? g1 4)
           true (has-edge? g1 1 2)
           false (has-node? g1 5)
           false (has-edge? g1 2 1)))))

#?(:cljs (run-tests))
