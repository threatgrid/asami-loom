(defproject org.clojars.quoll/asami-loom "0.3.1"
  :description "Loom extensions to Asami"
  :url "http://threatgrid/asami-loom"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/clojurescript "1.10.773"]
                 [prismatic/schema "1.1.12"] 
                 [org.clojars.quoll/asami "2.0.0-alpha4"]
                 [com.fasterxml.jackson.core/jackson-core "2.10.2"]
                 [aysylu/loom "1.0.2"]]
  :repl-options {:init-ns asami-loom.multi-graph}
  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {
    :builds {
      :dev
      {:source-paths ["src"]
       :compiler {
         :output-to "out/asami-loom/core.js"
         :optimizations :simple
         :pretty-print true}}
      :test
      {:source-paths ["src" "test"]
       :compiler {
         :output-to "out/asami-loom/test_memory.js"
         :optimizations :simple
         :pretty-print true}}
      }
    :test-commands {
      "unit" ["node" "out/asami-loom/test_memory.js"]}
    })
