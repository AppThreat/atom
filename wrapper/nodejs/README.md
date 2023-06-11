# Atom npm wrapper

This package wraps the atom distributable and makes it available via the npm package registry. Atom is an upcoming intermediate representation for application and packages that is optimized for threat analysis and supply-chain security use cases.

## Usage

```shell
npm install @appthreat/atom
```

Ensure Java 17/19 is available in the PATH.

## Troubleshooting

For large projects, atom requires more heap memory which could be passed via `JAVA_OPTS` environment variable.

```
export JAVA_OPTS="-Xms8G -Xmx16G"
```
