# JSON Schema

## Definitions

- **`AdditionalNodeProperty`** *(object)*: Cannot contain additional properties.
  - **`node_id`** *(string)*
  - **`property`**: Cannot contain additional properties. Refer to *#/definitions/atom.CpgStruct.Node.Property*.
- **`atom.BoolList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(boolean)*
- **`atom.ContainedRefs`** *(object)*: Cannot contain additional properties.
  - **`local_name`** *(string)*
  - **`refs`** *(array)*
    - **Items** *(string)*
- **`atom.CpgStruct.Node.Property`** *(object)*: Node properties. Cannot contain additional properties.
  - **`name`**: Atom v1.0.0 specification is currently compatible with Joern CPG v1.1 specification with 1 modification - imports edges are not hidden in atom https://cpg.joern.io. Must be one of: `['UNKNOWN_NODE_PROPERTY', 0, 'LINE_NUMBER', 2, 'PARSER_TYPE_NAME', 3, 'ORDER', 4, 'NAME', 5, 'FULL_NAME', 6, 'IS_EXTERNAL', 7, 'VALUE', 8, 'COLUMN_NUMBER', 11, 'LINE_NUMBER_END', 12, 'VERSION', 13, 'EVALUATION_STRATEGY', 15, 'COLUMN_NUMBER_END', 16, 'LANGUAGE', 19, 'CONTENT', 20, 'CODE', 21, 'SIGNATURE', 22, 'DISPATCH_TYPE', 25, 'MODIFIER_TYPE', 26, 'CONTROL_STRUCTURE_TYPE', 27, 'ARGUMENT_INDEX', 40, 'CLOSURE_BINDING_ID', 50, 'TYPE_FULL_NAME', 51, 'TYPE_DECL_FULL_NAME', 52, 'INHERITS_FROM_TYPE_FULL_NAME', 53, 'METHOD_FULL_NAME', 54, 'AST_PARENT_TYPE', 56, 'AST_PARENT_FULL_NAME', 57, 'DEPENDENCY_GROUP_ID', 58, 'SYMBOL', 100, 'METHOD_SHORT_NAME', 102, 'PACKAGE_NAME', 103, 'CLASS_NAME', 104, 'NODE_LABEL', 105, 'FILENAME', 106, 'OVERLAYS', 118, 'HASH', 120, 'ARGUMENT_NAME', 130, 'KEY', 131, 'CLASS_SHORT_NAME', 132, 'ALIAS_TYPE_FULL_NAME', 158, 'CLOSURE_ORIGINAL_NAME', 159, 'IS_VARIADIC', 221, 'ROOT', 1199, 'DYNAMIC_TYPE_HINT_FULL_NAME', 1591, 'INDEX', 2223, 'CANONICAL_NAME', 2001092, 'CONTAINED_REF', 2007161]`.
  - **`value`**: Cannot contain additional properties. Refer to *#/definitions/atom.PropertyValue*.
- **`atom.DoubleList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(number)*
- **`atom.FloatList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(number)*
- **`atom.IntList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(integer)*
- **`atom.LongList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(string)*
- **`atom.PropertyValue`** *(object)*: Cannot contain additional properties.
  - **`string_value`** *(string)*
  - **`bool_value`** *(boolean)*
  - **`int_value`** *(integer)*
  - **`long_value`** *(string)*
  - **`float_value`** *(number)*
  - **`double_value`** *(number)*
  - **`string_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.StringList*.
  - **`bool_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.BoolList*.
  - **`int_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.IntList*.
  - **`long_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.LongList*.
  - **`float_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.FloatList*.
  - **`double_list`**: Cannot contain additional properties. Refer to *#/definitions/atom.DoubleList*.
  - **`contained_refs`**: Cannot contain additional properties. Refer to *#/definitions/atom.ContainedRefs*.
- **`atom.StringList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(string)*