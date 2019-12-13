(ns ^{:doc "Extension of Asami graphs to Loom labeled graph protocols"
      :author "Paula Gearon"}
  asami-loom.label
  (:require [asami.graph :as asami :refer [graph-add graph-delete]]
            [loom.label :as label]
            #?(:clj  [asami.index :as index]
               :cljs [asami.index :as index :refer [GraphIndexed]])
            #?(:clj  [asami.multi-graph :as multi-graph]
               :cljs [asami.multi-graph :as multi-graph :refer [MultiGraph]])
            )
  #?(:clj (:import [asami.index GraphIndexed]
                   [asami.multi_graph MultiGraph])))

(defn update-predicate
  [g s p o new-p]
  (-> g
      (graph-delete s p o)
      (graph-add s new-p o)))

(extend-type GraphIndexed
  label/LabeledGraph

  (add-label
   ([{:keys [spo osp] :as g} node label]
    (if (contains? (get-in spo [node nil]) nil)
      (update-predicate g node nil nil label)
      (graph-add g node label nil)))
   ([{:keys [spo pos osp] :as g} n1 n2 label]
    (if (get-in spo [n1 :to n2])
      (update-predicate g n1 :to n2 label)
      (graph-add g n1 label n2))))

  (remove-label
   ([g node]
    (let [ps (get-in [g :osp nil node])
          labels (filter string? ps)]
      (if (seq labels)
        (reduce #(graph-delete %1 node %2 nil) g labels)
        g)))
   ([g n1 n2]
    (let [ps (get-in [g :osp n2 n1])
          labels (filter string? ps)]
      (if (seq labels)
        (-> (reduce #(graph-delete %1 n1 %2 n2) g labels)
            (graph-add n1 :to n2))
        g))))

  (label
   ([g node]
    (let [ps (get-in g [:osp nil node])]
      (or (first (filter string? ps))
          (if-let [p (some identity ps)]
            (name p)
            (name node)))))
   ([g n1 n2]
    (let [ps (get-in g [:osp n2 n1])]
      (or (first (filter string? ps))
          (if-let [p (some identity ps)] (name p) "-"))))))

(defn update-label
  [g s p o label]
  (-> g
      (assoc-in [:spo s p o :label] label)
      (assoc-in [:pos p o s :label] label)
      (assoc-in [:osp o s p :label] label)))

(defn dissoc-in
  [g path]
  (update-in g (butlast path) dissoc (last path)))

(defn remove-label
  [g s p o]
  (-> g
      (dissoc-in [:spo s p o :label])
      (dissoc-in [:pos p o s :label])
      (dissoc-in [:osp o s p :label])))

(extend-type MultiGraph
  label/LabeledGraph

  (add-label
   ([{:keys [spo osp] :as g} node label]
    (update-label g node nil nil label))
   ([{:keys [spo pos osp] :as g} n1 n2 label]
    (update-label g n1 :to n2 label)))

  (remove-label
   ([g node]
    (if (get-in [g :spo node nil nil :label])
      (remove-label g node nil nil)
      g))
   ([g n1 n2]
    (if (get-in [g :spo n1 :to n2])
      (remove-label g n1 :to n2)
      g)))

  (label
   ([g node]
    (let [node-map (get-in g [:spo node])]
      (or
       (get-in node-map [nil nil :label])
       (some-> (:id node-map) keys first)
       (some-> (:name node-map) keys first)
       (name node))))
   ([g n1 n2]
    (or (get-in g [:spo n1 :to n2 :label])
        (let [ps (keys (get-in g [:osp n2 n1]))]
          (or (first (filter string? ps))
              (if-let [p (some identity ps)] (name p) "-")))))))
