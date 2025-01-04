---
title: Atom Proto Specification
---

```proto
syntax = "proto3";

/**
 * Atom is a novel intermediate representation for applications. The representation is optimized for operations such as slicing useful for application analytics.
 *
 * ![Atom logo](./docs/Atom-logo.png)
 * Atom v1.0.0 specification is currently compatible with Joern CPG v1.1 specification with 1 modification
 * - imports edges are not hidden in atom
 * [https://cpg.joern.io](https://cpg.joern.io)
 */
package atom;

option go_package = "github.com/AppThreat/atom";
option java_package = "io.appthreat.atom";
option java_outer_classname = "Atom";
option csharp_namespace = "io.appthreat.atom";

// Enum for the name of a node property
enum NodePropertyName {
  UNKNOWN_NODE_PROPERTY = 0;

  // This optional field provides the line number of the program construct represented by the node.
  LINE_NUMBER = 2;

  // AST node type name emitted by parser.
  PARSER_TYPE_NAME = 3;

  // This integer indicates the position of the node among its siblings in the AST. The left-most child has an order of 0.
  ORDER = 4;

  // Name of represented object, e.g., method name (e.g. "run")
  NAME = 5;

  // This is the fully-qualified name of an entity, e.g., the fully-qualified name of a method or type. The details of what constitutes a fully-qualified name are language specific. This field SHOULD be human readable.
  FULL_NAME = 6;

  // Indicates that the construct (METHOD or TYPE_DECL) is external, that is, it is referenced but not defined in the code (applies both to insular parsing and to library functions where we have header files only)
  IS_EXTERNAL = 7;

  // This property denotes a string value as used in a key-value pair.
  VALUE = 8;

  // This optional fields provides the column number of the program construct represented by the node.
  COLUMN_NUMBER = 11;

  // This optional fields provides the line number at which the program construct represented by the node ends.
  LINE_NUMBER_END = 12;

  // A version, given as a string. Used, for example, in the META_DATA node to indicate which version of the CPG spec this CPG conforms to.
  VERSION = 13;

  // For formal method input parameters, output parameters, and return parameters, this field holds the evaluation strategy, which is one of the following: 1) `BY_REFERENCE` indicates that the parameter is passed by reference, 2) `BY_VALUE` indicates that it is passed by value, that is, a copy is made, 3) `BY_SHARING` the parameter is a pointer/reference and it is shared with the caller/callee. While a copy of the pointer is made, a copy of the object that it points to is not made.
  EVALUATION_STRATEGY = 15;

  // This optional fields provides the column number at which the program construct represented by the node ends.
  COLUMN_NUMBER_END = 16;

  // This field indicates which CPG language frontend generated the CPG. Frontend developers may freely choose a value that describes their frontend so long as it is not used by an existing frontend. Reserved values are to date: C, LLVM, GHIDRA, PHP.
  LANGUAGE = 19;

  // Certain files, e.g., configuration files, may be included in the CPG as-is. For such files, the `CONTENT` field contains the files content.
  CONTENT = 20;

  // This field holds the code snippet that the node represents.
  CODE = 21;

  // The method signature encodes the types of parameters in a string. The string SHOULD be human readable and suitable for differentiating methods with different parameter types sufficiently to allow for resolving of function overloading. The present specification does not enforce a strict format for the signature, that is, it can be chosen by the frontend implementor to fit the source language.
  SIGNATURE = 22;

  // This field holds the dispatch type of a call, which is either `STATIC_DISPATCH` or `DYNAMIC_DISPATCH`. For statically dispatched method calls, the call target is known at compile time while for dynamically dispatched calls, it can only be determined at runtime as it may depend on the type of an object (as is the case for virtual method calls) or calculation of an offset.
  DISPATCH_TYPE = 25;

  // The modifier type is a free-form string. The following are known modifier types: `STATIC`, `PUBLIC`, `PROTECTED`, `PRIVATE`, `ABSTRACT`, `NATIVE`, `CONSTRUCTOR`, `VIRTUAL`.
  MODIFIER_TYPE = 26;

  // The `CONTROL_STRUCTURE_TYPE` field indicates which kind of control structure a `CONTROL_STRUCTURE` node represents. The available types are the following: BREAK, CONTINUE, DO, WHILE, FOR, GOTO, IF, ELSE, TRY, THROW and SWITCH.
  CONTROL_STRUCTURE_TYPE = 27;

  // AST-children of CALL nodes have an argument index, that is used to match call-site arguments with callee parameters. Explicit parameters are numbered from 1 to N, while index 0 is reserved for implicit self / this parameter. CALLs without implicit parameter therefore have arguments starting with index 1. AST-children of BLOCK nodes may have an argument index as well; in this case, the last argument index determines the return expression of a BLOCK expression. If the `PARAMETER_NAME` field is set, then the `ARGUMENT_INDEX` field is ignored. It is suggested to set it to -1.
  ARGUMENT_INDEX = 40;

  // Identifier which uniquely describes a CLOSURE_BINDING. This property is used to match captured LOCAL nodes with the corresponding CLOSURE_BINDING nodes.
  CLOSURE_BINDING_ID = 50;

  // This field contains the fully-qualified static type name of the program construct represented by a node. It is the name of an instantiated type, e.g., `java.util.List<Integer>`, rather than `java.util.List[T]`. If the type cannot be determined, this field should be set to the empty string.
  TYPE_FULL_NAME = 51;

  // The static type decl of a TYPE. This property is matched against the FULL_NAME of TYPE_DECL nodes. It is required to have exactly one TYPE_DECL for each different TYPE_DECL_FULL_NAME.
  TYPE_DECL_FULL_NAME = 52;

  // The static types a TYPE_DECL inherits from. This property is matched against the FULL_NAME of TYPE nodes and thus it is required to have at least one TYPE node for each TYPE_FULL_NAME.
  INHERITS_FROM_TYPE_FULL_NAME = 53;

  // The FULL_NAME of a method. Used to link CALL and METHOD nodes. It is required to have exactly one METHOD node for each METHOD_FULL_NAME.
  METHOD_FULL_NAME = 54;

  // The type of the AST parent. Since this is only used in some parts of the graph, the list does not include all possible parents by intention. Possible parents: METHOD, TYPE_DECL, NAMESPACE_BLOCK.
  AST_PARENT_TYPE = 56;

  // This field holds the FULL_NAME of the AST parent of an entity.
  AST_PARENT_FULL_NAME = 57;

  // The group ID for a dependency
  DEPENDENCY_GROUP_ID = 58;

  // Symbols
  SYMBOL = 100;

  // Method short name.
  METHOD_SHORT_NAME = 102;

  // Method package name.
  PACKAGE_NAME = 103;

  // Method class name.
  CLASS_NAME = 104;

  // Label for the node which could be code.
  NODE_LABEL = 105;

  // The path of the source file this node was generated from, relative to the root path in the meta data node. This field must be set but may be set to the value `<unknown>` to indicate that no source file can be associated with the node, e.g., because the node represents an entity known to exist because it is referenced, but for which the file that is is declared in is unknown.
  FILENAME = 106;

  // The field contains the names of the overlays applied to this CPG, in order of their application. Names are free-form strings, that is, this specification does not dictate them but rather requires tool producers and consumers to communicate them between each other.
  OVERLAYS = 118;

  // This property contains a hash value in the form of a string. Hashes can be used to summarize data, e.g., to summarize the contents of source files or sub graphs. Such summaries are useful to determine whether code has already been analyzed in incremental analysis pipelines. This property is optional to allow its calculation to be deferred or skipped if the hash is not needed.
  HASH = 120;

  // For calls involving named parameters, the `ARGUMENT_NAME` field holds the name of the parameter initialized by the expression. For all other calls, this field is unset.
  ARGUMENT_NAME = 130;

  // This property denotes a key of a key-value pair.
  KEY = 131;

  // Class short name
  CLASS_SHORT_NAME = 132;

  // This property holds the fully qualified name of the type that the node is a type alias of.
  ALIAS_TYPE_FULL_NAME = 158;

  // The original name of the (potentially mangled) captured variable
  CLOSURE_ORIGINAL_NAME = 159;

  // Specifies whether a parameter is the variadic argument handling parameter of a variadic method. Only one parameter of a method is allowed to have this property set to true.
  IS_VARIADIC = 221;

  // The path to the root directory of the source/binary this CPG is generated from.
  ROOT = 1199;

  // Type hint for the dynamic type.
  DYNAMIC_TYPE_HINT_FULL_NAME = 1591;

  // Specifies an index, e.g., for a parameter or argument. Explicit parameters are numbered from 1 to N, while index 0 is reserved for implicit self / this parameter.
  INDEX = 2223;

  // This field holds the canonical name of a `FIELD_IDENTIFIER`. It is typically identical to the CODE field, but canonicalized according to source language semantics. Human readable names are preferable. `FIELD_IDENTIFIER` nodes must share identical `CANONICAL_NAME` if and only if they alias, e.g., in C-style unions (if the aliasing relationship is unknown or there are partial overlaps, then one must make a reasonable guess, and trade off between false negatives and false positives).
  CANONICAL_NAME = 2001092;

  // References to other nodes. This is not a real property; it exists here for the sake of proto serialization only. valueType and cardinality are meaningless.
  CONTAINED_REF = 2007161;

}

// Enum for the name of an edge
enum EdgePropertyName {
  UNKNOWN_EDGE_PROPERTY = 0;

  // This edge property represents the variable propagated by a reaching definition edge.
  VARIABLE = 11;
}

// Enum for the possible modifier types for symbols, methods and class nodes
enum ModifierTypes {
  UNKNOWN_MODIFIER_TYPE = 0;

  // The static modifier
  STATIC = 1;

  // The public modifier
  PUBLIC = 2;

  // The protected modifier
  PROTECTED = 3;

  // The private modifier
  PRIVATE = 4;

  // The abstract modifier
  ABSTRACT = 5;

  // The native modifier
  NATIVE = 6;

  // The constructor modifier
  CONSTRUCTOR = 7;

  // The virtual modifier
  VIRTUAL = 8;

  // The internal modifier
  INTERNAL = 9;

  // The final modifier
  FINAL = 10;

  // The readonly modifier
  READONLY = 11;
}

// Enum to represent the frontend language
enum LANGUAGES {

  // Unknown language
  UNKNOWN_LANGUAGE = 0;

  // Java
  JAVA = 1;

  // JavaScript and TypeScript
  JAVASCRIPT = 2;

  // Go
  GOLANG = 3;

  // csharp / dotnet
  CSHARP = 4;

  // C/C++
  C = 5;

  // Python
  PYTHON = 6;

  // llvm
  LLVM = 7;

  // PHP
  PHP = 8;

  // Test
  FUZZY_TEST_LANG = 9;

  // generic reverse engineering framework
  GHIDRA = 10;

  // Kotlin
  KOTLIN = 11;

  // Eclipse CDT based parser for C/C++
  NEWC = 12;

  // Source-based front-end for Java
  JAVASRC = 13;

  // Source-based front-end for Python
  PYTHONSRC = 14;

  // Source-based JS frontend based on Babel
  JSSRC = 15;

  // Solidity language frontend
  SOLIDITY = 16;

  // Source-based frontend for Ruby
  RUBYSRC = 17;

}

// Enum representing the evaluation strategy of the underlying parameter or method or literal.
enum EvaluationStrategies {
  UNKNOWN_EVALUATION_STRATEGY = 0;

    // A parameter or return of a function is passed by reference which means an address is used behind the scenes
  BY_REFERENCE = 1;

  // Only applicable to object parameter or return values. The pointer to the object is passed by value but the object itself is not copied and changes to it are thus propagated out of the method context
  BY_SHARING = 2;

  // A parameter or return of a function passed by value which means a flat copy is used
  BY_VALUE = 3;

}

// Enum representing the dispatch types
enum DispatchTypes {
  UNKNOWN_DISPATCH_TYPE = 0;

    // For statically dispatched calls the call target is known before program execution
  STATIC_DISPATCH = 1;

  // For dynamically dispatched calls the target is determined during runtime
  DYNAMIC_DISPATCH = 2;

  // For macro expansions, code is inlined.
  INLINED = 3;

}

// Enum representing the control structure types
enum CONTROL_STRUCTURE_TYPES {
  UNKNOWN_CONTROL_STRUCTURE_TYPE = 0;

  // Represents a break statement. Labeled breaks are expected to have a JUMP_LABEL node AST child with ORDER 1
  BREAK = 1;

  // Represents a continue statement. Labeled continues are expected to have a JUMP_LABEL node AST child with ORDER 1
  CONTINUE = 2;

  // Represents a while statement
  WHILE = 3;

  // Represents a do statement
  DO = 4;

  // Represents a for statement
  FOR = 5;

  // Represents a goto statement
  GOTO = 6;

  // Represents an if statement
  IF = 7;

  // Represents an else statement
  ELSE = 8;

  // Represents a switch statement
  SWITCH = 9;

  // Represents a try statement
  TRY = 10;

  // Represents a throw statement
  THROW = 11;

  // Represents a match expression
  MATCH = 12;

  // Represents a yield expression
  YIELD = 13;

}

/**
 * Programming languages offer many closely-related concepts for describing blocks of code that can be executed with input parameters and return output parameters, possibly causing side effects. In the CPG specification, we refer to all of these concepts (procedures, functions, methods, etc.) as methods. A single METHOD node must exist for each method found in the source program.
 * The `FULL_NAME` field specifies the method's fully-qualified name, including information about the namespace it is contained in if applicable, the name field is the function's short name. The field `IS_EXTERNAL` indicates whether it was possible to identify a method body for the method. This is true for methods that are defined in the source program, and false for methods that are dynamically linked to the program, that is, methods that exist in an external dependency.
 * Line and column number information is specified in the optional fields `LINE_NUMBER`, `COLUMN_NUMBER`, `LINE_NUMBER_END`, and `COLUMN_NUMBER_END` and the name of the source file is specified in `FILENAME`. An optional hash value MAY be calculated over the function contents and included in the `HASH` field.
 * Finally, the fully qualified name of the program constructs that the method is immediately contained in is stored in the `AST_PARENT_FULL_NAME` field and its type is indicated in the `AST_PARENT_TYPE` field to be one of `METHOD`, `TYPE_DECL` or `NAMESPACE_BLOCK`.
 */
enum NodeType {
  UNKNOWN_NODE_TYPE = 0;

  // Method Nodes
  METHOD = 1;

  // This node represents an (unnamed) formal method return parameter. It carries its fully qualified type name in `TYPE_FULL_NAME`. The `CODE` field MAY be set freely, e.g., to the constant `RET`, however, subsequent layer creators MUST NOT depend on this value.
  METHOD_RETURN = 3;

  // A method annotation. The semantics of the FULL_NAME property on this node differ from the usual FULL_NAME semantics in the sense that FULL_NAME describes the represented annotation class/interface itself and not the ANNOTATION node.
  ANNOTATION = 5;

  // Assignment of annotation argument to annotation parameter
  ANNOTATION_PARAMETER_ASSIGN = 6;

  // Formal annotation parameter
  ANNOTATION_PARAMETER = 7;

  // This node represents a literal such as an integer or string constant. Literals are symbols included in the code in verbatim form and which are immutable. The `TYPE_FULL_NAME` field stores the literal's fully-qualified type name, e.g., `java.lang.Integer`.
  LITERAL = 8;

  // This node represents a type member of a class, struct or union, e.g., for the type declaration `class Foo{ int i ; }`, it represents the declaration of the variable `i`.
  MEMBER = 9;

  // Initialization construct for arrays
  ARRAY_INITIALIZER = 14;

  // A (function/method/procedure) call. The `METHOD_FULL_NAME` property is the name of the invoked method (the callee) while the `TYPE_FULL_NAME` is its return type, and therefore, the return type of the call when viewing it as an expression. For languages like Javascript, it is common that we may know the (short-) name of the invoked method, but we do not know at compile time which method will actually be invoked, e.g., because it depends on a dynamic import. In this case, we leave `METHOD_FULL_NAME` blank but at least fill out `NAME`, which contains the method's (short-) name and `SIGNATURE`, which contains any information we may have about the types of arguments and return value.
  CALL = 15;

  // This node represents a local variable. Its fully qualified type name is stored in the `TYPE_FULL_NAME` field and its name in the `NAME` field. The `CODE` field contains the entire local variable declaration without initialization, e.g., for `int x = 10;`, it contains `int x`.
  LOCAL = 23;

  // This node represents a tag.
  TAG = 24;

  // A location node summarizes a source code location.
  LOCATION = 25;

  // This node represents an identifier as used when referring to a variable by name. It holds the identifier's name in the `NAME` field and its fully-qualified type name in `TYPE_FULL_NAME`.
  IDENTIFIER = 27;

  // This node represents a return instruction, e.g., `return x`. Note that it does NOT represent a formal return parameter as formal return parameters are represented via `METHOD_RETURN` nodes.
  RETURN = 30;

  // This node represents a compound statement. Compound statements are used in many languages to allow grouping a sequence of statements. For example, in C and Java, compound statements are statements enclosed by curly braces. Function/Method bodies are compound statements. We do not use the term "compound statement" because "statement" would imply that the block does not yield a value upon evaluation, that is, that it is not an expression. This is true in languages such as C and Java, but not for languages such as Scala where the value of the block is given by that of the last expression it contains. In fact, the Scala grammar uses the term "BlockExpr" (short for "block expression") to describe what in the CPG we call "Block".
  BLOCK = 31;

  // This node represents a formal output parameter. Corresponding output parameters for input parameters MUST NOT be created by the frontend as they are automatically created upon first loading the CPG.
  METHOD_PARAMETER_OUT = 33;

  // This node represents a formal input parameter. The field `NAME` contains its name, while the field `TYPE_FULL_NAME` contains the fully qualified type name.
  METHOD_PARAMETER_IN = 34;

  // This node represents a dependency
  DEPENDENCY = 35;

  /**
   * File nodes represent source files or a shared objects from which the CPG was generated. File nodes serve as indices, that is, they allow looking up all elements of the code by file.
   * For each file, the graph MUST contain exactly one File node. As file nodes are root nodes of abstract syntax tress, they are AstNodes and their order field is set to 0. This is because they have no sibling nodes, not because they are the first node of the AST.
   * Each CPG MUST contain a special file node with name set to `<unknown>`. This node is a placeholder used in cases where a file cannot be determined at compile time. As an example, consider external library functions. As their code is not available on CPG construction, the file name is unknown.
   * File nodes MUST NOT be created by the language frontend. Instead, the language frontend is assumed to fill out the `FILENAME` field wherever possible, allowing File nodes to be created automatically upon first loading the CPG.
   */
  FILE = 38;

  // This node contains the CPG meta data. Exactly one node of this type MUST exist per CPG. The `HASH` property MAY contain a hash value calculated over the source files this CPG was generated from. The `VERSION` MUST be set to the version of the specification ("1.1"). The language field indicates which language frontend was used to generate the CPG and the list property `OVERLAYS` specifies which overlays have been applied to the CPG.
  META_DATA = 39;

  // This node represents a namespace. Similar to FILE nodes, NAMESPACE nodes serve as indices that allow all definitions inside a namespace to be obtained by following outgoing edges from a NAMESPACE node.
  // NAMESPACE nodes MUST NOT be created by language frontends. Instead, they are generated from NAMESPACE_BLOCK nodes automatically upon first loading of the CPG.
  NAMESPACE = 40;

  /**
   * A reference to a namespace.
   * We borrow the concept of a "namespace block" from C++, that is, a namespace block is a block of code that has been placed in the same namespace by a programmer. This block may be introduced via a `package` statement in Java or a `namespace{ }` statement in C++.
   * The `FULL_NAME` field contains a unique identifier to represent the namespace block itself not just the namespace it references. So in addition to the namespace name it can be useful to use the containing file name to derive a unique identifier.
   *
   * The `NAME` field contains the namespace name in a human-readable format. The name should be given in dot-separated form where a dot indicates that the right hand side is a sub namespace of the left hand side, e.g., `foo.bar` denotes the namespace `bar` contained in the namespace `foo`.
   */
  NAMESPACE_BLOCK = 41;

  // Any AST node that the frontend would like to include in the AST but for which no suitable AST node is specified in the CPG specification may be included using a node of type `UNKNOWN`.
  UNKNOWN = 44;

  // This node represents a type instance, that is, a concrete instantiation of a type declaration.
  TYPE = 45;

  /**
   * This node represents a type declaration as for example given by a class-, struct-, or union declaration. In contrast to a `TYPE` node, this node does not represent a concrete instantiation of a type, e.g., for the parametrized type `List[T]`, it represents `List[T]`, but not `List[Integer]` where `Integer` is a concrete type.
   * The language frontend MUST create type declarations for all types declared in the source program and MAY provide type declarations for types that are not declared but referenced by the source program. If a declaration is present in the source program, the field `IS_EXTERNAL` is set to `false`. Otherwise, it is set to `true`.
   * The `FULL_NAME` field specifies the type's fully-qualified name, including information about the namespace it is contained in if applicable, the name field is the type's short name. Line and column number information is specified in the optional fields `LINE_NUMBER`, `COLUMN_NUMBER`, `LINE_NUMBER_END`, and `COLUMN_NUMBER_END` and the name of the source file is specified in `FILENAME`.
   * Base types can be specified via the `INHERITS_FROM_TYPE_FULL_NAME` list, where each entry contains the fully-qualified name of a base type. If the type is known to be an alias of another type (as for example introduced via the C `typedef` statement), the name of the alias is stored in `ALIAS_TYPE_FULL_NAME`.
   * Finally, the fully qualified name of the program constructs that the type declaration is immediately contained in is stored in the `AST_PARENT_FULL_NAME` field and its type is indicated in the `AST_PARENT_TYPE` field to be one of `METHOD`, `TYPE_DECL` or `NAMESPACE_BLOCK`.
   */
  TYPE_DECL = 46;

  // This node represents a formal type parameter, that is, the type parameter as given in a type-parametrized method or type declaration. Examples for languages that support type parameters are Java (via Generics) and C++ (via templates). Apart from the standard fields of AST nodes, the type parameter carries only a `NAME` field that holds the parameters name.
  TYPE_PARAMETER = 47;

  // An (actual) type argument as used to instantiate a parametrized type, in the same way an (actual) arguments provides concrete values for a parameter at method call sites. As it true for arguments, the method is not expected to  interpret the type argument. It MUST however store its code in the `CODE` field.
  TYPE_ARGUMENT = 48;

  // A literal value assigned to an ANNOTATION_PARAMETER
  ANNOTATION_LITERAL = 49;

  // This node type represent a configuration file, where `NAME` is the name of the file and `content` is its content. The exact representation of the name is left undefined and can be chosen as required by consumers of the corresponding configuration files.
  CONFIG_FILE = 50;

  // `BINDING` nodes represent name-signature pairs that can be resolved at a type declaration (`TYPE_DECL`). They are connected to `TYPE_DECL` nodes via incoming `BINDS` edges. The bound method is either associated with an outgoing `REF` edge to a `METHOD` or with the `METHOD_FULL_NAME` property. The `REF` edge if present has priority.
  BINDING = 146;

  // This node contains an arbitrary node and an associated tag node.
  TAG_NODE_PAIR = 208;

  // Finding nodes may be used to store analysis results in the graph that are to be exposed to an end-user, e.g., information about potential vulnerabilities or dangerous programming practices.
  // A Finding node may contain an abitrary list of key value pairs that characterize the finding, as well as a list of nodes that serve as evidence for the finding.
  FINDING = 214;

  // This node represents a key value pair, where both the key and the value are strings.
  KEY_VALUE_PAIR = 217;

  // This field represents a (language-dependent) modifier such as `static`, `private` or `public`. Unlike most other AST nodes, it is NOT an expression, that is, it cannot be evaluated and cannot be passed as an argument in function calls.
  MODIFIER = 300;

  // This node represents a reference to a method/function/procedure as it appears when a method is passed as an argument in a call. The `METHOD_FULL_NAME` field holds the fully-qualified name of the referenced method and the `TYPE_FULL_NAME` holds its fully-qualified type name.
  METHOD_REF = 333;

  // Represents the binding of a LOCAL or METHOD_PARAMETER_IN into the closure of a method
  CLOSURE_BINDING = 334;

  // Reference to a type/class
  TYPE_REF = 335;

  // This node represents a control structure as introduced by control structure statements as well as conditional and unconditional jumps. Its type is stored in the `CONTROL_STRUCTURE_TYPE` field to be one of several pre-defined types. These types are used in the construction of the control flow layer, making it possible to generate the control flow layer from the abstract syntax tree layer automatically.

  // In addition to the `CONTROL_STRUCTURE_TYPE` field, the `PARSER_TYPE_NAME` field MAY be used by frontends to store the name of the control structure as emitted by the parser or disassembler, however, the value of this field is not relevant for construction of the control flow layer.
  CONTROL_STRUCTURE = 339;

  // A jump target is any location in the code that has been specifically marked as the target of a jump, e.g., via a label. The `NAME` field holds the name of the label while the `PARSER_TYPE_NAME` field holds the name of language construct that this jump target is created from, e.g., "Label".
  JUMP_TARGET = 340;

  // A jump label specifies the label and thus the JUMP_TARGET of control structures BREAK and CONTINUE. The `NAME` field holds the name of the label while the `PARSER_TYPE_NAME` field holds the name of language construct that this jump label is created from, e.g., "Label".
  JUMP_LABEL = 341;

  // This node represents a DOM node used in template languages, e.g., JSX/TSX
  TEMPLATE_DOM = 417;

  // A source code comment
  COMMENT = 511;

  // This node represents the field accessed in a field access, e.g., in `a.b`, it represents `b`. The field name as it occurs in the code is stored in the `CODE` field. This may mean that the `CODE` field holds an expression. The `CANONICAL_NAME` field MAY contain the same value is the `CODE` field but SHOULD contain the normalized name that results from evaluating `CODE` as an expression if such an evaluation is possible for the language frontend. The objective is to store an identifier in `CANONICAL_NAME` that is the same for two nodes iff they refer to the same field, regardless of whether they use the same expression to reference it.
  FIELD_IDENTIFIER = 2001081;

}


// Message to store the property values such as string or int values
message PropertyValue {
  oneof value {
    string string_value = 1;
    bool bool_value = 2;
    int32 int_value = 3;
    int64 long_value = 4;
    float float_value = 5;
    double double_value = 6;
    StringList string_list = 7;
    BoolList bool_list = 8;
    IntList int_list = 9;
    LongList long_list = 10;
    FloatList float_list = 11;
    DoubleList double_list = 12;
    ContainedRefs contained_refs = 13;
  }
}

message ContainedRefs {
  string local_name = 1;
  repeated int64 refs = 2;
}

message StringList {
  repeated string values = 1;
}

message BoolList {
  repeated bool values = 1;
}

message IntList {
  repeated int32 values = 1;
}

message LongList {
  repeated int64 values = 1;
}

message FloatList {
  repeated float values = 1;
}

message DoubleList {
  repeated double values = 1;
}

/**
 * This is the CORE structure that represents a Code Property Graph for the given language
 * This structure must be serialized as bytes and stored in a zip file (such as app.atom) with the name "cpg.proto"
 *
 * Example code snippet in Python
 * ```python
 * atom_struct = atom.CpgStruct(node=[method])
 * with ZipFile(file_name, "w") as zip_file:
 *     zip_file.writestr("cpg.proto", bytes(atom_struct))
 * ```
 *
 * Example code snippet in TypeScript
 * ```typescript
 * const methodFullName = new atom.CpgStruct.Node.Property({
 *  name: atom.NodePropertyName.FULL_NAME,
 *  value: new atom.PropertyValue({ string_value: "main" }),
 * });
 * const method = new atom.CpgStruct.Node({
 *   key: 1,
 *   type: atom.NodeType.METHOD,
 *   property: [methodFullName],
 * });
 * const atomStruct = new atom.CpgStruct({ node: [method] });
 * const serializedBytes = atomStruct.serialize();
 * ```
 */
message CpgStruct {
  message Node {
    int64 key = 1;
    NodeType type = 2;

    // Node properties.
    message Property {
      NodePropertyName name = 1;
      PropertyValue value = 2;
    }
  repeated Property property = 3;
  }
  repeated Node node = 1;

  // Represents a directed edge of a graph
  message Edge {
    reserved 5;
    reserved "key";
    // Source node.
    int64 src = 1;
    // Destination node.
    int64 dst = 2;
    // Edge type.
    enum EdgeType {
      UNKNOWN_EDGE_TYPE = 0;
      // This edge connects a parent node to its child in the syntax tree.
      AST = 3;

      // This edge connects call sites, i.e., nodes with the type `CALL`, to the method node that represent the method they invoke. The frontend MAY create `CALL` edges but is not required to do so. Instead, of the `METHOD_FULL_NAME` field of the `CALL` node is set correctly, `CALL` edges are created automatically as the CPG is first loaded.
      CALL = 6;

      // This edge indicates that the source node is an identifier that denotes access to the destination node. For example, an identifier may reference a local variable.
      REF = 10;

      // Edges from nodes to the tags they are tagged by.
      TAGGED_BY = 11;

      // This edge connects a method input parameter to the corresponding method output parameter.
      PARAMETER_LINK = 12;

      // This edge indicates control flow from the source to the destination node.
      CFG = 19;

      // This edge connects a node to its evaluation type.
      EVAL_TYPE = 21;

      // This edge connects type arguments to type parameters to indicate that the type argument is used to instantiate the type parameter.
      BINDS_TO = 22;

      // Inheritance relation between a type declaration and a type. This edge MUST NOT be created by the language frontend as it is automatically created from `INHERITS_FROM_TYPE_FULL_NAME` fields then the CPG is first loaded.
      INHERITS_FROM = 23;

      // This edge connects a node to the method that contains it.
      CONTAINS = 28;

      // Represents the capturing of a variable into a closure
      CAPTURE = 40;

      // Connection between a captured LOCAL and the corresponding CLOSURE_BINDING
      CAPTURED_BY = 41;

      // Similar to `ARGUMENT` edges, `RECEIVER` edges connect call sites to their receiver arguments. A receiver argument is the object on which a method operates, that is, it is the expression that is assigned to the `this` pointer as control is transferred to the method.
      RECEIVER = 55;

      // The edge connects control structure nodes to the expressions that holds their conditions.
      CONDITION = 56;

      // A reaching definition edge indicates that a variable produced at the source node reaches the destination node without being reassigned on the way. The `VARIABLE` property indicates which variable is propagated.
      REACHING_DEF = 137;

      // This edge represents an alias relation between a type declaration and a type. The language frontend MUST NOT create `ALIAS_OF` edges as they are created automatically based on `ALIAS_TYPE_FULL_NAME` fields when the CPG is first loaded.
      ALIAS_OF = 138;

      // This edge connects a type declaration (`TYPE_DECL`) with a binding node (`BINDING`) and indicates that the type declaration has the binding represented by the binding node, in other words, there is a (name, signature) pair that can be resolved for the type declaration as stored in the binding node.
      BINDS = 155;

      // Argument edges connect call sites (node type `CALL`) to their arguments (node type `EXPRESSION`) as well as `RETURN` nodes to the expressions that return.
      ARGUMENT = 156;

      // This edge connects a node to the node that represents its source file. These edges MUST not be created by the language frontend but are automatically created based on `FILENAME` fields.
      SOURCE_FILE = 157;

      // This edge indicates that the source node immediately dominates the destination node.
      DOMINATE = 181;

      // This edge indicates that the source node immediately post dominates the destination node.
      POST_DOMINATE = 182;

      // A CDG edge expresses that the destination node is control dependent on the source node.
      CDG = 183;

      // Edge from imports to dependencies
      IMPORTS = 23663;

      // Edge from CALL statement in the AST to the IMPORT. We use this edge to traverse from the logical representation of the IMPORT to the corresponding import statement in the AST.
      IS_CALL_FOR_IMPORT = 23664;

    }
    EdgeType type = 3;

    // Edge properties.
    message Property {
      EdgePropertyName name = 1;
      PropertyValue value = 2;
    }
  repeated Property property = 4;
  }
  repeated Edge edge = 2;
}

message AdditionalNodeProperty {
  int64 node_id = 1;
  CpgStruct.Node.Property property = 2;
}

message AdditionalEdgeProperty {
  int64 edge_id = 1;
  CpgStruct.Edge.Property property = 2;
  int64 out_node_key = 3;
  int64 in_node_key = 4;
  CpgStruct.Edge.EdgeType edge_type = 5;
}

// Overlays can be stacked onto each other, therefor their node ids must be globally unique.
message CpgOverlay {
  repeated CpgStruct.Node node = 1;
  repeated CpgStruct.Edge edge = 2;
  repeated AdditionalNodeProperty node_property = 3;
  repeated AdditionalEdgeProperty edge_property = 4;
}

// DiffGraphs can be created independently of each other and therefor when _adding_ nodes|edges,
// each DiffGraph has its own ID space. However, when removing nodes|edges, the nodeIds refer to the
// globally unique graph id space.
message DiffGraph {
  message RemoveNode {
    int64 key = 1;
  }

  message RemoveNodeProperty {
    int64 key = 1;
    NodePropertyName name = 2;
    string local_name = 3;
  }

  message RemoveEdge {
    int64 out_node_key = 1;
    int64 in_node_key = 2;
    CpgStruct.Edge.EdgeType edge_type = 3;
    bytes propertiesHash = 4; // used to identify edges (since our edges don't have ids)
  }

  message RemoveEdgeProperty {
    int64 out_node_key = 1;
    int64 in_node_key = 2;
    CpgStruct.Edge.EdgeType edge_type = 3;
    bytes propertiesHash = 4; // used to identify edges (since our edges don't have ids)
    EdgePropertyName property_name = 5;
  }

  message Entry {
    oneof value {
      CpgStruct.Node node = 1;
      CpgStruct.Edge edge = 2;
      AdditionalNodeProperty node_property = 3;
      AdditionalEdgeProperty edge_property = 4;
      RemoveNode remove_node = 5;
      RemoveNodeProperty remove_node_property = 6;
      RemoveEdge remove_edge = 7;
      RemoveEdgeProperty remove_edge_property = 8;
    }
  }

  repeated Entry entries = 1;
}

/**
 * The usages slice describes how a variable interacts within its procedure. This is perhaps a more "descriptive" slice in some ways. The variables are locals and parameters and the referencing identifiers are tracked to find what the variable calls and what calls it forms an argument of.
 * There are two lists. There is a list of MethodUsageSlice with key "objectSlices" and a list of UserDefinedTypes with key "userDefinedTypes"
 *
 * ![Usage slices overview](./docs/Library%20Usages.png)
 */
message UsageSlice {
    // Label type.
    enum LabelType {
      // Any is used to represent multiple mechanisms
      ANY = 0;
      // Represents a local transfer of data via aliasing. The data defined is via some alias.
      LOCAL = 1;
      // Represents a literal.
      LITERAL = 2;
      // Represents data introduced via a parameter.
      PARAM = 3;
      // Represents data introduced by the return value of a call.
      CALL = 4;
      // Identifier
      IDENTIFIER = 5;
      // Type ref
      TYPE_REF = 6;
      // Represents data introduced by an unhandled data structure.
      UNKNOWN = 10;
    }

    // Represents a source of data-generation, i.e., where data is defined and can be assigned to some variable or used in an argument.
    message TargetObj {
        // variable or parameter name
        string name = 1;
        // Fullname of the data type
        string typeFullName = 2;
        // Position of the parameter or argument
        uint32 position = 3;
        // Boolean to indicate if the call belongs to an external method. label=CALL
        bool isExternal = 4;
        // Line number in the file
        uint32 lineNumber = 5;
        // Column number
        uint32 columnNumber = 6;

        LabelType label = 7;
    }
    // Places where the given symbol is defined
    message DefinedBy {
        // variable or parameter name
        string name = 1;
        // Fullname of the data type
        string typeFullName = 2;
        // Method name
        string resolvedMethod = 3;
        // Position
        uint32 position = 4;
        // Boolean to indicate if the call belongs to an external method. label=CALL
        bool isExternal = 5;
        // Line number
        uint32 lineNumber = 6;
        // Column number
        uint32 columnNumber = 7;
        // Label describing the resolved method or position
        string label = 8;
    }
    // The calls this object is observed to call.
    message InvokedCalls {
        // Call method name
        string callName = 1;
        // Full name of the resolved method
        string resolvedMethod = 2;
        // Types of the parameters
        repeated string paramTypes = 3;
        // Return type
        string returnType = 4;
        // Boolean to indicate if the call belongs to an external method
        bool isExternal = 5;
        // Line number
        uint32 lineNumber = 6;
        // Column number
        uint32 columnNumber = 7;
    }
    // The calls this object is observed to be an argument of.
    message ArgToCalls {
        // Call method name
        string callName = 1;
        // Full name of the resolved method
        string resolvedMethod = 2;
        // Types of the parameters
        repeated string paramTypes = 3;
        // Return type
        string returnType = 4;
        // Argument position
        uint32 position = 5;
        // Boolean to indicate if the call belongs to an external method
        bool isExternal = 6;
        // Line number
        uint32 lineNumber = 7;
        // Column number
        uint32 columnNumber = 8;
    }
    // Describes where and how the given external object/type is used
    message ObjectUsageSlice {
        TargetObj targetObj = 1;
        DefinedBy definedBy = 2;
        repeated InvokedCalls invokedCalls = 3;
        repeated ArgToCalls argToCalls = 4;
    }

    // Packages the object usage slices along with location and an optional method source code.
    message MethodUsageSlice {
        // Raw source code of the method
        string code = 1;
        // Method full name
        string fullName = 2;
        // File name
        string fileName = 3;
        // Line number
        uint32 lineNumber = 4;
        // Column number
        uint32 columnNumber = 5;
        repeated ObjectUsageSlice usages = 6;
    }

    // Represents a local transfer of data via aliasing. The data defined is via some alias.
    message Fields {
        // Name of the local variable
        string name = 1;
        // Full name of the type
        string typeFullName = 2;
        // Line number
        uint32 lineNumber = 3;
        // Column number
        uint32 columnNumber = 4;
        // Type of the definition. Usually LOCAL
        LabelType label = 5;
    }

    // Details related to an observed call.
    message Procedures {
        // Name of the method or call
        string callName = 1;
        // Full name of the resolved method
        string resolvedMethod = 2;
        // Types of the parameters
        repeated string paramTypes = 3;
        // Type of the return value
        string returnType = 4;
        // Line number
        uint32 lineNumber = 5;
        // Column number
        uint32 columnNumber = 6;
    }

    // Describes custom types defined within the application.
    message UserDefinedTypes {
        // Name of the type
        string name = 1;
        repeated Fields fields = 2;
        repeated Procedures procedures = 3;
        // File name
        string fileName = 4;
        // Line number
        uint32 lineNumber = 5;
        // Column number
        uint32 columnNumber = 6;
    }

    repeated MethodUsageSlice objectSlices = 1;
    repeated UserDefinedTypes userDefinedTypes = 2;
}

message Nodes {
        // Id of the node
        uint32 id = 1;
        // Label
        NodeType label = 2;
        // Name of the call or identifier or parameter
        string name = 3;
        // Full name of the call
        string fullName = 4;
        // Method signature of the call
        string signature = 5;
        // Boolean to indicate if this call belongs to an external method
        bool isExternal = 6;
        // Source code
        string code = 7;
        // Type full name
        string typeFullName = 8;
        // Parent method name
        string parentMethodName = 9;
        // Parent filename
        string parentFileName = 10;
        // Parent package name
        string parentPackageName = 11;
        // Parent class name
        string parentClassName = 12;
        // Line number
        uint32 lineNumber = 13;
        // Column number
        uint32 columnNumber = 14;
        // Tags. Can contain simple names including package url
        string tags = 15;
}

/**
 * DataFlow slices offers a list of nodes and edges exported from data dependency graph
 *
 * ![Data Flow slices overview](./docs/Data%20Flows.png)
 */
message DataFlowSlice {

    message Edges {
        // Source node id
        uint32 src = 1;
        // Destination node id
        uint32 dst = 2;
        // Edge type
        CpgStruct.Edge.EdgeType label = 3;
    }

    message Flows {
      // Node id
      repeated uint32 id = 1;
    }

    message Paths {
      // Flows from source to sink
      repeated Flows flows = 1;
    }

    message Graph {
      repeated Nodes nodes = 1;
      repeated Edges edges = 2;
    }

    Graph graph = 1;
    Paths path = 2;
}

/**
 * Reachables slices offers a list of reachable nodes based on automated tags exported from data dependency graph
 */
message ReachableSlice {

    message Reachables {
      repeated Nodes nodes = 1;
    }

    message Purls {
      repeated string purls = 1;
    }

    Reachables reachables = 1;
    Purls purls = 2;
}

```