// package: atom
// file: atom.proto

import * as jspb from "google-protobuf";

export class PropertyValue extends jspb.Message {
  hasStringValue(): boolean;
  clearStringValue(): void;
  getStringValue(): string;
  setStringValue(value: string): void;

  hasBoolValue(): boolean;
  clearBoolValue(): void;
  getBoolValue(): boolean;
  setBoolValue(value: boolean): void;

  hasIntValue(): boolean;
  clearIntValue(): void;
  getIntValue(): number;
  setIntValue(value: number): void;

  hasLongValue(): boolean;
  clearLongValue(): void;
  getLongValue(): number;
  setLongValue(value: number): void;

  hasFloatValue(): boolean;
  clearFloatValue(): void;
  getFloatValue(): number;
  setFloatValue(value: number): void;

  hasDoubleValue(): boolean;
  clearDoubleValue(): void;
  getDoubleValue(): number;
  setDoubleValue(value: number): void;

  hasStringList(): boolean;
  clearStringList(): void;
  getStringList(): StringList | undefined;
  setStringList(value?: StringList): void;

  hasBoolList(): boolean;
  clearBoolList(): void;
  getBoolList(): BoolList | undefined;
  setBoolList(value?: BoolList): void;

  hasIntList(): boolean;
  clearIntList(): void;
  getIntList(): IntList | undefined;
  setIntList(value?: IntList): void;

  hasLongList(): boolean;
  clearLongList(): void;
  getLongList(): LongList | undefined;
  setLongList(value?: LongList): void;

  hasFloatList(): boolean;
  clearFloatList(): void;
  getFloatList(): FloatList | undefined;
  setFloatList(value?: FloatList): void;

  hasDoubleList(): boolean;
  clearDoubleList(): void;
  getDoubleList(): DoubleList | undefined;
  setDoubleList(value?: DoubleList): void;

  hasContainedRefs(): boolean;
  clearContainedRefs(): void;
  getContainedRefs(): ContainedRefs | undefined;
  setContainedRefs(value?: ContainedRefs): void;

  getValueCase(): PropertyValue.ValueCase;
  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): PropertyValue.AsObject;
  static toObject(includeInstance: boolean, msg: PropertyValue): PropertyValue.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: PropertyValue, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): PropertyValue;
  static deserializeBinaryFromReader(message: PropertyValue, reader: jspb.BinaryReader): PropertyValue;
}

export namespace PropertyValue {
  export type AsObject = {
    stringValue: string,
    boolValue: boolean,
    intValue: number,
    longValue: number,
    floatValue: number,
    doubleValue: number,
    stringList?: StringList.AsObject,
    boolList?: BoolList.AsObject,
    intList?: IntList.AsObject,
    longList?: LongList.AsObject,
    floatList?: FloatList.AsObject,
    doubleList?: DoubleList.AsObject,
    containedRefs?: ContainedRefs.AsObject,
  }

  export enum ValueCase {
    VALUE_NOT_SET = 0,
    STRING_VALUE = 1,
    BOOL_VALUE = 2,
    INT_VALUE = 3,
    LONG_VALUE = 4,
    FLOAT_VALUE = 5,
    DOUBLE_VALUE = 6,
    STRING_LIST = 7,
    BOOL_LIST = 8,
    INT_LIST = 9,
    LONG_LIST = 10,
    FLOAT_LIST = 11,
    DOUBLE_LIST = 12,
    CONTAINED_REFS = 13,
  }
}

export class ContainedRefs extends jspb.Message {
  getLocalName(): string;
  setLocalName(value: string): void;

  clearRefsList(): void;
  getRefsList(): Array<number>;
  setRefsList(value: Array<number>): void;
  addRefs(value: number, index?: number): number;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ContainedRefs.AsObject;
  static toObject(includeInstance: boolean, msg: ContainedRefs): ContainedRefs.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: ContainedRefs, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): ContainedRefs;
  static deserializeBinaryFromReader(message: ContainedRefs, reader: jspb.BinaryReader): ContainedRefs;
}

export namespace ContainedRefs {
  export type AsObject = {
    localName: string,
    refsList: Array<number>,
  }
}

export class StringList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<string>;
  setValuesList(value: Array<string>): void;
  addValues(value: string, index?: number): string;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): StringList.AsObject;
  static toObject(includeInstance: boolean, msg: StringList): StringList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: StringList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): StringList;
  static deserializeBinaryFromReader(message: StringList, reader: jspb.BinaryReader): StringList;
}

export namespace StringList {
  export type AsObject = {
    valuesList: Array<string>,
  }
}

export class BoolList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<boolean>;
  setValuesList(value: Array<boolean>): void;
  addValues(value: boolean, index?: number): boolean;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): BoolList.AsObject;
  static toObject(includeInstance: boolean, msg: BoolList): BoolList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: BoolList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): BoolList;
  static deserializeBinaryFromReader(message: BoolList, reader: jspb.BinaryReader): BoolList;
}

export namespace BoolList {
  export type AsObject = {
    valuesList: Array<boolean>,
  }
}

export class IntList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<number>;
  setValuesList(value: Array<number>): void;
  addValues(value: number, index?: number): number;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): IntList.AsObject;
  static toObject(includeInstance: boolean, msg: IntList): IntList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: IntList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): IntList;
  static deserializeBinaryFromReader(message: IntList, reader: jspb.BinaryReader): IntList;
}

export namespace IntList {
  export type AsObject = {
    valuesList: Array<number>,
  }
}

export class LongList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<number>;
  setValuesList(value: Array<number>): void;
  addValues(value: number, index?: number): number;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): LongList.AsObject;
  static toObject(includeInstance: boolean, msg: LongList): LongList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: LongList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): LongList;
  static deserializeBinaryFromReader(message: LongList, reader: jspb.BinaryReader): LongList;
}

export namespace LongList {
  export type AsObject = {
    valuesList: Array<number>,
  }
}

