# asami-loom

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

## License

Copyright © 2019 Cisco Systems

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
