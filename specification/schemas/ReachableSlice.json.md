# JSON Schema

## Definitions

- <a id="definitions/ReachableSlice"></a>**`ReachableSlice`** *(object)*: * Reachables slices offers a list of reachable nodes based on automated tags exported from data dependency graph. Cannot contain additional properties.
  - <a id="definitions/ReachableSlice/properties/flows"></a>**`flows`** *(array)*: Cannot contain additional properties.
    - <a id="definitions/ReachableSlice/properties/flows/items"></a>**Items**: Refer to *[#/definitions/atom.Nodes](#definitions/atom.Nodes)*.
  - <a id="definitions/ReachableSlice/properties/purls"></a>**`purls`** *(array)*
    - <a id="definitions/ReachableSlice/properties/purls/items"></a>**Items** *(string)*
- <a id="definitions/atom.Nodes"></a>**`atom.Nodes`** *(object)*: Cannot contain additional properties.
  - <a id="definitions/atom.Nodes/properties/id"></a>**`id`** *(integer)*: Id of the node.
  - <a id="definitions/atom.Nodes/properties/label"></a>**`label`**: * Programming languages offer many closely-related concepts for describing blocks of code that can be executed with input parameters and return output parameters, possibly causing side effects. In the CPG specification, we refer to all of these concepts (procedures, functions, methods, etc.) as methods. A single METHOD node must exist for each method found in the source program. The `FULL_NAME` field specifies the method's fully-qualified name, including information about the namespace it is contained in if applicable, the name field is the function's short name. The field `IS_EXTERNAL` indicates whether it was possible to identify a method body for the method. This is true for methods that are defined in the source program, and false for methods that are dynamically linked to the program, that is, methods that exist in an external dependency. Line and column number information is specified in the optional fields `LINE_NUMBER`, `COLUMN_NUMBER`, `LINE_NUMBER_END`, and `COLUMN_NUMBER_END` and the name of the source file is specified in `FILENAME`. An optional hash value MAY be calculated over the function contents and included in the `HASH` field. Finally, the fully qualified name of the program constructs that the method is immediately contained in is stored in the `AST_PARENT_FULL_NAME` field and its type is indicated in the `AST_PARENT_TYPE` field to be one of `METHOD`, `TYPE_DECL` or `NAMESPACE_BLOCK`. Must be one of: "UNKNOWN_NODE_TYPE", 0, "METHOD", 1, "METHOD_RETURN", 3, "ANNOTATION", 5, "ANNOTATION_PARAMETER_ASSIGN", 6, "ANNOTATION_PARAMETER", 7, "LITERAL", 8, "MEMBER", 9, "ARRAY_INITIALIZER", 14, "CALL", 15, "LOCAL", 23, "TAG", 24, "LOCATION", 25, "IDENTIFIER", 27, "RETURN", 30, "BLOCK", 31, "METHOD_PARAMETER_OUT", 33, "METHOD_PARAMETER_IN", 34, "DEPENDENCY", 35, "FILE", 38, "META_DATA", 39, "NAMESPACE", 40, "NAMESPACE_BLOCK", 41, "UNKNOWN", 44, "TYPE", 45, "TYPE_DECL", 46, "TYPE_PARAMETER", 47, "TYPE_ARGUMENT", 48, "ANNOTATION_LITERAL", 49, "CONFIG_FILE", 50, "BINDING", 146, "TAG_NODE_PAIR", 208, "FINDING", 214, "KEY_VALUE_PAIR", 217, "MODIFIER", 300, "METHOD_REF", 333, "CLOSURE_BINDING", 334, "TYPE_REF", 335, "CONTROL_STRUCTURE", 339, "JUMP_TARGET", 340, "JUMP_LABEL", 341, "TEMPLATE_DOM", 417, "COMMENT", 511, "FIELD_IDENTIFIER", or 2001081.
    - **One of**
      - <a id="definitions/atom.Nodes/properties/label/oneOf/0"></a>*string*
      - <a id="definitions/atom.Nodes/properties/label/oneOf/1"></a>*integer*
  - <a id="definitions/atom.Nodes/properties/name"></a>**`name`** *(string)*: Name of the call or identifier or parameter.
  - <a id="definitions/atom.Nodes/properties/fullName"></a>**`fullName`** *(string)*: Full name of the call.
  - <a id="definitions/atom.Nodes/properties/signature"></a>**`signature`** *(string)*: Method signature of the call.
  - <a id="definitions/atom.Nodes/properties/isExternal"></a>**`isExternal`** *(boolean)*: Boolean to indicate if this call belongs to an external method.
  - <a id="definitions/atom.Nodes/properties/code"></a>**`code`** *(string)*: Source code.
  - <a id="definitions/atom.Nodes/properties/typeFullName"></a>**`typeFullName`** *(string)*: Type full name.
  - <a id="definitions/atom.Nodes/properties/parentMethodName"></a>**`parentMethodName`** *(string)*: Parent method name.
  - <a id="definitions/atom.Nodes/properties/parentMethodSignature"></a>**`parentMethodSignature`** *(string)*: Parent method signature.
  - <a id="definitions/atom.Nodes/properties/parentFileName"></a>**`parentFileName`** *(string)*: Parent filename.
  - <a id="definitions/atom.Nodes/properties/parentPackageName"></a>**`parentPackageName`** *(string)*: Parent package name.
  - <a id="definitions/atom.Nodes/properties/parentClassName"></a>**`parentClassName`** *(string)*: Parent class name.
  - <a id="definitions/atom.Nodes/properties/lineNumber"></a>**`lineNumber`** *(integer)*: Line number.
  - <a id="definitions/atom.Nodes/properties/columnNumber"></a>**`columnNumber`** *(integer)*: Column number.
  - <a id="definitions/atom.Nodes/properties/tags"></a>**`tags`** *(string)*: Tags. Can contain simple names including package url.