export class FloatList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<number>;
  setValuesList(value: Array<number>): void;
  addValues(value: number, index?: number): number;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): FloatList.AsObject;
  static toObject(includeInstance: boolean, msg: FloatList): FloatList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: FloatList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): FloatList;
  static deserializeBinaryFromReader(message: FloatList, reader: jspb.BinaryReader): FloatList;
}

export namespace FloatList {
  export type AsObject = {
    valuesList: Array<number>,
  }
}

export class DoubleList extends jspb.Message {
  clearValuesList(): void;
  getValuesList(): Array<number>;
  setValuesList(value: Array<number>): void;
  addValues(value: number, index?: number): number;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): DoubleList.AsObject;
  static toObject(includeInstance: boolean, msg: DoubleList): DoubleList.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: DoubleList, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): DoubleList;
  static deserializeBinaryFromReader(message: DoubleList, reader: jspb.BinaryReader): DoubleList;
}

export namespace DoubleList {
  export type AsObject = {
    valuesList: Array<number>,
  }
}

export class CpgStruct extends jspb.Message {
  clearNodeList(): void;
  getNodeList(): Array<CpgStruct.Node>;
  setNodeList(value: Array<CpgStruct.Node>): void;
  addNode(value?: CpgStruct.Node, index?: number): CpgStruct.Node;

  clearEdgeList(): void;
  getEdgeList(): Array<CpgStruct.Edge>;
  setEdgeList(value: Array<CpgStruct.Edge>): void;
  addEdge(value?: CpgStruct.Edge, index?: number): CpgStruct.Edge;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): CpgStruct.AsObject;
  static toObject(includeInstance: boolean, msg: CpgStruct): CpgStruct.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: CpgStruct, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): CpgStruct;
  static deserializeBinaryFromReader(message: CpgStruct, reader: jspb.BinaryReader): CpgStruct;
}

export namespace CpgStruct {
  export type AsObject = {
    nodeList: Array<CpgStruct.Node.AsObject>,
    edgeList: Array<CpgStruct.Edge.AsObject>,
  }

  export class Node extends jspb.Message {
    getKey(): number;
    setKey(value: number): void;

    getType(): NodeTypeMap[keyof NodeTypeMap];
    setType(value: NodeTypeMap[keyof NodeTypeMap]): void;

    clearPropertyList(): void;
    getPropertyList(): Array<CpgStruct.Node.Property>;
    setPropertyList(value: Array<CpgStruct.Node.Property>): void;
    addProperty(value?: CpgStruct.Node.Property, index?: number): CpgStruct.Node.Property;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Node.AsObject;
    static toObject(includeInstance: boolean, msg: Node): Node.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Node, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Node;
    static deserializeBinaryFromReader(message: Node, reader: jspb.BinaryReader): Node;
  }

  export namespace Node {
    export type AsObject = {
      key: number,
      type: NodeTypeMap[keyof NodeTypeMap],
      propertyList: Array<CpgStruct.Node.Property.AsObject>,
    }

    export class Property extends jspb.Message {
      getName(): NodePropertyNameMap[keyof NodePropertyNameMap];
      setName(value: NodePropertyNameMap[keyof NodePropertyNameMap]): void;

      hasValue(): boolean;
      clearValue(): void;
      getValue(): PropertyValue | undefined;
      setValue(value?: PropertyValue): void;

      serializeBinary(): Uint8Array;
      toObject(includeInstance?: boolean): Property.AsObject;
      static toObject(includeInstance: boolean, msg: Property): Property.AsObject;
      static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
      static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
      static serializeBinaryToWriter(message: Property, writer: jspb.BinaryWriter): void;
      static deserializeBinary(bytes: Uint8Array): Property;
      static deserializeBinaryFromReader(message: Property, reader: jspb.BinaryReader): Property;
    }

    export namespace Property {
      export type AsObject = {
        name: NodePropertyNameMap[keyof NodePropertyNameMap],
        value?: PropertyValue.AsObject,
      }
    }
  }

  export class Edge extends jspb.Message {
    getSrc(): number;
    setSrc(value: number): void;

    getDst(): number;
    setDst(value: number): void;

    getType(): CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap];
    setType(value: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap]): void;

    clearPropertyList(): void;
    getPropertyList(): Array<CpgStruct.Edge.Property>;
    setPropertyList(value: Array<CpgStruct.Edge.Property>): void;
    addProperty(value?: CpgStruct.Edge.Property, index?: number): CpgStruct.Edge.Property;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Edge.AsObject;
    static toObject(includeInstance: boolean, msg: Edge): Edge.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Edge, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Edge;
    static deserializeBinaryFromReader(message: Edge, reader: jspb.BinaryReader): Edge;
  }

  export namespace Edge {
    export type AsObject = {
      src: number,
      dst: number,
      type: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap],
      propertyList: Array<CpgStruct.Edge.Property.AsObject>,
    }

    export class Property extends jspb.Message {
      getName(): EdgePropertyNameMap[keyof EdgePropertyNameMap];
      setName(value: EdgePropertyNameMap[keyof EdgePropertyNameMap]): void;

      hasValue(): boolean;
      clearValue(): void;
      getValue(): PropertyValue | undefined;
      setValue(value?: PropertyValue): void;

      serializeBinary(): Uint8Array;
      toObject(includeInstance?: boolean): Property.AsObject;
      static toObject(includeInstance: boolean, msg: Property): Property.AsObject;
      static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
      static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
      static serializeBinaryToWriter(message: Property, writer: jspb.BinaryWriter): void;
      static deserializeBinary(bytes: Uint8Array): Property;
      static deserializeBinaryFromReader(message: Property, reader: jspb.BinaryReader): Property;
    }

    export namespace Property {
      export type AsObject = {
        name: EdgePropertyNameMap[keyof EdgePropertyNameMap],
        value?: PropertyValue.AsObject,
      }
    }

    export interface EdgeTypeMap {
      UNKNOWN_EDGE_TYPE: 0;
      AST: 3;
      CALL: 6;
      REF: 10;
      TAGGED_BY: 11;
      PARAMETER_LINK: 12;
      CFG: 19;
      EVAL_TYPE: 21;
      BINDS_TO: 22;
      INHERITS_FROM: 23;
      CONTAINS: 28;
      CAPTURE: 40;
      CAPTURED_BY: 41;
      RECEIVER: 55;
      CONDITION: 56;
      REACHING_DEF: 137;
      ALIAS_OF: 138;
      BINDS: 155;
      ARGUMENT: 156;
      SOURCE_FILE: 157;
      DOMINATE: 181;
      POST_DOMINATE: 182;
      CDG: 183;
      IMPORTS: 23663;
      IS_CALL_FOR_IMPORT: 23664;
    }

    export const EdgeType: EdgeTypeMap;
  }
}

