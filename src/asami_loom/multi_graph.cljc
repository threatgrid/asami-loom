(ns ^{:doc "Extension of Asami in-memory multi-graph index to Loom protocols."
      :author "Paula Gearon"}
    asami-loom.multi-graph
  (:require [asami.graph :as gr :refer [graph-delete resolve-triple]]
            [clojure.set :as set]
            [loom.graph :as loom :refer [nodes edges has-node? successors* build-graph
                                         out-degree out-edges
                                         add-edges* add-nodes*
                                         add-nodes add-edges]]
            #?(:clj  [asami.multi-graph :as multi-graph :refer [multi-graph-add]]
               :cljs [asami.multi-graph :as multi-graph :refer [MultiGraph multi-graph-add]])
            #?(:clj  [schema.core :as s]
               :cljs [schema.core :as s :include-macros true]))
  #?(:clj (:import [asami.multi_graph MultiGraph])))

(defn node-only?
  "Tests if a graph contains a node without associated edges"
  [{spo :spo} n]
  (let [o-level (get-in spo [n nil])]
    (and (contains? o-level nil)
         (not (:label (o-level nil))))))

(defn clean
  "Removes a node-only entry from a graph"
  [g n]
  (if (node-only? g n)
    (graph-delete g n nil nil)
    g))

(defn all-triple-edges
  [g n]
  (concat
    (map (cons n (resolve-triple g n '?a '?b)))
    (map (conj (resolve-triple g '?a '?b n) n))))

(extend-type MultiGraph
  loom/Graph
  (nodes [{:keys [spo osp] :as graph}]
    (disj (into (set (keys spo)) (keys osp)) nil))

  (edges [{:keys [osp] :as graph}]
    (for [[o sp] osp :when o s (keys sp)] [s o]))

  (has-node? [{:keys [spo osp] :as graph} node]
    (when node (boolean (or (spo node) (osp node)))))

  (has-edge? [{:keys [osp] :as graph} n1 n2]
    (boolean (get-in osp [n2 n1])))

  (successors* [{:keys [spo] :as graph} node]
    (disj (apply set/union (vals (spo node))) nil))

  (out-degree [{:keys [spo] :as graph} node]
    (->> (spo node) vals (mapcat vals) (map :count) (apply +)))

  (out-edges [{:keys [spo] :as graph} node]
    (for [[o md] (->> (spo node) vals (apply concat))
          _ (range (:count md))]
      [node o]))

  loom/EditableGraph
  (add-nodes* [gr nodes]
    (reduce
     (fn [{:keys [spo osp] :as g} n]
       (if (or (spo n) (osp n)) g (gr/graph-add g n nil nil)))
     gr nodes))

  (add-edges* [gr edges]
    (reduce
     (fn [g [s o]]
       (-> g
           (clean s)
           (clean o)
           (multi-graph-add s :to o)))
     gr edges))

  (remove-nodes* [gr nodes]
    (reduce
     (fn [{:keys [spo osp] :as g} [s o]]
       (let [other-ends (into (set (mapcat keys (vals (spo s)))) (keys (osp o)))
             all-triples (concat (all-triple-edges g s) (all-triple-edges g o))
             {:keys [spo* osp*] :as scrubbed} (reduce #(apply graph-delete %1 %2)
                                                      (graph-delete g s nil nil)
                                                      all-triples)
             reinserts (remove #(or (spo* %) (osp* %)) other-ends)]
         (reduce #(gr/graph-add %1 %2 nil nil) scrubbed reinserts)))
     gr nodes))

  (remove-all [gr] multi-graph/empty-multi-graph)

  loom/Digraph
  (predecessors* [{:keys [osp]} node]
    (for [[s pm] (osp node) _ (range (apply + (map :count (vals pm))))] s))

  (in-degree [{:keys [osp]} node]
    (->> (osp node) vals (map vals) (mapcat (partial map :count)) (apply +)))

  (in-edges [{:keys [osp]} node]
    (for [[s pm] (osp node) _ (range (apply + (map :count (vals pm))))] [s node]))

  (transpose [{:keys [spo osp] :as gr}]
    (let [nodes (keys (get osp nil))
          tuples (for [s (keys spo) [p om] (spo s) [o {c :count}] om] [s p o c])]
      (-> (reduce (fn [g [s p o c]] (if o (multi-graph-add g o p s c) g)) multi-graph/empty-multi-graph tuples)
          (add-nodes* nodes))))

  loom/WeightedGraph
  (weight*
    ([g [n1 n2]]
     (loom/weight g n1 n2))
    ([g n1 n2]
     (->> (get-in g [:osp n2 n1])
          vals 
          (map :count)
          (apply +)))))

(defn weighted-graph
  "Creates an index graph with a set of directed, weighted edges."
  [& inits]
  (apply build-graph multi-graph/empty-multi-graph inits))

