# Atom npm wrapper

Atom is a novel intermediate representation for applications and a standalone tool powered by the [chen](https://github.com/AppThreat/chen) library. The intermediate representation (a network with nodes and links) is optimized for operations typically used for application analytics and machine learning, including [slicing](./specification/docs/slices.md) and [vectoring](./specification/docs/vectors.md).

This package wraps the atom distributable and makes it available via the npm package registry.

## Usage

```shell
npm install @appthreat/atom
```

Ensure Java 21 is available in the PATH.