export class AdditionalNodeProperty extends jspb.Message {
  getNodeId(): number;
  setNodeId(value: number): void;

  hasProperty(): boolean;
  clearProperty(): void;
  getProperty(): CpgStruct.Node.Property | undefined;
  setProperty(value?: CpgStruct.Node.Property): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): AdditionalNodeProperty.AsObject;
  static toObject(includeInstance: boolean, msg: AdditionalNodeProperty): AdditionalNodeProperty.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: AdditionalNodeProperty, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): AdditionalNodeProperty;
  static deserializeBinaryFromReader(message: AdditionalNodeProperty, reader: jspb.BinaryReader): AdditionalNodeProperty;
}

export namespace AdditionalNodeProperty {
  export type AsObject = {
    nodeId: number,
    property?: CpgStruct.Node.Property.AsObject,
  }
}

export class AdditionalEdgeProperty extends jspb.Message {
  getEdgeId(): number;
  setEdgeId(value: number): void;

  hasProperty(): boolean;
  clearProperty(): void;
  getProperty(): CpgStruct.Edge.Property | undefined;
  setProperty(value?: CpgStruct.Edge.Property): void;

  getOutNodeKey(): number;
  setOutNodeKey(value: number): void;

  getInNodeKey(): number;
  setInNodeKey(value: number): void;

  getEdgeType(): CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap];
  setEdgeType(value: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap]): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): AdditionalEdgeProperty.AsObject;
  static toObject(includeInstance: boolean, msg: AdditionalEdgeProperty): AdditionalEdgeProperty.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: AdditionalEdgeProperty, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): AdditionalEdgeProperty;
  static deserializeBinaryFromReader(message: AdditionalEdgeProperty, reader: jspb.BinaryReader): AdditionalEdgeProperty;
}

export namespace AdditionalEdgeProperty {
  export type AsObject = {
    edgeId: number,
    property?: CpgStruct.Edge.Property.AsObject,
    outNodeKey: number,
    inNodeKey: number,
    edgeType: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap],
  }
}

export class CpgOverlay extends jspb.Message {
  clearNodeList(): void;
  getNodeList(): Array<CpgStruct.Node>;
  setNodeList(value: Array<CpgStruct.Node>): void;
  addNode(value?: CpgStruct.Node, index?: number): CpgStruct.Node;

  clearEdgeList(): void;
  getEdgeList(): Array<CpgStruct.Edge>;
  setEdgeList(value: Array<CpgStruct.Edge>): void;
  addEdge(value?: CpgStruct.Edge, index?: number): CpgStruct.Edge;

  clearNodePropertyList(): void;
  getNodePropertyList(): Array<AdditionalNodeProperty>;
  setNodePropertyList(value: Array<AdditionalNodeProperty>): void;
  addNodeProperty(value?: AdditionalNodeProperty, index?: number): AdditionalNodeProperty;

  clearEdgePropertyList(): void;
  getEdgePropertyList(): Array<AdditionalEdgeProperty>;
  setEdgePropertyList(value: Array<AdditionalEdgeProperty>): void;
  addEdgeProperty(value?: AdditionalEdgeProperty, index?: number): AdditionalEdgeProperty;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): CpgOverlay.AsObject;
  static toObject(includeInstance: boolean, msg: CpgOverlay): CpgOverlay.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: CpgOverlay, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): CpgOverlay;
  static deserializeBinaryFromReader(message: CpgOverlay, reader: jspb.BinaryReader): CpgOverlay;
}

export namespace CpgOverlay {
  export type AsObject = {
    nodeList: Array<CpgStruct.Node.AsObject>,
    edgeList: Array<CpgStruct.Edge.AsObject>,
    nodePropertyList: Array<AdditionalNodeProperty.AsObject>,
    edgePropertyList: Array<AdditionalEdgeProperty.AsObject>,
  }
}

export class DiffGraph extends jspb.Message {
  clearEntriesList(): void;
  getEntriesList(): Array<DiffGraph.Entry>;
  setEntriesList(value: Array<DiffGraph.Entry>): void;
  addEntries(value?: DiffGraph.Entry, index?: number): DiffGraph.Entry;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): DiffGraph.AsObject;
  static toObject(includeInstance: boolean, msg: DiffGraph): DiffGraph.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: DiffGraph, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): DiffGraph;
  static deserializeBinaryFromReader(message: DiffGraph, reader: jspb.BinaryReader): DiffGraph;
}

export namespace DiffGraph {
  export type AsObject = {
    entriesList: Array<DiffGraph.Entry.AsObject>,
  }

