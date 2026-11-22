# JSON Schema

The schema of one finding of atom's `memory-safety` command. Unlike the other schemas in this
directory, which are generated from `atom.proto`, this output is circe-encoded, so this schema
is written by hand. The command's json format writes a JSON array of these objects; the sarif
format writes the same facts as a SARIF 2.1.0 log. This file documents the schema; the recipe
below is normative for the `fingerprint` field and is implemented once, in
`MemorySafetyCommands`.

## Definitions

- <a id="definitions/MemorySafetyFinding"></a>**`MemorySafetyFinding`** *(object)*: One finding
  of the memory-safety overlay. Cannot contain additional properties.
  - <a id="definitions/MemorySafetyFinding/properties/rule"></a>**`rule`** *(string)*: The rule
    id, e.g. `MS-BOUND-001` or `MS-ALLOC-003`.
  - <a id="definitions/MemorySafetyFinding/properties/cwe"></a>**`cwe`** *(string)*: The CWE the
    rule maps to, e.g. `CWE-787`.
  - <a id="definitions/MemorySafetyFinding/properties/kind"></a>**`kind`** *(string)*: The
    rule's kind, e.g. `size-param-contract-violation`.
  - <a id="definitions/MemorySafetyFinding/properties/file"></a>**`file`** *(string)*: The
    finding's file: the analysed input's name in front of a path inside it, or an absolute path
    for a file outside it.
  - <a id="definitions/MemorySafetyFinding/properties/line"></a>**`line`** *(integer)*: The
    finding's line, 1-based; 0 when the node carries no position.
  - <a id="definitions/MemorySafetyFinding/properties/column"></a>**`column`** *(integer)*: The
    finding's column, 1-based; 0 when the node carries no position.
  - <a id="definitions/MemorySafetyFinding/properties/severity"></a>**`severity`** *(string)*:
    One of `critical`, `high`, `medium`, `low`.
  - <a id="definitions/MemorySafetyFinding/properties/confidence"></a>**`confidence`**
    *(string)*: One of `high`, `medium`, `low`.
  - <a id="definitions/MemorySafetyFinding/properties/message"></a>**`message`** *(string)*:
    The offending code and the rule's message, with the notes the rule adds.
  - <a id="definitions/MemorySafetyFinding/properties/function"></a>**`function`** *(string)*:
    The full name of the method the finding sits in; empty when there is none.
  - <a id="definitions/MemorySafetyFinding/properties/purl"></a>**`purl`** *(string)*: The
    package URL of the BOM component that owns the finding's file (`cdx:vendored:path`,
    `cdx:cmake:sourceDir`). Present only when a located component owns the file; a path named
    only by the `--external-paths` pattern has no purl, and the field is omitted then.
  - <a id="definitions/MemorySafetyFinding/properties/flow"></a>**`flow`** *(array)*: The
    evidence chain: the offending node, its memory operation, the origin, guard and extent
    facts the rule read, and the definitions that produced the value. Items are
    [`MemorySafetyFinding.FlowStep`](#definitions/MemorySafetyFinding.FlowStep).
  - <a id="definitions/MemorySafetyFinding/properties/fingerprint"></a>**`fingerprint`**
    *(string)*: SHA-256, as lowercase hex, over the UTF-8 bytes of the string

    ```
    rule + "|" + function + "|" + file + "|" + apis
    ```

    where `rule` is the finding's rule id; `function` and `file` are the rendered values of
    those two fields (`file` exactly as the `file` field prints it); and `apis` is the ordered
    distinct names of the finding's memory-operation calls, joined with `,` and empty when
    there are none: the finding node's own name when the node is a Call, plus the Call parent
    the node is an argument of, read from the graph nodes and never parsed from `code` text.
    The line and the column are not part of the recipe, so the fingerprint survives edits that
    move the finding within its function. In SARIF output the same value rides under
    `partialFingerprints` as `atom/memorySafety/v1`.
  - <a id="definitions/MemorySafetyFinding/properties/atom"></a>**`atom`** *(string)*: The path
    of the atom file the finding was rendered from.
  - <a id="definitions/MemorySafetyFinding/properties/instances"></a>**`instances`** *(array
    of strings)*: The `template-args` of each instantiation that shares this finding, present
    only on a finding of a template instance that other instances share.
- <a id="definitions/MemorySafetyFinding.FlowStep"></a>**`MemorySafetyFinding.FlowStep`**
  *(object)*: One row of the evidence chain. Cannot contain additional properties.
  - <a id="definitions/MemorySafetyFinding.FlowStep/properties/file"></a>**`file`**
    *(string)*: The step's file: relative to the analysed input without its name in front, or
    absolute for a file outside it.
  - <a id="definitions/MemorySafetyFinding.FlowStep/properties/line"></a>**`line`**
    *(integer)*: The step's line, 1-based.
  - <a id="definitions/MemorySafetyFinding.FlowStep/properties/code"></a>**`code`**
    *(string)*: The step's code, or the text of a fact the rule read. Required.
  - <a id="definitions/MemorySafetyFinding.FlowStep/properties/role"></a>**`role`**
    *(string)*: One of `length-argument`, `memory-operation`, `exit`, `origin`, `guard`,
    `destination`, `definition`. Required.
