(ns ^{:doc "Extension of Asami block-based durable index to Loom protocols"
      :author "Paula Gearon"}
    asami-loom.durable
  (:require [asami.graph :as gr :refer [graph-add graph-delete resolve-triple]]
            [clojure.set :as set]
            [loom.graph :as loom :refer [nodes edges has-node? successors* build-graph
                                         out-degree out-edges
                                         add-edges* add-nodes*]]
            #?(:clj  [asami.durable.common :as c]
               :cljs [asami.durable.common :as c :refer [BlockGraph]]))
  #?(:clj (:import [asami.index BlockGraph])))

(defn node-only?
  "Tests if a graph contains a node without associated edges"
  [g n]
  (seq (resolve-triple g n :tg/nil :tg/nil)))

(defn clean
  "Removes a node-only entry from a graph"
  [g n]
  (if (node-only? g n)
    (graph-delete g n :tg/nil :tg/nil)
    g))

(defn all-triple-edges
  [g n]
  (concat
    (map #(cons n %) (resolve-triple g n '?a '?b))
    (map #(conj % n) (resolve-triple g '?a '?b n))))

(extend-type BlockGraph
  loom/Graph
  (nodes [{:keys [spot ospt pool] :as graph}]
    (let [lineproc (map #(c/find-object pool (first %)))
          subjects (c/find-tuples spot [])
          objects (c/find-tuples ospt [])]
      (disj (into #{} lineproc (concat subjects objects)) :tg/nil)))

  (edges [{:keys [ospt pool] :as graph}]
    (let [tuples (c/find-tuples ospt [])
          tx (comp (map (fn [[o s]] [(c/find-object pool s) (c/find-object pool o)]))
                   (remove #(= :tg/nil (first %)))
                   (dedupe))]
      (sequence tx tuples)))

  (has-node? [{:keys [spot ospt pool] :as graph} node]
    (when (and node (not= :tg/nil node))
      (let [n (c/find-id pool node)]
        (boolean (or (seq (c/find-tuples spot [n]))
                     (seq (c/find-tuples ospt [n])))))))

  (has-edge? [{:keys [ospt pool] :as graph} node1 node2]
    (let [n1 (c/find-id pool node1)
          n2 (c/find-id pool node2)]
      (boolean (and n1 n2 (seq (c/find-tuples ospt [n2 n1]))))))

  (successors* [{:keys [spot pool] :as graph} node]
    (let [n (c/find-id pool node)
          os (comp (map #(nth % 2)) (map #(c/find-object pool %)))
          s (into #{} os (c/find-tuples spot [n]))]
      (disj s :tg/nil)))

  (out-degree [{:keys [spot pool] :as graph} node]
    ;; drops duplicates for different predicates!
    (let [nil-val (c/find-id pool :tg/nil)
          n (c/find-id pool node)
          os (comp (map #(nth % 2)) (remove #(= nil-val %)))
          o (sequence os (c/find-tuples spot [n]))]
      (count o)))

  (out-edges [graph node]
    "Returns all the outgoing edges of node"
    (for [o (successors* graph node)] [node o]))

  loom/EditableGraph
  (add-nodes* [gr nodes]
    (reduce
     (fn [{:keys [spot ospt pool] :as g} node]
       (let [n (c/find-id pool node)] 
         (if (or (nil? n)
                 (and (empty? (c/find-tuples spot [n])) 
                      (empty? (c/find-tuples ospt [n]))))
           (graph-add g node :tg/nil :tg/nil)
           g)))
     gr nodes))

  (add-edges* [gr edges]
    (reduce
     (fn [g [s o]]
       (-> g
           (clean s)
           (clean o)
           (graph-add s :to o)))
     gr edges))

  ;; below here has been copy/pasted from index.cljc. Not yet converted and will fail
  (remove-nodes* [{pool :as gr} nodes]
    (reduce
     (fn [{:keys [spot ospt] :as g} [s o]]
       (let [other-ends (into (apply set/union (vals (spo s))) (keys (osp o)))
             all-triples (concat (all-triple-edges g s) (all-triple-edges g o))
             {:keys [spo* osp*] :as scrubbed} (reduce #(apply graph-delete %1 %2)
                                                      (graph-delete g s nil nil)
                                                      all-triples)
             reinserts (remove #(or (spo* %) (osp* %)) other-ends)]
         (reduce #(graph-add %1 %2 nil nil) scrubbed reinserts)))
     gr nodes))

  (remove-all [gr] index/empty-graph)

  loom/Digraph
  (predecessors* [{:keys [osp]} node]
    (keys (osp node)))

  (in-degree [{:keys [osp]} node]
    (count (osp node)))

  (in-edges [{:keys [osp]} node]
    (map (fn [s] [s node]) (keys (osp node))))

  (transpose [{:keys [osp] :as gr}]
    (let [nodes (keys (get osp nil))
          triples (resolve-triple gr '?a '?b '?c)]
      (-> (reduce (fn [g [a b c]] (if c (graph-add g c b a) g)) index/empty-graph triples)
          (add-nodes* nodes)))))

(defn graph
  "Creates an index graph with a set of edges. All edges are unlabelled."
  [& inits]
  (apply build-graph index/empty-graph inits))