  export class RemoveNode extends jspb.Message {
    getKey(): number;
    setKey(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): RemoveNode.AsObject;
    static toObject(includeInstance: boolean, msg: RemoveNode): RemoveNode.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: RemoveNode, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): RemoveNode;
    static deserializeBinaryFromReader(message: RemoveNode, reader: jspb.BinaryReader): RemoveNode;
  }

  export namespace RemoveNode {
    export type AsObject = {
      key: number,
    }
  }

  export class RemoveNodeProperty extends jspb.Message {
    getKey(): number;
    setKey(value: number): void;

    getName(): NodePropertyNameMap[keyof NodePropertyNameMap];
    setName(value: NodePropertyNameMap[keyof NodePropertyNameMap]): void;

    getLocalName(): string;
    setLocalName(value: string): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): RemoveNodeProperty.AsObject;
    static toObject(includeInstance: boolean, msg: RemoveNodeProperty): RemoveNodeProperty.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: RemoveNodeProperty, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): RemoveNodeProperty;
    static deserializeBinaryFromReader(message: RemoveNodeProperty, reader: jspb.BinaryReader): RemoveNodeProperty;
  }

  export namespace RemoveNodeProperty {
    export type AsObject = {
      key: number,
      name: NodePropertyNameMap[keyof NodePropertyNameMap],
      localName: string,
    }
  }

  export class RemoveEdge extends jspb.Message {
    getOutNodeKey(): number;
    setOutNodeKey(value: number): void;

    getInNodeKey(): number;
    setInNodeKey(value: number): void;

    getEdgeType(): CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap];
    setEdgeType(value: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap]): void;

    getPropertieshash(): Uint8Array | string;
    getPropertieshash_asU8(): Uint8Array;
    getPropertieshash_asB64(): string;
    setPropertieshash(value: Uint8Array | string): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): RemoveEdge.AsObject;
    static toObject(includeInstance: boolean, msg: RemoveEdge): RemoveEdge.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: RemoveEdge, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): RemoveEdge;
    static deserializeBinaryFromReader(message: RemoveEdge, reader: jspb.BinaryReader): RemoveEdge;
  }

  export namespace RemoveEdge {
    export type AsObject = {
      outNodeKey: number,
      inNodeKey: number,
      edgeType: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap],
      propertieshash: Uint8Array | string,
    }
  }

  export class RemoveEdgeProperty extends jspb.Message {
    getOutNodeKey(): number;
    setOutNodeKey(value: number): void;

    getInNodeKey(): number;
    setInNodeKey(value: number): void;

    getEdgeType(): CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap];
    setEdgeType(value: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap]): void;

    getPropertieshash(): Uint8Array | string;
    getPropertieshash_asU8(): Uint8Array;
    getPropertieshash_asB64(): string;
    setPropertieshash(value: Uint8Array | string): void;

    getPropertyName(): EdgePropertyNameMap[keyof EdgePropertyNameMap];
    setPropertyName(value: EdgePropertyNameMap[keyof EdgePropertyNameMap]): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): RemoveEdgeProperty.AsObject;
    static toObject(includeInstance: boolean, msg: RemoveEdgeProperty): RemoveEdgeProperty.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: RemoveEdgeProperty, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): RemoveEdgeProperty;
    static deserializeBinaryFromReader(message: RemoveEdgeProperty, reader: jspb.BinaryReader): RemoveEdgeProperty;
  }

  export namespace RemoveEdgeProperty {
    export type AsObject = {
      outNodeKey: number,
      inNodeKey: number,
      edgeType: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap],
      propertieshash: Uint8Array | string,
      propertyName: EdgePropertyNameMap[keyof EdgePropertyNameMap],
    }
  }

  export class Entry extends jspb.Message {
    hasNode(): boolean;
    clearNode(): void;
    getNode(): CpgStruct.Node | undefined;
    setNode(value?: CpgStruct.Node): void;

    hasEdge(): boolean;
    clearEdge(): void;
    getEdge(): CpgStruct.Edge | undefined;
    setEdge(value?: CpgStruct.Edge): void;

    hasNodeProperty(): boolean;
    clearNodeProperty(): void;
    getNodeProperty(): AdditionalNodeProperty | undefined;
    setNodeProperty(value?: AdditionalNodeProperty): void;

    hasEdgeProperty(): boolean;
    clearEdgeProperty(): void;
    getEdgeProperty(): AdditionalEdgeProperty | undefined;
    setEdgeProperty(value?: AdditionalEdgeProperty): void;

    hasRemoveNode(): boolean;
    clearRemoveNode(): void;
    getRemoveNode(): DiffGraph.RemoveNode | undefined;
    setRemoveNode(value?: DiffGraph.RemoveNode): void;

    hasRemoveNodeProperty(): boolean;
    clearRemoveNodeProperty(): void;
    getRemoveNodeProperty(): DiffGraph.RemoveNodeProperty | undefined;
    setRemoveNodeProperty(value?: DiffGraph.RemoveNodeProperty): void;

    hasRemoveEdge(): boolean;
    clearRemoveEdge(): void;
    getRemoveEdge(): DiffGraph.RemoveEdge | undefined;
    setRemoveEdge(value?: DiffGraph.RemoveEdge): void;

    hasRemoveEdgeProperty(): boolean;
    clearRemoveEdgeProperty(): void;
    getRemoveEdgeProperty(): DiffGraph.RemoveEdgeProperty | undefined;
    setRemoveEdgeProperty(value?: DiffGraph.RemoveEdgeProperty): void;

    getValueCase(): Entry.ValueCase;
    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Entry.AsObject;
    static toObject(includeInstance: boolean, msg: Entry): Entry.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Entry, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Entry;
    static deserializeBinaryFromReader(message: Entry, reader: jspb.BinaryReader): Entry;
  }

  export namespace Entry {
    export type AsObject = {
      node?: CpgStruct.Node.AsObject,
      edge?: CpgStruct.Edge.AsObject,
      nodeProperty?: AdditionalNodeProperty.AsObject,
      edgeProperty?: AdditionalEdgeProperty.AsObject,
      removeNode?: DiffGraph.RemoveNode.AsObject,
      removeNodeProperty?: DiffGraph.RemoveNodeProperty.AsObject,
      removeEdge?: DiffGraph.RemoveEdge.AsObject,
      removeEdgeProperty?: DiffGraph.RemoveEdgeProperty.AsObject,
    }

    export enum ValueCase {
      VALUE_NOT_SET = 0,
      NODE = 1,
      EDGE = 2,
      NODE_PROPERTY = 3,
      EDGE_PROPERTY = 4,
      REMOVE_NODE = 5,
      REMOVE_NODE_PROPERTY = 6,
      REMOVE_EDGE = 7,
      REMOVE_EDGE_PROPERTY = 8,
    }
  }
}

