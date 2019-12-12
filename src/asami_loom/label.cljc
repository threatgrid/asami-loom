(ns ^{:doc "Extension of Asami graphs to Loom labeled graph protocols"
      :author "Paula Gearon"}
  asami-loom.label
  (:require [loom.label :as label]
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
        (-> (reduce #(graph-delete %1 node %2 nil) g labels)
            (graph-add n1 :to n2))
        g))))

  (label
   ([g node]
    (label g node nil))
   ([g n1 n2]
    (-> g
        (get-in [:osp n2 n1])
        keys
        (filter string?)
        first))))

