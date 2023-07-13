# Protocol Documentation
<a name="top"></a>

## Table of Contents

- [atom.proto](#atom-proto)
    - [AdditionalEdgeProperty](#atom-AdditionalEdgeProperty)
    - [AdditionalNodeProperty](#atom-AdditionalNodeProperty)
    - [BoolList](#atom-BoolList)
    - [ContainedRefs](#atom-ContainedRefs)
    - [CpgOverlay](#atom-CpgOverlay)
    - [CpgStruct](#atom-CpgStruct)
    - [CpgStruct.Edge](#atom-CpgStruct-Edge)
    - [CpgStruct.Edge.Property](#atom-CpgStruct-Edge-Property)
    - [CpgStruct.Node](#atom-CpgStruct-Node)
    - [CpgStruct.Node.Property](#atom-CpgStruct-Node-Property)
    - [DataFlowSlice](#atom-DataFlowSlice)
    - [DataFlowSlice.Edges](#atom-DataFlowSlice-Edges)
    - [DataFlowSlice.Flows](#atom-DataFlowSlice-Flows)
    - [DataFlowSlice.Graph](#atom-DataFlowSlice-Graph)
    - [DataFlowSlice.Nodes](#atom-DataFlowSlice-Nodes)
    - [DataFlowSlice.Paths](#atom-DataFlowSlice-Paths)
    - [DiffGraph](#atom-DiffGraph)
    - [DiffGraph.Entry](#atom-DiffGraph-Entry)
    - [DiffGraph.RemoveEdge](#atom-DiffGraph-RemoveEdge)
    - [DiffGraph.RemoveEdgeProperty](#atom-DiffGraph-RemoveEdgeProperty)
    - [DiffGraph.RemoveNode](#atom-DiffGraph-RemoveNode)
    - [DiffGraph.RemoveNodeProperty](#atom-DiffGraph-RemoveNodeProperty)
    - [DoubleList](#atom-DoubleList)
    - [FloatList](#atom-FloatList)
    - [IntList](#atom-IntList)
    - [LongList](#atom-LongList)
    - [PropertyValue](#atom-PropertyValue)
    - [StringList](#atom-StringList)
    - [UsageSlice](#atom-UsageSlice)
    - [UsageSlice.ArgToCalls](#atom-UsageSlice-ArgToCalls)
    - [UsageSlice.DefinedBy](#atom-UsageSlice-DefinedBy)
    - [UsageSlice.Fields](#atom-UsageSlice-Fields)
    - [UsageSlice.InvokedCalls](#atom-UsageSlice-InvokedCalls)
    - [UsageSlice.MethodUsageSlice](#atom-UsageSlice-MethodUsageSlice)
    - [UsageSlice.ObjectUsageSlice](#atom-UsageSlice-ObjectUsageSlice)
    - [UsageSlice.Procedures](#atom-UsageSlice-Procedures)
    - [UsageSlice.TargetObj](#atom-UsageSlice-TargetObj)
    - [UsageSlice.UserDefinedTypes](#atom-UsageSlice-UserDefinedTypes)
  
    - [CONTROL_STRUCTURE_TYPES](#atom-CONTROL_STRUCTURE_TYPES)
    - [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType)
    - [DispatchTypes](#atom-DispatchTypes)
    - [EdgePropertyName](#atom-EdgePropertyName)
    - [EvaluationStrategies](#atom-EvaluationStrategies)
    - [LANGUAGES](#atom-LANGUAGES)
    - [ModifierTypes](#atom-ModifierTypes)
    - [NodePropertyName](#atom-NodePropertyName)
    - [NodeType](#atom-NodeType)
    - [UsageSlice.LabelType](#atom-UsageSlice-LabelType)
  
- [Scalar Value Types](#scalar-value-types)



<a name="atom-proto"></a>
<p align="right"><a href="#top">Top</a></p>

## atom.proto



<a name="atom-AdditionalEdgeProperty"></a>

### AdditionalEdgeProperty



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| edge_id | [int64](#int64) |  |  |
| property | [CpgStruct.Edge.Property](#atom-CpgStruct-Edge-Property) |  |  |
| out_node_key | [int64](#int64) |  |  |
| in_node_key | [int64](#int64) |  |  |
| edge_type | [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType) |  |  |






<a name="atom-AdditionalNodeProperty"></a>

### AdditionalNodeProperty



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| node_id | [int64](#int64) |  |  |
| property | [CpgStruct.Node.Property](#atom-CpgStruct-Node-Property) |  |  |






<a name="atom-BoolList"></a>

### BoolList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [bool](#bool) | repeated |  |






<a name="atom-ContainedRefs"></a>

### ContainedRefs



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| local_name | [string](#string) |  |  |
| refs | [int64](#int64) | repeated |  |






<a name="atom-CpgOverlay"></a>

### CpgOverlay
Overlays can be stacked onto each other, therefor their node ids must be globally unique.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| node | [CpgStruct.Node](#atom-CpgStruct-Node) | repeated |  |
| edge | [CpgStruct.Edge](#atom-CpgStruct-Edge) | repeated |  |
| node_property | [AdditionalNodeProperty](#atom-AdditionalNodeProperty) | repeated |  |
| edge_property | [AdditionalEdgeProperty](#atom-AdditionalEdgeProperty) | repeated |  |






<a name="atom-CpgStruct"></a>

### CpgStruct
This is the CORE structure that represents a Code Property Graph for the given language
This structure must be serialized as bytes and stored in a zip file (such as app.atom) with the name &#34;cpg.proto&#34;

Example code snippet in Python
```python
atom_struct = atom.CpgStruct(node=[method])
with ZipFile(file_name, &#34;w&#34;) as zip_file:
    zip_file.writestr(&#34;cpg.proto&#34;, bytes(atom_struct))
```

Example code snippet in TypeScript
```typescript
const methodFullName = new atom.CpgStruct.Node.Property({
 name: atom.NodePropertyName.FULL_NAME,
 value: new atom.PropertyValue({ string_value: &#34;main&#34; }),
});
const method = new atom.CpgStruct.Node({
  key: 1,
  type: atom.NodeType.METHOD,
  property: [methodFullName],
});
const atomStruct = new atom.CpgStruct({ node: [method] });
const serializedBytes = atomStruct.serialize();
```


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| node | [CpgStruct.Node](#atom-CpgStruct-Node) | repeated |  |
| edge | [CpgStruct.Edge](#atom-CpgStruct-Edge) | repeated |  |






<a name="atom-CpgStruct-Edge"></a>

### CpgStruct.Edge
Represents a directed edge of a graph


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| src | [int64](#int64) |  | Source node. |
| dst | [int64](#int64) |  | Destination node. |
| type | [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType) |  |  |
| property | [CpgStruct.Edge.Property](#atom-CpgStruct-Edge-Property) | repeated |  |






<a name="atom-CpgStruct-Edge-Property"></a>

### CpgStruct.Edge.Property
Edge properties.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [EdgePropertyName](#atom-EdgePropertyName) |  |  |
| value | [PropertyValue](#atom-PropertyValue) |  |  |






<a name="atom-CpgStruct-Node"></a>

### CpgStruct.Node



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| key | [int64](#int64) |  |  |
| type | [NodeType](#atom-NodeType) |  |  |
| property | [CpgStruct.Node.Property](#atom-CpgStruct-Node-Property) | repeated |  |






<a name="atom-CpgStruct-Node-Property"></a>

### CpgStruct.Node.Property
Node properties.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [NodePropertyName](#atom-NodePropertyName) |  |  |
| value | [PropertyValue](#atom-PropertyValue) |  |  |






<a name="atom-DataFlowSlice"></a>

### DataFlowSlice
DataFlow slices offers a list of nodes and edges exported from data dependency graph


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| graph | [DataFlowSlice.Graph](#atom-DataFlowSlice-Graph) |  |  |
| path | [DataFlowSlice.Paths](#atom-DataFlowSlice-Paths) |  |  |






<a name="atom-DataFlowSlice-Edges"></a>

### DataFlowSlice.Edges



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| src | [uint32](#uint32) |  | Source node id |
| dst | [uint32](#uint32) |  | Destination node id |
| label | [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType) |  | Edge type |






<a name="atom-DataFlowSlice-Flows"></a>

### DataFlowSlice.Flows



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [uint32](#uint32) | repeated | Node id |






<a name="atom-DataFlowSlice-Graph"></a>

### DataFlowSlice.Graph



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| nodes | [DataFlowSlice.Nodes](#atom-DataFlowSlice-Nodes) | repeated |  |
| edges | [DataFlowSlice.Edges](#atom-DataFlowSlice-Edges) | repeated |  |






<a name="atom-DataFlowSlice-Nodes"></a>

### DataFlowSlice.Nodes



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [uint32](#uint32) |  | Id of the node |
| label | [NodeType](#atom-NodeType) |  | Label |
| name | [string](#string) |  | Name of the call or identifier or parameter |
| fullName | [string](#string) |  | Full name of the call |
| signature | [string](#string) |  | Method signature of the call |
| isExternal | [bool](#bool) |  | Boolean to indicate if this call belongs to an external method |
| code | [string](#string) |  | Source code |
| typeFullName | [string](#string) |  | Type full name |
| parentMethod | [string](#string) |  | Parent method |
| parentFile | [string](#string) |  | Parent filename |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |






<a name="atom-DataFlowSlice-Paths"></a>

### DataFlowSlice.Paths



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| flows | [DataFlowSlice.Flows](#atom-DataFlowSlice-Flows) | repeated | Flows from source to sink |






<a name="atom-DiffGraph"></a>

### DiffGraph
DiffGraphs can be created independently of each other and therefor when _adding_ nodes|edges,
each DiffGraph has its own ID space. However, when removing nodes|edges, the nodeIds refer to the
globally unique graph id space.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| entries | [DiffGraph.Entry](#atom-DiffGraph-Entry) | repeated |  |






<a name="atom-DiffGraph-Entry"></a>

### DiffGraph.Entry



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| node | [CpgStruct.Node](#atom-CpgStruct-Node) |  |  |
| edge | [CpgStruct.Edge](#atom-CpgStruct-Edge) |  |  |
| node_property | [AdditionalNodeProperty](#atom-AdditionalNodeProperty) |  |  |
| edge_property | [AdditionalEdgeProperty](#atom-AdditionalEdgeProperty) |  |  |
| remove_node | [DiffGraph.RemoveNode](#atom-DiffGraph-RemoveNode) |  |  |
| remove_node_property | [DiffGraph.RemoveNodeProperty](#atom-DiffGraph-RemoveNodeProperty) |  |  |
| remove_edge | [DiffGraph.RemoveEdge](#atom-DiffGraph-RemoveEdge) |  |  |
| remove_edge_property | [DiffGraph.RemoveEdgeProperty](#atom-DiffGraph-RemoveEdgeProperty) |  |  |






<a name="atom-DiffGraph-RemoveEdge"></a>

### DiffGraph.RemoveEdge



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| out_node_key | [int64](#int64) |  |  |
| in_node_key | [int64](#int64) |  |  |
| edge_type | [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType) |  |  |
| propertiesHash | [bytes](#bytes) |  | used to identify edges (since our edges don&#39;t have ids) |






<a name="atom-DiffGraph-RemoveEdgeProperty"></a>

### DiffGraph.RemoveEdgeProperty



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| out_node_key | [int64](#int64) |  |  |
| in_node_key | [int64](#int64) |  |  |
| edge_type | [CpgStruct.Edge.EdgeType](#atom-CpgStruct-Edge-EdgeType) |  |  |
| propertiesHash | [bytes](#bytes) |  | used to identify edges (since our edges don&#39;t have ids) |
| property_name | [EdgePropertyName](#atom-EdgePropertyName) |  |  |






<a name="atom-DiffGraph-RemoveNode"></a>

### DiffGraph.RemoveNode



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| key | [int64](#int64) |  |  |






<a name="atom-DiffGraph-RemoveNodeProperty"></a>

### DiffGraph.RemoveNodeProperty



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| key | [int64](#int64) |  |  |
| name | [NodePropertyName](#atom-NodePropertyName) |  |  |
| local_name | [string](#string) |  |  |






<a name="atom-DoubleList"></a>

### DoubleList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [double](#double) | repeated |  |






<a name="atom-FloatList"></a>

### FloatList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [float](#float) | repeated |  |






<a name="atom-IntList"></a>

### IntList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [int32](#int32) | repeated |  |






<a name="atom-LongList"></a>

### LongList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [int64](#int64) | repeated |  |






<a name="atom-PropertyValue"></a>

### PropertyValue
Message to store the property values such as string or int values


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| string_value | [string](#string) |  |  |
| bool_value | [bool](#bool) |  |  |
| int_value | [int32](#int32) |  |  |
| long_value | [int64](#int64) |  |  |
| float_value | [float](#float) |  |  |
| double_value | [double](#double) |  |  |
| string_list | [StringList](#atom-StringList) |  |  |
| bool_list | [BoolList](#atom-BoolList) |  |  |
| int_list | [IntList](#atom-IntList) |  |  |
| long_list | [LongList](#atom-LongList) |  |  |
| float_list | [FloatList](#atom-FloatList) |  |  |
| double_list | [DoubleList](#atom-DoubleList) |  |  |
| contained_refs | [ContainedRefs](#atom-ContainedRefs) |  |  |






<a name="atom-StringList"></a>

### StringList



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| values | [string](#string) | repeated |  |






<a name="atom-UsageSlice"></a>

### UsageSlice
The usages slice describes how a variable interacts within its procedure. This is perhaps a more &#34;descriptive&#34; slice in some ways. The variables are locals and parameters and the referencing identifiers are tracked to find what the variable calls and what calls it forms an argument of.
There are two lists. There is a list of MethodUsageSlice with key &#34;objectSlices&#34; and a list of UserDefinedTypes with key &#34;userDefinedTypes&#34;


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| objectSlices | [UsageSlice.MethodUsageSlice](#atom-UsageSlice-MethodUsageSlice) | repeated |  |
| userDefinedTypes | [UsageSlice.UserDefinedTypes](#atom-UsageSlice-UserDefinedTypes) | repeated |  |






<a name="atom-UsageSlice-ArgToCalls"></a>

### UsageSlice.ArgToCalls
The calls this object is observed to be an argument of.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| callName | [string](#string) |  | Call method name |
| resolvedMethod | [string](#string) |  | Full name of the resolved method |
| paramTypes | [string](#string) | repeated | Types of the parameters |
| returnType | [string](#string) |  | Return type |
| position | [uint32](#uint32) |  | Argument position |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |






<a name="atom-UsageSlice-DefinedBy"></a>

### UsageSlice.DefinedBy
Places where the given symbol is defined


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [string](#string) |  | variable or parameter name |
| typeFullName | [string](#string) |  | Fullname of the data type |
| resolvedMethod | [string](#string) |  | Method name |
| position | [uint32](#uint32) |  | Position |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |
| label | [string](#string) |  | Label describing the resolved method or position |






<a name="atom-UsageSlice-Fields"></a>

### UsageSlice.Fields
Represents a local transfer of data via aliasing. The data defined is via some alias.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [string](#string) |  | Name of the local variable |
| typeFullName | [string](#string) |  | Full name of the type |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |
| label | [UsageSlice.LabelType](#atom-UsageSlice-LabelType) |  | Type of the definition. Usually LOCAL |






<a name="atom-UsageSlice-InvokedCalls"></a>

### UsageSlice.InvokedCalls
The calls this object is observed to call.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| callName | [string](#string) |  | Call method name |
| resolvedMethod | [string](#string) |  | Full name of the resolved method |
| paramTypes | [string](#string) | repeated | Types of the parameters |
| returnType | [string](#string) |  | Return type |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |






<a name="atom-UsageSlice-MethodUsageSlice"></a>

### UsageSlice.MethodUsageSlice
Packages the object usage slices along with location and an optional method source code.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| code | [string](#string) |  | Raw source code of the method |
| fullName | [string](#string) |  | Method full name |
| fileName | [string](#string) |  | File name |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |
| usages | [UsageSlice.ObjectUsageSlice](#atom-UsageSlice-ObjectUsageSlice) | repeated |  |






<a name="atom-UsageSlice-ObjectUsageSlice"></a>

### UsageSlice.ObjectUsageSlice
Describes where and how the given external object/type is used


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| targetObj | [UsageSlice.TargetObj](#atom-UsageSlice-TargetObj) |  |  |
| definedBy | [UsageSlice.DefinedBy](#atom-UsageSlice-DefinedBy) |  |  |
| invokedCalls | [UsageSlice.InvokedCalls](#atom-UsageSlice-InvokedCalls) | repeated |  |
| argToCalls | [UsageSlice.ArgToCalls](#atom-UsageSlice-ArgToCalls) | repeated |  |






<a name="atom-UsageSlice-Procedures"></a>

### UsageSlice.Procedures
Details related to an observed call.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| callName | [string](#string) |  | Name of the method or call |
| resolvedMethod | [string](#string) |  | Full name of the resolved method |
| paramTypes | [string](#string) | repeated | Types of the parameters |
| returnType | [string](#string) |  | Type of the return value |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |






<a name="atom-UsageSlice-TargetObj"></a>

### UsageSlice.TargetObj
Represents a source of data-generation, i.e., where data is defined and can be assigned to some variable or used in an argument.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [string](#string) |  | variable or parameter name |
| typeFullName | [string](#string) |  | Fullname of the data type |
| position | [uint32](#uint32) |  | Position of the parameter or argument |
| lineNumber | [uint32](#uint32) |  | Line number in the file |
| columnNumber | [uint32](#uint32) |  | Column number |
| label | [UsageSlice.LabelType](#atom-UsageSlice-LabelType) |  |  |






<a name="atom-UsageSlice-UserDefinedTypes"></a>

### UsageSlice.UserDefinedTypes
Describes custom types defined within the application.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [string](#string) |  | Name of the type |
| fields | [UsageSlice.Fields](#atom-UsageSlice-Fields) | repeated |  |
| procedures | [UsageSlice.Procedures](#atom-UsageSlice-Procedures) | repeated |  |
| fileName | [string](#string) |  | File name |
| lineNumber | [uint32](#uint32) |  | Line number |
| columnNumber | [uint32](#uint32) |  | Column number |





 


<a name="atom-CONTROL_STRUCTURE_TYPES"></a>

### CONTROL_STRUCTURE_TYPES
Enum representing the control structure types

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_CONTROL_STRUCTURE_TYPE | 0 |  |
| BREAK | 1 | Represents a break statement. Labeled breaks are expected to have a JUMP_LABEL node AST child with ORDER 1 |
| CONTINUE | 2 | Represents a continue statement. Labeled continues are expected to have a JUMP_LABEL node AST child with ORDER 1 |
| WHILE | 3 | Represents a while statement |
| DO | 4 | Represents a do statement |
| FOR | 5 | Represents a for statement |
| GOTO | 6 | Represents a goto statement |
| IF | 7 | Represents an if statement |
| ELSE | 8 | Represents an else statement |
| SWITCH | 9 | Represents a switch statement |
| TRY | 10 | Represents a try statement |
| THROW | 11 | Represents a throw statement |
| MATCH | 12 | Represents a match expression |
| YIELD | 13 | Represents a yield expression |



<a name="atom-CpgStruct-Edge-EdgeType"></a>

### CpgStruct.Edge.EdgeType
Edge type.

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_EDGE_TYPE | 0 |  |
| AST | 3 | This edge connects a parent node to its child in the syntax tree. |
| CALL | 6 | This edge connects call sites, i.e., nodes with the type `CALL`, to the method node that represent the method they invoke. The frontend MAY create `CALL` edges but is not required to do so. Instead, of the `METHOD_FULL_NAME` field of the `CALL` node is set correctly, `CALL` edges are created automatically as the CPG is first loaded. |
| REF | 10 | This edge indicates that the source node is an identifier that denotes access to the destination node. For example, an identifier may reference a local variable. |
| TAGGED_BY | 11 | Edges from nodes to the tags they are tagged by. |
| PARAMETER_LINK | 12 | This edge connects a method input parameter to the corresponding method output parameter. |
| CFG | 19 | This edge indicates control flow from the source to the destination node. |
| EVAL_TYPE | 21 | This edge connects a node to its evaluation type. |
| BINDS_TO | 22 | This edge connects type arguments to type parameters to indicate that the type argument is used to instantiate the type parameter. |
| INHERITS_FROM | 23 | Inheritance relation between a type declaration and a type. This edge MUST NOT be created by the language frontend as it is automatically created from `INHERITS_FROM_TYPE_FULL_NAME` fields then the CPG is first loaded. |
| CONTAINS | 28 | This edge connects a node to the method that contains it. |
| CAPTURE | 40 | Represents the capturing of a variable into a closure |
| CAPTURED_BY | 41 | Connection between a captured LOCAL and the corresponding CLOSURE_BINDING |
| RECEIVER | 55 | Similar to `ARGUMENT` edges, `RECEIVER` edges connect call sites to their receiver arguments. A receiver argument is the object on which a method operates, that is, it is the expression that is assigned to the `this` pointer as control is transferred to the method. |
| CONDITION | 56 | The edge connects control structure nodes to the expressions that holds their conditions. |
| REACHING_DEF | 137 | A reaching definition edge indicates that a variable produced at the source node reaches the destination node without being reassigned on the way. The `VARIABLE` property indicates which variable is propagated. |
| ALIAS_OF | 138 | This edge represents an alias relation between a type declaration and a type. The language frontend MUST NOT create `ALIAS_OF` edges as they are created automatically based on `ALIAS_TYPE_FULL_NAME` fields when the CPG is first loaded. |
| BINDS | 155 | This edge connects a type declaration (`TYPE_DECL`) with a binding node (`BINDING`) and indicates that the type declaration has the binding represented by the binding node, in other words, there is a (name, signature) pair that can be resolved for the type declaration as stored in the binding node. |
| ARGUMENT | 156 | Argument edges connect call sites (node type `CALL`) to their arguments (node type `EXPRESSION`) as well as `RETURN` nodes to the expressions that return. |
| SOURCE_FILE | 157 | This edge connects a node to the node that represents its source file. These edges MUST not be created by the language frontend but are automatically created based on `FILENAME` fields. |
| DOMINATE | 181 | This edge indicates that the source node immediately dominates the destination node. |
| POST_DOMINATE | 182 | This edge indicates that the source node immediately post dominates the destination node. |
| CDG | 183 | A CDG edge expresses that the destination node is control dependent on the source node. |
| IMPORTS | 23663 | Edge from imports to dependencies |
| IS_CALL_FOR_IMPORT | 23664 | Edge from CALL statement in the AST to the IMPORT. We use this edge to traverse from the logical representation of the IMPORT to the corresponding import statement in the AST. |



<a name="atom-DispatchTypes"></a>

### DispatchTypes
Enum representing the dispatch types

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_DISPATCH_TYPE | 0 |  |
| STATIC_DISPATCH | 1 | For statically dispatched calls the call target is known before program execution |
| DYNAMIC_DISPATCH | 2 | For dynamically dispatched calls the target is determined during runtime |
| INLINED | 3 | For macro expansions, code is inlined. |



<a name="atom-EdgePropertyName"></a>

### EdgePropertyName
Enum for the name of an edge

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_EDGE_PROPERTY | 0 |  |
| VARIABLE | 11 | This edge property represents the variable propagated by a reaching definition edge. |



<a name="atom-EvaluationStrategies"></a>

### EvaluationStrategies
Enum representing the evaluation strategy of the underlying parameter or method or literal.

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_EVALUATION_STRATEGY | 0 |  |
| BY_REFERENCE | 1 | A parameter or return of a function is passed by reference which means an address is used behind the scenes |
| BY_SHARING | 2 | Only applicable to object parameter or return values. The pointer to the object is passed by value but the object itself is not copied and changes to it are thus propagated out of the method context |
| BY_VALUE | 3 | A parameter or return of a function passed by value which means a flat copy is used |



<a name="atom-LANGUAGES"></a>

### LANGUAGES
Enum to represent the frontend language

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_LANGUAGE | 0 | Unknown language |
| JAVA | 1 | Java |
| JAVASCRIPT | 2 | JavaScript and TypeScript |
| GOLANG | 3 | Go |
| CSHARP | 4 | csharp / dotnet |
| C | 5 | C/C&#43;&#43; |
| PYTHON | 6 | Python |
| LLVM | 7 | llvm |
| PHP | 8 | PHP |
| FUZZY_TEST_LANG | 9 | Test |
| GHIDRA | 10 | generic reverse engineering framework |
| KOTLIN | 11 | Kotlin |
| NEWC | 12 | Eclipse CDT based parser for C/C&#43;&#43; |
| JAVASRC | 13 | Source-based front-end for Java |
| PYTHONSRC | 14 | Source-based front-end for Python |
| JSSRC | 15 | Source-based JS frontend based on Babel |
| SOLIDITY | 16 | Solidity language frontend |
| RUBYSRC | 17 | Source-based frontend for Ruby |



<a name="atom-ModifierTypes"></a>

### ModifierTypes
Enum for the possible modifier types for symbols, methods and class nodes

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_MODIFIER_TYPE | 0 |  |
| STATIC | 1 | The static modifier |
| PUBLIC | 2 | The public modifier |
| PROTECTED | 3 | The protected modifier |
| PRIVATE | 4 | The private modifier |
| ABSTRACT | 5 | The abstract modifier |
| NATIVE | 6 | The native modifier |
| CONSTRUCTOR | 7 | The constructor modifier |
| VIRTUAL | 8 | The virtual modifier |
| INTERNAL | 9 | The internal modifier |
| FINAL | 10 | The final modifier |
| READONLY | 11 | The readonly modifier |



<a name="atom-NodePropertyName"></a>

### NodePropertyName
Enum for the name of a node property

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_NODE_PROPERTY | 0 |  |
| LINE_NUMBER | 2 | This optional field provides the line number of the program construct represented by the node. |
| PARSER_TYPE_NAME | 3 | AST node type name emitted by parser. |
| ORDER | 4 | This integer indicates the position of the node among its siblings in the AST. The left-most child has an order of 0. |
| NAME | 5 | Name of represented object, e.g., method name (e.g. &#34;run&#34;) |
| FULL_NAME | 6 | This is the fully-qualified name of an entity, e.g., the fully-qualified name of a method or type. The details of what constitutes a fully-qualified name are language specific. This field SHOULD be human readable. |
| IS_EXTERNAL | 7 | Indicates that the construct (METHOD or TYPE_DECL) is external, that is, it is referenced but not defined in the code (applies both to insular parsing and to library functions where we have header files only) |
| VALUE | 8 | This property denotes a string value as used in a key-value pair. |
| COLUMN_NUMBER | 11 | This optional fields provides the column number of the program construct represented by the node. |
| LINE_NUMBER_END | 12 | This optional fields provides the line number at which the program construct represented by the node ends. |
| VERSION | 13 | A version, given as a string. Used, for example, in the META_DATA node to indicate which version of the CPG spec this CPG conforms to. |
| EVALUATION_STRATEGY | 15 | For formal method input parameters, output parameters, and return parameters, this field holds the evaluation strategy, which is one of the following: 1) `BY_REFERENCE` indicates that the parameter is passed by reference, 2) `BY_VALUE` indicates that it is passed by value, that is, a copy is made, 3) `BY_SHARING` the parameter is a pointer/reference and it is shared with the caller/callee. While a copy of the pointer is made, a copy of the object that it points to is not made. |
| COLUMN_NUMBER_END | 16 | This optional fields provides the column number at which the program construct represented by the node ends. |
| LANGUAGE | 19 | This field indicates which CPG language frontend generated the CPG. Frontend developers may freely choose a value that describes their frontend so long as it is not used by an existing frontend. Reserved values are to date: C, LLVM, GHIDRA, PHP. |
| CONTENT | 20 | Certain files, e.g., configuration files, may be included in the CPG as-is. For such files, the `CONTENT` field contains the files content. |
| CODE | 21 | This field holds the code snippet that the node represents. |
| SIGNATURE | 22 | The method signature encodes the types of parameters in a string. The string SHOULD be human readable and suitable for differentiating methods with different parameter types sufficiently to allow for resolving of function overloading. The present specification does not enforce a strict format for the signature, that is, it can be chosen by the frontend implementor to fit the source language. |
| DISPATCH_TYPE | 25 | This field holds the dispatch type of a call, which is either `STATIC_DISPATCH` or `DYNAMIC_DISPATCH`. For statically dispatched method calls, the call target is known at compile time while for dynamically dispatched calls, it can only be determined at runtime as it may depend on the type of an object (as is the case for virtual method calls) or calculation of an offset. |
| MODIFIER_TYPE | 26 | The modifier type is a free-form string. The following are known modifier types: `STATIC`, `PUBLIC`, `PROTECTED`, `PRIVATE`, `ABSTRACT`, `NATIVE`, `CONSTRUCTOR`, `VIRTUAL`. |
| CONTROL_STRUCTURE_TYPE | 27 | The `CONTROL_STRUCTURE_TYPE` field indicates which kind of control structure a `CONTROL_STRUCTURE` node represents. The available types are the following: BREAK, CONTINUE, DO, WHILE, FOR, GOTO, IF, ELSE, TRY, THROW and SWITCH. |
| ARGUMENT_INDEX | 40 | AST-children of CALL nodes have an argument index, that is used to match call-site arguments with callee parameters. Explicit parameters are numbered from 1 to N, while index 0 is reserved for implicit self / this parameter. CALLs without implicit parameter therefore have arguments starting with index 1. AST-children of BLOCK nodes may have an argument index as well; in this case, the last argument index determines the return expression of a BLOCK expression. If the `PARAMETER_NAME` field is set, then the `ARGUMENT_INDEX` field is ignored. It is suggested to set it to -1. |
| CLOSURE_BINDING_ID | 50 | Identifier which uniquely describes a CLOSURE_BINDING. This property is used to match captured LOCAL nodes with the corresponding CLOSURE_BINDING nodes. |
| TYPE_FULL_NAME | 51 | This field contains the fully-qualified static type name of the program construct represented by a node. It is the name of an instantiated type, e.g., `java.util.List&lt;Integer&gt;`, rather than `java.util.List[T]`. If the type cannot be determined, this field should be set to the empty string. |
| TYPE_DECL_FULL_NAME | 52 | The static type decl of a TYPE. This property is matched against the FULL_NAME of TYPE_DECL nodes. It is required to have exactly one TYPE_DECL for each different TYPE_DECL_FULL_NAME. |
| INHERITS_FROM_TYPE_FULL_NAME | 53 | The static types a TYPE_DECL inherits from. This property is matched against the FULL_NAME of TYPE nodes and thus it is required to have at least one TYPE node for each TYPE_FULL_NAME. |
| METHOD_FULL_NAME | 54 | The FULL_NAME of a method. Used to link CALL and METHOD nodes. It is required to have exactly one METHOD node for each METHOD_FULL_NAME. |
| AST_PARENT_TYPE | 56 | The type of the AST parent. Since this is only used in some parts of the graph, the list does not include all possible parents by intention. Possible parents: METHOD, TYPE_DECL, NAMESPACE_BLOCK. |
| AST_PARENT_FULL_NAME | 57 | This field holds the FULL_NAME of the AST parent of an entity. |
| DEPENDENCY_GROUP_ID | 58 | The group ID for a dependency |
| SYMBOL | 100 | Symbols |
| METHOD_SHORT_NAME | 102 | Method short name. |
| PACKAGE_NAME | 103 | Method package name. |
| CLASS_NAME | 104 | Method class name. |
| NODE_LABEL | 105 | Label for the node which could be code. |
| FILENAME | 106 | The path of the source file this node was generated from, relative to the root path in the meta data node. This field must be set but may be set to the value `&lt;unknown&gt;` to indicate that no source file can be associated with the node, e.g., because the node represents an entity known to exist because it is referenced, but for which the file that is is declared in is unknown. |
| OVERLAYS | 118 | The field contains the names of the overlays applied to this CPG, in order of their application. Names are free-form strings, that is, this specification does not dictate them but rather requires tool producers and consumers to communicate them between each other. |
| HASH | 120 | This property contains a hash value in the form of a string. Hashes can be used to summarize data, e.g., to summarize the contents of source files or sub graphs. Such summaries are useful to determine whether code has already been analyzed in incremental analysis pipelines. This property is optional to allow its calculation to be deferred or skipped if the hash is not needed. |
| ARGUMENT_NAME | 130 | For calls involving named parameters, the `ARGUMENT_NAME` field holds the name of the parameter initialized by the expression. For all other calls, this field is unset. |
| KEY | 131 | This property denotes a key of a key-value pair. |
| CLASS_SHORT_NAME | 132 | Class short name |
| ALIAS_TYPE_FULL_NAME | 158 | This property holds the fully qualified name of the type that the node is a type alias of. |
| CLOSURE_ORIGINAL_NAME | 159 | The original name of the (potentially mangled) captured variable |
| IS_VARIADIC | 221 | Specifies whether a parameter is the variadic argument handling parameter of a variadic method. Only one parameter of a method is allowed to have this property set to true. |
| ROOT | 1199 | The path to the root directory of the source/binary this CPG is generated from. |
| DYNAMIC_TYPE_HINT_FULL_NAME | 1591 | Type hint for the dynamic type. |
| INDEX | 2223 | Specifies an index, e.g., for a parameter or argument. Explicit parameters are numbered from 1 to N, while index 0 is reserved for implicit self / this parameter. |
| CANONICAL_NAME | 2001092 | This field holds the canonical name of a `FIELD_IDENTIFIER`. It is typically identical to the CODE field, but canonicalized according to source language semantics. Human readable names are preferable. `FIELD_IDENTIFIER` nodes must share identical `CANONICAL_NAME` if and only if they alias, e.g., in C-style unions (if the aliasing relationship is unknown or there are partial overlaps, then one must make a reasonable guess, and trade off between false negatives and false positives). |
| CONTAINED_REF | 2007161 | References to other nodes. This is not a real property; it exists here for the sake of proto serialization only. valueType and cardinality are meaningless. |



<a name="atom-NodeType"></a>

### NodeType
Programming languages offer many closely-related concepts for describing blocks of code that can be executed with input parameters and return output parameters, possibly causing side effects. In the CPG specification, we refer to all of these concepts (procedures, functions, methods, etc.) as methods. A single METHOD node must exist for each method found in the source program.
The `FULL_NAME` field specifies the method&#39;s fully-qualified name, including information about the namespace it is contained in if applicable, the name field is the function&#39;s short name. The field `IS_EXTERNAL` indicates whether it was possible to identify a method body for the method. This is true for methods that are defined in the source program, and false for methods that are dynamically linked to the program, that is, methods that exist in an external dependency.
Line and column number information is specified in the optional fields `LINE_NUMBER`, `COLUMN_NUMBER`, `LINE_NUMBER_END`, and `COLUMN_NUMBER_END` and the name of the source file is specified in `FILENAME`. An optional hash value MAY be calculated over the function contents and included in the `HASH` field.
Finally, the fully qualified name of the program constructs that the method is immediately contained in is stored in the `AST_PARENT_FULL_NAME` field and its type is indicated in the `AST_PARENT_TYPE` field to be one of `METHOD`, `TYPE_DECL` or `NAMESPACE_BLOCK`.

| Name | Number | Description |
| ---- | ------ | ----------- |
| UNKNOWN_NODE_TYPE | 0 |  |
| METHOD | 1 | Method Nodes |
| METHOD_RETURN | 3 | This node represents an (unnamed) formal method return parameter. It carries its fully qualified type name in `TYPE_FULL_NAME`. The `CODE` field MAY be set freely, e.g., to the constant `RET`, however, subsequent layer creators MUST NOT depend on this value. |
| ANNOTATION | 5 | A method annotation. The semantics of the FULL_NAME property on this node differ from the usual FULL_NAME semantics in the sense that FULL_NAME describes the represented annotation class/interface itself and not the ANNOTATION node. |
| ANNOTATION_PARAMETER_ASSIGN | 6 | Assignment of annotation argument to annotation parameter |
| ANNOTATION_PARAMETER | 7 | Formal annotation parameter |
| LITERAL | 8 | This node represents a literal such as an integer or string constant. Literals are symbols included in the code in verbatim form and which are immutable. The `TYPE_FULL_NAME` field stores the literal&#39;s fully-qualified type name, e.g., `java.lang.Integer`. |
| MEMBER | 9 | This node represents a type member of a class, struct or union, e.g., for the type declaration `class Foo{ int i ; }`, it represents the declaration of the variable `i`. |
| ARRAY_INITIALIZER | 14 | Initialization construct for arrays |
| CALL | 15 | A (function/method/procedure) call. The `METHOD_FULL_NAME` property is the name of the invoked method (the callee) while the `TYPE_FULL_NAME` is its return type, and therefore, the return type of the call when viewing it as an expression. For languages like Javascript, it is common that we may know the (short-) name of the invoked method, but we do not know at compile time which method will actually be invoked, e.g., because it depends on a dynamic import. In this case, we leave `METHOD_FULL_NAME` blank but at least fill out `NAME`, which contains the method&#39;s (short-) name and `SIGNATURE`, which contains any information we may have about the types of arguments and return value. |
| LOCAL | 23 | This node represents a local variable. Its fully qualified type name is stored in the `TYPE_FULL_NAME` field and its name in the `NAME` field. The `CODE` field contains the entire local variable declaration without initialization, e.g., for `int x = 10;`, it contains `int x`. |
| TAG | 24 | This node represents a tag. |
| LOCATION | 25 | A location node summarizes a source code location. |
| IDENTIFIER | 27 | This node represents an identifier as used when referring to a variable by name. It holds the identifier&#39;s name in the `NAME` field and its fully-qualified type name in `TYPE_FULL_NAME`. |
| RETURN | 30 | This node represents a return instruction, e.g., `return x`. Note that it does NOT represent a formal return parameter as formal return parameters are represented via `METHOD_RETURN` nodes. |
| BLOCK | 31 | This node represents a compound statement. Compound statements are used in many languages to allow grouping a sequence of statements. For example, in C and Java, compound statements are statements enclosed by curly braces. Function/Method bodies are compound statements. We do not use the term &#34;compound statement&#34; because &#34;statement&#34; would imply that the block does not yield a value upon evaluation, that is, that it is not an expression. This is true in languages such as C and Java, but not for languages such as Scala where the value of the block is given by that of the last expression it contains. In fact, the Scala grammar uses the term &#34;BlockExpr&#34; (short for &#34;block expression&#34;) to describe what in the CPG we call &#34;Block&#34;. |
| METHOD_PARAMETER_OUT | 33 | This node represents a formal output parameter. Corresponding output parameters for input parameters MUST NOT be created by the frontend as they are automatically created upon first loading the CPG. |
| METHOD_PARAMETER_IN | 34 | This node represents a formal input parameter. The field `NAME` contains its name, while the field `TYPE_FULL_NAME` contains the fully qualified type name. |
| DEPENDENCY | 35 | This node represents a dependency |
| FILE | 38 | File nodes represent source files or a shared objects from which the CPG was generated. File nodes serve as indices, that is, they allow looking up all elements of the code by file. For each file, the graph MUST contain exactly one File node. As file nodes are root nodes of abstract syntax tress, they are AstNodes and their order field is set to 0. This is because they have no sibling nodes, not because they are the first node of the AST. Each CPG MUST contain a special file node with name set to `&lt;unknown&gt;`. This node is a placeholder used in cases where a file cannot be determined at compile time. As an example, consider external library functions. As their code is not available on CPG construction, the file name is unknown. File nodes MUST NOT be created by the language frontend. Instead, the language frontend is assumed to fill out the `FILENAME` field wherever possible, allowing File nodes to be created automatically upon first loading the CPG. |
| META_DATA | 39 | This node contains the CPG meta data. Exactly one node of this type MUST exist per CPG. The `HASH` property MAY contain a hash value calculated over the source files this CPG was generated from. The `VERSION` MUST be set to the version of the specification (&#34;1.1&#34;). The language field indicates which language frontend was used to generate the CPG and the list property `OVERLAYS` specifies which overlays have been applied to the CPG. |
| NAMESPACE | 40 | This node represents a namespace. Similar to FILE nodes, NAMESPACE nodes serve as indices that allow all definitions inside a namespace to be obtained by following outgoing edges from a NAMESPACE node. NAMESPACE nodes MUST NOT be created by language frontends. Instead, they are generated from NAMESPACE_BLOCK nodes automatically upon first loading of the CPG. |
| NAMESPACE_BLOCK | 41 | A reference to a namespace. We borrow the concept of a &#34;namespace block&#34; from C&#43;&#43;, that is, a namespace block is a block of code that has been placed in the same namespace by a programmer. This block may be introduced via a `package` statement in Java or a `namespace{ }` statement in C&#43;&#43;. The `FULL_NAME` field contains a unique identifier to represent the namespace block itself not just the namespace it references. So in addition to the namespace name it can be useful to use the containing file name to derive a unique identifier.

The `NAME` field contains the namespace name in a human-readable format. The name should be given in dot-separated form where a dot indicates that the right hand side is a sub namespace of the left hand side, e.g., `foo.bar` denotes the namespace `bar` contained in the namespace `foo`. |
| UNKNOWN | 44 | Any AST node that the frontend would like to include in the AST but for which no suitable AST node is specified in the CPG specification may be included using a node of type `UNKNOWN`. |
| TYPE | 45 | This node represents a type instance, that is, a concrete instantiation of a type declaration. |
| TYPE_DECL | 46 | This node represents a type declaration as for example given by a class-, struct-, or union declaration. In contrast to a `TYPE` node, this node does not represent a concrete instantiation of a type, e.g., for the parametrized type `List[T]`, it represents `List[T]`, but not `List[Integer]` where `Integer` is a concrete type. The language frontend MUST create type declarations for all types declared in the source program and MAY provide type declarations for types that are not declared but referenced by the source program. If a declaration is present in the source program, the field `IS_EXTERNAL` is set to `false`. Otherwise, it is set to `true`. The `FULL_NAME` field specifies the type&#39;s fully-qualified name, including information about the namespace it is contained in if applicable, the name field is the type&#39;s short name. Line and column number information is specified in the optional fields `LINE_NUMBER`, `COLUMN_NUMBER`, `LINE_NUMBER_END`, and `COLUMN_NUMBER_END` and the name of the source file is specified in `FILENAME`. Base types can be specified via the `INHERITS_FROM_TYPE_FULL_NAME` list, where each entry contains the fully-qualified name of a base type. If the type is known to be an alias of another type (as for example introduced via the C `typedef` statement), the name of the alias is stored in `ALIAS_TYPE_FULL_NAME`. Finally, the fully qualified name of the program constructs that the type declaration is immediately contained in is stored in the `AST_PARENT_FULL_NAME` field and its type is indicated in the `AST_PARENT_TYPE` field to be one of `METHOD`, `TYPE_DECL` or `NAMESPACE_BLOCK`. |
| TYPE_PARAMETER | 47 | This node represents a formal type parameter, that is, the type parameter as given in a type-parametrized method or type declaration. Examples for languages that support type parameters are Java (via Generics) and C&#43;&#43; (via templates). Apart from the standard fields of AST nodes, the type parameter carries only a `NAME` field that holds the parameters name. |
| TYPE_ARGUMENT | 48 | An (actual) type argument as used to instantiate a parametrized type, in the same way an (actual) arguments provides concrete values for a parameter at method call sites. As it true for arguments, the method is not expected to interpret the type argument. It MUST however store its code in the `CODE` field. |
| ANNOTATION_LITERAL | 49 | A literal value assigned to an ANNOTATION_PARAMETER |
| CONFIG_FILE | 50 | This node type represent a configuration file, where `NAME` is the name of the file and `content` is its content. The exact representation of the name is left undefined and can be chosen as required by consumers of the corresponding configuration files. |
| BINDING | 146 | `BINDING` nodes represent name-signature pairs that can be resolved at a type declaration (`TYPE_DECL`). They are connected to `TYPE_DECL` nodes via incoming `BINDS` edges. The bound method is either associated with an outgoing `REF` edge to a `METHOD` or with the `METHOD_FULL_NAME` property. The `REF` edge if present has priority. |
| TAG_NODE_PAIR | 208 | This node contains an arbitrary node and an associated tag node. |
| FINDING | 214 | Finding nodes may be used to store analysis results in the graph that are to be exposed to an end-user, e.g., information about potential vulnerabilities or dangerous programming practices. A Finding node may contain an abitrary list of key value pairs that characterize the finding, as well as a list of nodes that serve as evidence for the finding. |
| KEY_VALUE_PAIR | 217 | This node represents a key value pair, where both the key and the value are strings. |
| MODIFIER | 300 | This field represents a (language-dependent) modifier such as `static`, `private` or `public`. Unlike most other AST nodes, it is NOT an expression, that is, it cannot be evaluated and cannot be passed as an argument in function calls. |
| METHOD_REF | 333 | This node represents a reference to a method/function/procedure as it appears when a method is passed as an argument in a call. The `METHOD_FULL_NAME` field holds the fully-qualified name of the referenced method and the `TYPE_FULL_NAME` holds its fully-qualified type name. |
| CLOSURE_BINDING | 334 | Represents the binding of a LOCAL or METHOD_PARAMETER_IN into the closure of a method |
| TYPE_REF | 335 | Reference to a type/class |
| CONTROL_STRUCTURE | 339 | In addition to the `CONTROL_STRUCTURE_TYPE` field, the `PARSER_TYPE_NAME` field MAY be used by frontends to store the name of the control structure as emitted by the parser or disassembler, however, the value of this field is not relevant for construction of the control flow layer. |
| JUMP_TARGET | 340 | A jump target is any location in the code that has been specifically marked as the target of a jump, e.g., via a label. The `NAME` field holds the name of the label while the `PARSER_TYPE_NAME` field holds the name of language construct that this jump target is created from, e.g., &#34;Label&#34;. |
| JUMP_LABEL | 341 | A jump label specifies the label and thus the JUMP_TARGET of control structures BREAK and CONTINUE. The `NAME` field holds the name of the label while the `PARSER_TYPE_NAME` field holds the name of language construct that this jump label is created from, e.g., &#34;Label&#34;. |
| TEMPLATE_DOM | 417 | This node represents a DOM node used in template languages, e.g., JSX/TSX |
| COMMENT | 511 | A source code comment |
| FIELD_IDENTIFIER | 2001081 | This node represents the field accessed in a field access, e.g., in `a.b`, it represents `b`. The field name as it occurs in the code is stored in the `CODE` field. This may mean that the `CODE` field holds an expression. The `CANONICAL_NAME` field MAY contain the same value is the `CODE` field but SHOULD contain the normalized name that results from evaluating `CODE` as an expression if such an evaluation is possible for the language frontend. The objective is to store an identifier in `CANONICAL_NAME` that is the same for two nodes iff they refer to the same field, regardless of whether they use the same expression to reference it. |



<a name="atom-UsageSlice-LabelType"></a>

### UsageSlice.LabelType
Label type.

| Name | Number | Description |
| ---- | ------ | ----------- |
| ANY | 0 | Any is used to represent multiple mechanisms |
| LOCAL | 1 | Represents a local transfer of data via aliasing. The data defined is via some alias. |
| LITERAL | 2 | Represents a literal. |
| PARAM | 3 | Represents data introduced via a parameter. |
| CALL | 4 | Represents data introduced by the return value of a call. |
| IDENTIFIER | 5 | Identifier |
| TYPE_REF | 6 | Type ref |
| UNKNOWN | 10 | Represents data introduced by an unhandled data structure. |


 

 

 



## Scalar Value Types

| .proto Type | Notes | C++ | Java | Python | Go | C# | PHP | Ruby |
| ----------- | ----- | --- | ---- | ------ | -- | -- | --- | ---- |
| <a name="double" /> double |  | double | double | float | float64 | double | float | Float |
| <a name="float" /> float |  | float | float | float | float32 | float | float | Float |
| <a name="int32" /> int32 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint32 instead. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="int64" /> int64 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint64 instead. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="uint32" /> uint32 | Uses variable-length encoding. | uint32 | int | int/long | uint32 | uint | integer | Bignum or Fixnum (as required) |
| <a name="uint64" /> uint64 | Uses variable-length encoding. | uint64 | long | int/long | uint64 | ulong | integer/string | Bignum or Fixnum (as required) |
| <a name="sint32" /> sint32 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int32s. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="sint64" /> sint64 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int64s. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="fixed32" /> fixed32 | Always four bytes. More efficient than uint32 if values are often greater than 2^28. | uint32 | int | int | uint32 | uint | integer | Bignum or Fixnum (as required) |
| <a name="fixed64" /> fixed64 | Always eight bytes. More efficient than uint64 if values are often greater than 2^56. | uint64 | long | int/long | uint64 | ulong | integer/string | Bignum |
| <a name="sfixed32" /> sfixed32 | Always four bytes. | int32 | int | int | int32 | int | integer | Bignum or Fixnum (as required) |
| <a name="sfixed64" /> sfixed64 | Always eight bytes. | int64 | long | int/long | int64 | long | integer/string | Bignum |
| <a name="bool" /> bool |  | bool | boolean | boolean | bool | bool | boolean | TrueClass/FalseClass |
| <a name="string" /> string | A string must always contain UTF-8 encoded or 7-bit ASCII text. | string | String | str/unicode | string | string | string | String (UTF-8) |
| <a name="bytes" /> bytes | May contain any arbitrary sequence of bytes. | string | ByteString | str | []byte | ByteString | string | String (ASCII-8BIT) |