export class UsageSlice extends jspb.Message {
  clearObjectslicesList(): void;
  getObjectslicesList(): Array<UsageSlice.MethodUsageSlice>;
  setObjectslicesList(value: Array<UsageSlice.MethodUsageSlice>): void;
  addObjectslices(value?: UsageSlice.MethodUsageSlice, index?: number): UsageSlice.MethodUsageSlice;

  clearUserdefinedtypesList(): void;
  getUserdefinedtypesList(): Array<UsageSlice.UserDefinedTypes>;
  setUserdefinedtypesList(value: Array<UsageSlice.UserDefinedTypes>): void;
  addUserdefinedtypes(value?: UsageSlice.UserDefinedTypes, index?: number): UsageSlice.UserDefinedTypes;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): UsageSlice.AsObject;
  static toObject(includeInstance: boolean, msg: UsageSlice): UsageSlice.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: UsageSlice, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): UsageSlice;
  static deserializeBinaryFromReader(message: UsageSlice, reader: jspb.BinaryReader): UsageSlice;
}

export namespace UsageSlice {
  export type AsObject = {
    objectslicesList: Array<UsageSlice.MethodUsageSlice.AsObject>,
    userdefinedtypesList: Array<UsageSlice.UserDefinedTypes.AsObject>,
  }

  export class TargetObj extends jspb.Message {
    getName(): string;
    setName(value: string): void;

    getTypefullname(): string;
    setTypefullname(value: string): void;

    getPosition(): number;
    setPosition(value: number): void;

