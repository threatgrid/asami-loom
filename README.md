# asami-loom [![Build Status](https://travis-ci.org/threatgrid/asami-loom.svg?branch=master)](https://travis-ci.org/threatgrid/asami-loom)

This library extends Asami in-memory graphs to Loom.

By requiring `asami-loom.index`, then the `asami.index.IndexGraph` graphs are extended to:
- `loom.graph.Graph`
- `loom.graph.EditableGraph`
- `loom.graph.DiGraph`

By requiring `asami-loom.multi-graph`, then the `asami.multi-graph.MultiGraph` graphs are extended to:
- `loom.graph.Graph`
- `loom.graph.EditableGraph`
- `loom.graph.DiGraph`
- `loom.graph.WeightedGraph`

By requiring `asami-loom.label`, then both the `asami.index.IndexGraph` and `asami.multi-graph.MultiGraph`
graphs get extended to:
- `loom.label.LabeledGraph`

## Usage

Requiring one of the above namespaces in any namespace that uses Asami in-memory graphs will automatically
apply the extensions. Loom can use the graphs with the standard APIs.

## Example

Configuring Asami Loom can be done by requiring the appropriate name spaces. Then just load data into Asami as usual.

Note: Some of this API will change soon to simplify. Specifically, the use of the Naga Storage API will be dropped.
This means that `naga.store` won't be needed, as the graph won't need to be extracted from the store object.

```clojure
(require '[asami.core :refer [empty-store q]])
(require '[naga.store :refer [new-node assert-data]])

(defn nn [] (new-node empty-store))

(def data
 (let [pmc (nn)
       gh (nn)
       jl (nn)
       r1 (nn)
       r2 (nn)
       r3 (nn)
       r4 (nn)
       r5 (nn)]
   [[pmc :artist/name "Paul McCartney"]
    [gh :artist/name "George Harrison"]
    [jl :artist/name "John Lennon"]
    [r1 :release/artists pmc]
    [r1 :release/name "My Sweet Lord"]
    [r2 :release/artists gh]
    [r2 :release/name "Electronic Sound"]
    [r3 :release/artists gh]
    [r3 :release/name "Give Me Love (Give Me Peace on Earth)"]
    [r4 :release/artists gh]
    [r4 :release/name "All Things Must Pass"]
    [r5 :release/artists jl]
    [r5 :release/name "Imagine"]]))

(def graph (:graph (assert-data empty-store)))
```

The entity IDs created with `nn` above are represented as keywords with the namespace "mem".
Also, edges are typically keywords, but can also be strings.
With these assumptions in mind, we can use the following definitions to get labels for edges or nodes.

```clojure
(defn edge-label
  [g s d]
  (let [edge (ffirst (concat (resolve-triple g s '?e d)
                             (resolve-triple g d '?e s)))]
    (cond (string? edge) edge
          (keyword? edge) (name edge)
          :default (str edge))))

(defn node-label
  [g n]
  (let [id (ffirst (resolve-triple g n :id '?id))]
    (cond id (str id)
          (and (keyword? n) (= (namespace n) "mem")) (str ":" (name n))
          :default (str n))))
```

With this configuration set up, the Loom `display` function can be used to create a graphical view.

```clojure
(require '[asami-loom.index])
(require '[asami-loom.label])
(require '[loom.io :as loom-io])

(loom-io/view graph :fmt :pdf :alg :sfdp :edge-label edge-label :node-label node-label)
```

## View Parameters

Valid algorithms are:
- :dot
- :neato
- :fdp
- :sfdp
- :twopi
- :circo

Valid Formats are:
- :png
- :ps
- :pdf
- :svg

Loom launches the system default viewers for these file types.

## License

Copyright © 2020 Cisco Systems

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
