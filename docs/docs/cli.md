---
sidebar_position: 5
title: CLI Usage
---

# CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|reachables] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  -s, --slice-outfile <value>
                           export intra-procedural slices as json
  -l, --language <value>   source language
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --remove-atom            do not persist the atom file - defaults to `false`
  -x, --export-atom        export the atom file with data-dependencies to graphml - defaults to `false`
  --export-dir <value>     export directory. Default: atom-exports
  --file-filter <value>    the name of the source file to generate slices from. Uses regex.
  --method-name-filter <value>
                           filters in slices that go through specific methods by names. Uses regex.
  --method-parameter-filter <value>
                           filters in slices that go through methods with specific types on the method parameters. Uses regex.
  --method-annotation-filter <value>
                           filters in slices that go through methods with specific annotations on the methods. Uses regex.
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation - defaults to 2000
Command: parsedeps
Extract dependencies from the build file and imports
Command: data-flow [options]
Extract backward data-flow slices
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice - defaults to 7.
  --sink-filter <value>    filters on the sink's `code` property. Uses regex.
Command: usages [options]
Extract local variable and parameter usages
  --min-num-calls <value>  the minimum number of calls required for a usage slice - defaults to 1.
  --include-source         includes method source code in the slices - defaults to false.
  --extract-endpoints      extract http endpoints and convert to openapi format using atom-tools - defaults to false.
Command: reachables [options]
Extract reachable data-flow slices based on automated framework tags
  --source-tag <value>     source tag - defaults to framework-input.
  --sink-tag <value>       sink tag - defaults to framework-output.
  --include-crypto         includes crypto library flows - defaults to false.
  --help                   display this help message
```