    getIsexternal(): boolean;
    setIsexternal(value: boolean): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    getLabel(): UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap];
    setLabel(value: UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap]): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): TargetObj.AsObject;
    static toObject(includeInstance: boolean, msg: TargetObj): TargetObj.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: TargetObj, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): TargetObj;
    static deserializeBinaryFromReader(message: TargetObj, reader: jspb.BinaryReader): TargetObj;
  }

  export namespace TargetObj {
    export type AsObject = {
      name: string,
      typefullname: string,
      position: number,
      isexternal: boolean,
      linenumber: number,
      columnnumber: number,
      label: UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap],
    }
  }

  export class DefinedBy extends jspb.Message {
    getName(): string;
    setName(value: string): void;

    getTypefullname(): string;
    setTypefullname(value: string): void;

    getResolvedmethod(): string;
    setResolvedmethod(value: string): void;

    getPosition(): number;
    setPosition(value: number): void;

    getIsexternal(): boolean;
    setIsexternal(value: boolean): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    getLabel(): string;
    setLabel(value: string): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): DefinedBy.AsObject;
    static toObject(includeInstance: boolean, msg: DefinedBy): DefinedBy.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: DefinedBy, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): DefinedBy;
    static deserializeBinaryFromReader(message: DefinedBy, reader: jspb.BinaryReader): DefinedBy;
  }

  export namespace DefinedBy {
    export type AsObject = {
      name: string,
      typefullname: string,
      resolvedmethod: string,
      position: number,
      isexternal: boolean,
      linenumber: number,
      columnnumber: number,
      label: string,
    }
  }

  export class InvokedCalls extends jspb.Message {
    getCallname(): string;
    setCallname(value: string): void;

    getResolvedmethod(): string;
    setResolvedmethod(value: string): void;

    clearParamtypesList(): void;
    getParamtypesList(): Array<string>;
    setParamtypesList(value: Array<string>): void;
    addParamtypes(value: string, index?: number): string;

    getReturntype(): string;
    setReturntype(value: string): void;

    getIsexternal(): boolean;
    setIsexternal(value: boolean): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): InvokedCalls.AsObject;
    static toObject(includeInstance: boolean, msg: InvokedCalls): InvokedCalls.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: InvokedCalls, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): InvokedCalls;
    static deserializeBinaryFromReader(message: InvokedCalls, reader: jspb.BinaryReader): InvokedCalls;
  }

  export namespace InvokedCalls {
    export type AsObject = {
      callname: string,
      resolvedmethod: string,
      paramtypesList: Array<string>,
      returntype: string,
      isexternal: boolean,
      linenumber: number,
      columnnumber: number,
    }
  }

  export class ArgToCalls extends jspb.Message {
    getCallname(): string;
    setCallname(value: string): void;

    getResolvedmethod(): string;
    setResolvedmethod(value: string): void;

    clearParamtypesList(): void;
    getParamtypesList(): Array<string>;
    setParamtypesList(value: Array<string>): void;
    addParamtypes(value: string, index?: number): string;

    getReturntype(): string;
    setReturntype(value: string): void;

    getPosition(): number;
    setPosition(value: number): void;

    getIsexternal(): boolean;
    setIsexternal(value: boolean): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): ArgToCalls.AsObject;
    static toObject(includeInstance: boolean, msg: ArgToCalls): ArgToCalls.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: ArgToCalls, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): ArgToCalls;
    static deserializeBinaryFromReader(message: ArgToCalls, reader: jspb.BinaryReader): ArgToCalls;
  }

  export namespace ArgToCalls {
    export type AsObject = {
      callname: string,
      resolvedmethod: string,
      paramtypesList: Array<string>,
      returntype: string,
      position: number,
      isexternal: boolean,
      linenumber: number,
      columnnumber: number,
    }
  }

  export class ObjectUsageSlice extends jspb.Message {
    hasTargetobj(): boolean;
    clearTargetobj(): void;
    getTargetobj(): UsageSlice.TargetObj | undefined;
    setTargetobj(value?: UsageSlice.TargetObj): void;

    hasDefinedby(): boolean;
    clearDefinedby(): void;
    getDefinedby(): UsageSlice.DefinedBy | undefined;
    setDefinedby(value?: UsageSlice.DefinedBy): void;

    clearInvokedcallsList(): void;
    getInvokedcallsList(): Array<UsageSlice.InvokedCalls>;
    setInvokedcallsList(value: Array<UsageSlice.InvokedCalls>): void;
    addInvokedcalls(value?: UsageSlice.InvokedCalls, index?: number): UsageSlice.InvokedCalls;

    clearArgtocallsList(): void;
    getArgtocallsList(): Array<UsageSlice.ArgToCalls>;
    setArgtocallsList(value: Array<UsageSlice.ArgToCalls>): void;
    addArgtocalls(value?: UsageSlice.ArgToCalls, index?: number): UsageSlice.ArgToCalls;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): ObjectUsageSlice.AsObject;
    static toObject(includeInstance: boolean, msg: ObjectUsageSlice): ObjectUsageSlice.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: ObjectUsageSlice, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): ObjectUsageSlice;
    static deserializeBinaryFromReader(message: ObjectUsageSlice, reader: jspb.BinaryReader): ObjectUsageSlice;
  }

  export namespace ObjectUsageSlice {
    export type AsObject = {
      targetobj?: UsageSlice.TargetObj.AsObject,
      definedby?: UsageSlice.DefinedBy.AsObject,
      invokedcallsList: Array<UsageSlice.InvokedCalls.AsObject>,
      argtocallsList: Array<UsageSlice.ArgToCalls.AsObject>,
    }
  }

  export class MethodUsageSlice extends jspb.Message {
    getCode(): string;
    setCode(value: string): void;

    getFullname(): string;
    setFullname(value: string): void;

    getFilename(): string;
    setFilename(value: string): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    clearUsagesList(): void;
    getUsagesList(): Array<UsageSlice.ObjectUsageSlice>;
    setUsagesList(value: Array<UsageSlice.ObjectUsageSlice>): void;
    addUsages(value?: UsageSlice.ObjectUsageSlice, index?: number): UsageSlice.ObjectUsageSlice;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): MethodUsageSlice.AsObject;
    static toObject(includeInstance: boolean, msg: MethodUsageSlice): MethodUsageSlice.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: MethodUsageSlice, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): MethodUsageSlice;
    static deserializeBinaryFromReader(message: MethodUsageSlice, reader: jspb.BinaryReader): MethodUsageSlice;
  }

  export namespace MethodUsageSlice {
    export type AsObject = {
      code: string,
      fullname: string,
      filename: string,
      linenumber: number,
      columnnumber: number,
      usagesList: Array<UsageSlice.ObjectUsageSlice.AsObject>,
    }
  }

  export class Fields extends jspb.Message {
    getName(): string;
    setName(value: string): void;

    getTypefullname(): string;
    setTypefullname(value: string): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    getLabel(): UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap];
    setLabel(value: UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap]): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Fields.AsObject;
    static toObject(includeInstance: boolean, msg: Fields): Fields.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Fields, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Fields;
    static deserializeBinaryFromReader(message: Fields, reader: jspb.BinaryReader): Fields;
  }

  export namespace Fields {
    export type AsObject = {
      name: string,
      typefullname: string,
      linenumber: number,
      columnnumber: number,
      label: UsageSlice.LabelTypeMap[keyof UsageSlice.LabelTypeMap],
    }
  }

  export class Procedures extends jspb.Message {
    getCallname(): string;
    setCallname(value: string): void;

    getResolvedmethod(): string;
    setResolvedmethod(value: string): void;

    clearParamtypesList(): void;
    getParamtypesList(): Array<string>;
    setParamtypesList(value: Array<string>): void;
    addParamtypes(value: string, index?: number): string;

    getReturntype(): string;
    setReturntype(value: string): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Procedures.AsObject;
    static toObject(includeInstance: boolean, msg: Procedures): Procedures.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Procedures, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Procedures;
    static deserializeBinaryFromReader(message: Procedures, reader: jspb.BinaryReader): Procedures;
  }

  export namespace Procedures {
    export type AsObject = {
      callname: string,
      resolvedmethod: string,
      paramtypesList: Array<string>,
      returntype: string,
      linenumber: number,
      columnnumber: number,
    }
  }

  export class UserDefinedTypes extends jspb.Message {
    getName(): string;
    setName(value: string): void;

    clearFieldsList(): void;
    getFieldsList(): Array<UsageSlice.Fields>;
    setFieldsList(value: Array<UsageSlice.Fields>): void;
    addFields(value?: UsageSlice.Fields, index?: number): UsageSlice.Fields;

    clearProceduresList(): void;
    getProceduresList(): Array<UsageSlice.Procedures>;
    setProceduresList(value: Array<UsageSlice.Procedures>): void;
    addProcedures(value?: UsageSlice.Procedures, index?: number): UsageSlice.Procedures;

    getFilename(): string;
    setFilename(value: string): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): UserDefinedTypes.AsObject;
    static toObject(includeInstance: boolean, msg: UserDefinedTypes): UserDefinedTypes.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: UserDefinedTypes, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): UserDefinedTypes;
    static deserializeBinaryFromReader(message: UserDefinedTypes, reader: jspb.BinaryReader): UserDefinedTypes;
  }

  export namespace UserDefinedTypes {
    export type AsObject = {
      name: string,
      fieldsList: Array<UsageSlice.Fields.AsObject>,
      proceduresList: Array<UsageSlice.Procedures.AsObject>,
      filename: string,
      linenumber: number,
      columnnumber: number,
    }
  }

  export interface LabelTypeMap {
    ANY: 0;
    LOCAL: 1;
    LITERAL: 2;
    PARAM: 3;
    CALL: 4;
    IDENTIFIER: 5;
    TYPE_REF: 6;
    UNKNOWN: 10;
  }

  export const LabelType: LabelTypeMap;
}

export class DataFlowSlice extends jspb.Message {
  hasGraph(): boolean;
  clearGraph(): void;
  getGraph(): DataFlowSlice.Graph | undefined;
  setGraph(value?: DataFlowSlice.Graph): void;

