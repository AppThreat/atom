# JSON Schema

## Definitions

- **`PropertyValue`** *(object)*: Cannot contain additional properties.
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
- **`atom.BoolList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(boolean)*
- **`atom.ContainedRefs`** *(object)*: Cannot contain additional properties.
  - **`local_name`** *(string)*
  - **`refs`** *(array)*
    - **Items** *(string)*
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
- **`atom.StringList`** *(object)*: Cannot contain additional properties.
  - **`values`** *(array)*
    - **Items** *(string)*
