---
sidebar_position: 9
title: Generate Atom Files
---

# Generating atom files

Atom files (app.⚛ or app.atom) are zip files with serialized protobuf data. Atom cli is the preferred approach to generate these files. It is possible to author a generator tool from scratch using the [proto specification](./specification/atom.proto). We offer samples in [Python](./specification/samples/python-atomgen/README.md) and [Deno](./specification/samples/deno-atomgen/README.md) for interested users. We also offer proto bindings in additional languages which can be found [here](./specification/bindings/).

Example code snippet for generating an atom in python.

```python
# Create a method fullname property
methodFullName = atom.CpgStructNodeProperty(
    name=atom.NodePropertyName.FULL_NAME, value=atom.PropertyValue("main")
)

# Create a method node with the fullname property
method = atom.CpgStructNode(
    key=1, type=atom.NodeType.METHOD, property=[methodFullName]
)

# Create an atom with a single node
atom_struct = atom.CpgStruct(node=[method])

# Create an atom (app.atom) by serializing this data into a zip file
with ZipFile("app.atom", "w") as zip_file:
    zip_file.writestr("cpg.proto", bytes(atom_struct))
```