  hasPath(): boolean;
  clearPath(): void;
  getPath(): DataFlowSlice.Paths | undefined;
  setPath(value?: DataFlowSlice.Paths): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): DataFlowSlice.AsObject;
  static toObject(includeInstance: boolean, msg: DataFlowSlice): DataFlowSlice.AsObject;
  static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
  static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
  static serializeBinaryToWriter(message: DataFlowSlice, writer: jspb.BinaryWriter): void;
  static deserializeBinary(bytes: Uint8Array): DataFlowSlice;
  static deserializeBinaryFromReader(message: DataFlowSlice, reader: jspb.BinaryReader): DataFlowSlice;
}

export namespace DataFlowSlice {
  export type AsObject = {
    graph?: DataFlowSlice.Graph.AsObject,
    path?: DataFlowSlice.Paths.AsObject,
  }

  export class Nodes extends jspb.Message {
    getId(): number;
    setId(value: number): void;

    getLabel(): NodeTypeMap[keyof NodeTypeMap];
    setLabel(value: NodeTypeMap[keyof NodeTypeMap]): void;

    getName(): string;
    setName(value: string): void;

    getFullname(): string;
    setFullname(value: string): void;

    getSignature(): string;
    setSignature(value: string): void;

    getIsexternal(): boolean;
    setIsexternal(value: boolean): void;

    getCode(): string;
    setCode(value: string): void;

    getTypefullname(): string;
    setTypefullname(value: string): void;

    getParentmethodname(): string;
    setParentmethodname(value: string): void;

    getParentfilename(): string;
    setParentfilename(value: string): void;

    getParentpackagename(): string;
    setParentpackagename(value: string): void;

    getParentclassname(): string;
    setParentclassname(value: string): void;

    getLinenumber(): number;
    setLinenumber(value: number): void;

    getColumnnumber(): number;
    setColumnnumber(value: number): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Nodes.AsObject;
    static toObject(includeInstance: boolean, msg: Nodes): Nodes.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Nodes, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Nodes;
    static deserializeBinaryFromReader(message: Nodes, reader: jspb.BinaryReader): Nodes;
  }

  export namespace Nodes {
    export type AsObject = {
      id: number,
      label: NodeTypeMap[keyof NodeTypeMap],
      name: string,
      fullname: string,
      signature: string,
      isexternal: boolean,
      code: string,
      typefullname: string,
      parentmethodname: string,
      parentfilename: string,
      parentpackagename: string,
      parentclassname: string,
      linenumber: number,
      columnnumber: number,
    }
  }

  export class Edges extends jspb.Message {
    getSrc(): number;
    setSrc(value: number): void;

    getDst(): number;
    setDst(value: number): void;

    getLabel(): CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap];
    setLabel(value: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap]): void;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Edges.AsObject;
    static toObject(includeInstance: boolean, msg: Edges): Edges.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Edges, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Edges;
    static deserializeBinaryFromReader(message: Edges, reader: jspb.BinaryReader): Edges;
  }

  export namespace Edges {
    export type AsObject = {
      src: number,
      dst: number,
      label: CpgStruct.Edge.EdgeTypeMap[keyof CpgStruct.Edge.EdgeTypeMap],
    }
  }

  export class Flows extends jspb.Message {
    clearIdList(): void;
    getIdList(): Array<number>;
    setIdList(value: Array<number>): void;
    addId(value: number, index?: number): number;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Flows.AsObject;
    static toObject(includeInstance: boolean, msg: Flows): Flows.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Flows, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Flows;
    static deserializeBinaryFromReader(message: Flows, reader: jspb.BinaryReader): Flows;
  }

  export namespace Flows {
    export type AsObject = {
      idList: Array<number>,
    }
  }

  export class Paths extends jspb.Message {
    clearFlowsList(): void;
    getFlowsList(): Array<DataFlowSlice.Flows>;
    setFlowsList(value: Array<DataFlowSlice.Flows>): void;
    addFlows(value?: DataFlowSlice.Flows, index?: number): DataFlowSlice.Flows;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Paths.AsObject;
    static toObject(includeInstance: boolean, msg: Paths): Paths.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Paths, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Paths;
    static deserializeBinaryFromReader(message: Paths, reader: jspb.BinaryReader): Paths;
  }

  export namespace Paths {
    export type AsObject = {
      flowsList: Array<DataFlowSlice.Flows.AsObject>,
    }
  }

  export class Graph extends jspb.Message {
    clearNodesList(): void;
    getNodesList(): Array<DataFlowSlice.Nodes>;
    setNodesList(value: Array<DataFlowSlice.Nodes>): void;
    addNodes(value?: DataFlowSlice.Nodes, index?: number): DataFlowSlice.Nodes;

    clearEdgesList(): void;
    getEdgesList(): Array<DataFlowSlice.Edges>;
    setEdgesList(value: Array<DataFlowSlice.Edges>): void;
    addEdges(value?: DataFlowSlice.Edges, index?: number): DataFlowSlice.Edges;

    serializeBinary(): Uint8Array;
    toObject(includeInstance?: boolean): Graph.AsObject;
    static toObject(includeInstance: boolean, msg: Graph): Graph.AsObject;
    static extensions: {[key: number]: jspb.ExtensionFieldInfo<jspb.Message>};
    static extensionsBinary: {[key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>};
    static serializeBinaryToWriter(message: Graph, writer: jspb.BinaryWriter): void;
    static deserializeBinary(bytes: Uint8Array): Graph;
    static deserializeBinaryFromReader(message: Graph, reader: jspb.BinaryReader): Graph;
  }

  export namespace Graph {
    export type AsObject = {
      nodesList: Array<DataFlowSlice.Nodes.AsObject>,
      edgesList: Array<DataFlowSlice.Edges.AsObject>,
    }
  }
}

export interface NodePropertyNameMap {
  UNKNOWN_NODE_PROPERTY: 0;
  LINE_NUMBER: 2;
  PARSER_TYPE_NAME: 3;
  ORDER: 4;
  NAME: 5;
  FULL_NAME: 6;
  IS_EXTERNAL: 7;
  VALUE: 8;
  COLUMN_NUMBER: 11;
  LINE_NUMBER_END: 12;
  VERSION: 13;
  EVALUATION_STRATEGY: 15;
  COLUMN_NUMBER_END: 16;
  LANGUAGE: 19;
  CONTENT: 20;
  CODE: 21;
  SIGNATURE: 22;
  DISPATCH_TYPE: 25;
  MODIFIER_TYPE: 26;
  CONTROL_STRUCTURE_TYPE: 27;
  ARGUMENT_INDEX: 40;
  CLOSURE_BINDING_ID: 50;
  TYPE_FULL_NAME: 51;
  TYPE_DECL_FULL_NAME: 52;
  INHERITS_FROM_TYPE_FULL_NAME: 53;
  METHOD_FULL_NAME: 54;
  AST_PARENT_TYPE: 56;
  AST_PARENT_FULL_NAME: 57;
  DEPENDENCY_GROUP_ID: 58;
  SYMBOL: 100;
  METHOD_SHORT_NAME: 102;
  PACKAGE_NAME: 103;
  CLASS_NAME: 104;
  NODE_LABEL: 105;
  FILENAME: 106;
  OVERLAYS: 118;
  HASH: 120;
  ARGUMENT_NAME: 130;
  KEY: 131;
  CLASS_SHORT_NAME: 132;
  ALIAS_TYPE_FULL_NAME: 158;
  CLOSURE_ORIGINAL_NAME: 159;
  IS_VARIADIC: 221;
  ROOT: 1199;
  DYNAMIC_TYPE_HINT_FULL_NAME: 1591;
  INDEX: 2223;
  CANONICAL_NAME: 2001092;
  CONTAINED_REF: 2007161;
}

export const NodePropertyName: NodePropertyNameMap;

export interface EdgePropertyNameMap {
  UNKNOWN_EDGE_PROPERTY: 0;
  VARIABLE: 11;
}

export const EdgePropertyName: EdgePropertyNameMap;

export interface ModifierTypesMap {
  UNKNOWN_MODIFIER_TYPE: 0;
  STATIC: 1;
  PUBLIC: 2;
  PROTECTED: 3;
  PRIVATE: 4;
  ABSTRACT: 5;
  NATIVE: 6;
  CONSTRUCTOR: 7;
  VIRTUAL: 8;
  INTERNAL: 9;
  FINAL: 10;
  READONLY: 11;
}

export const ModifierTypes: ModifierTypesMap;

export interface LANGUAGESMap {
  UNKNOWN_LANGUAGE: 0;
  JAVA: 1;
  JAVASCRIPT: 2;
  GOLANG: 3;
  CSHARP: 4;
  C: 5;
  PYTHON: 6;
  LLVM: 7;
  PHP: 8;
  FUZZY_TEST_LANG: 9;
  GHIDRA: 10;
  KOTLIN: 11;
  NEWC: 12;
  JAVASRC: 13;
  PYTHONSRC: 14;
  JSSRC: 15;
  SOLIDITY: 16;
  RUBYSRC: 17;
}

export const LANGUAGES: LANGUAGESMap;

export interface EvaluationStrategiesMap {
  UNKNOWN_EVALUATION_STRATEGY: 0;
  BY_REFERENCE: 1;
  BY_SHARING: 2;
  BY_VALUE: 3;
}

export const EvaluationStrategies: EvaluationStrategiesMap;

export interface DispatchTypesMap {
  UNKNOWN_DISPATCH_TYPE: 0;
  STATIC_DISPATCH: 1;
  DYNAMIC_DISPATCH: 2;
  INLINED: 3;
}

export const DispatchTypes: DispatchTypesMap;

export interface CONTROL_STRUCTURE_TYPESMap {
  UNKNOWN_CONTROL_STRUCTURE_TYPE: 0;
  BREAK: 1;
  CONTINUE: 2;
  WHILE: 3;
  DO: 4;
  FOR: 5;
  GOTO: 6;
  IF: 7;
  ELSE: 8;
  SWITCH: 9;
  TRY: 10;
  THROW: 11;
  MATCH: 12;
  YIELD: 13;
}

export const CONTROL_STRUCTURE_TYPES: CONTROL_STRUCTURE_TYPESMap;

export interface NodeTypeMap {
  UNKNOWN_NODE_TYPE: 0;
  METHOD: 1;
  METHOD_RETURN: 3;
  ANNOTATION: 5;
  ANNOTATION_PARAMETER_ASSIGN: 6;
  ANNOTATION_PARAMETER: 7;
  LITERAL: 8;
  MEMBER: 9;
  ARRAY_INITIALIZER: 14;
  CALL: 15;
  LOCAL: 23;
  TAG: 24;
  LOCATION: 25;
  IDENTIFIER: 27;
  RETURN: 30;
  BLOCK: 31;
  METHOD_PARAMETER_OUT: 33;
  METHOD_PARAMETER_IN: 34;
  DEPENDENCY: 35;
  FILE: 38;
  META_DATA: 39;
  NAMESPACE: 40;
  NAMESPACE_BLOCK: 41;
  UNKNOWN: 44;
  TYPE: 45;
  TYPE_DECL: 46;
  TYPE_PARAMETER: 47;
  TYPE_ARGUMENT: 48;
  ANNOTATION_LITERAL: 49;
  CONFIG_FILE: 50;
  BINDING: 146;
  TAG_NODE_PAIR: 208;
  FINDING: 214;
  KEY_VALUE_PAIR: 217;
  MODIFIER: 300;
  METHOD_REF: 333;
  CLOSURE_BINDING: 334;
  TYPE_REF: 335;
  CONTROL_STRUCTURE: 339;
  JUMP_TARGET: 340;
  JUMP_LABEL: 341;
  TEMPLATE_DOM: 417;
  COMMENT: 511;
  FIELD_IDENTIFIER: 2001081;
}

export const NodeType: NodeTypeMap;

