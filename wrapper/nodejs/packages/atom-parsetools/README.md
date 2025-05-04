# Introduction

This package hosts a collection of parsing tools that complement the `@appthreat/atom` project. These tools offer parsing and analysis-related functionalities such as generating AST and semantics information in JSON format. The full list of tools and bin commands exposed by this package is below:

- astgen - Generates AST for JavaScript and TypeScript projects in JSON format
- phpastgen - Generates AST for PHP projects using `php-parse` command from `nikic/php-parser`
- rbastgen - Generates AST for Ruby projects using the AppThreat's `ruby_ast_gen` gem
- scalasem - Generates a custom semantics slice for Scala Projects by utilising scalac command.

## Command usages

### astgen

```text
node astgen.js -h
Options:
  -i, --src      Source directory                                 [default: "."]
  -o, --output   Output directory for generated AST JSON files
                                                            [default: "ast_out"]
  -t, --type     Project type. Default auto-detect
  -r, --recurse  Recurse mode suitable for mono-repos  [boolean] [default: true]
      --tsTypes  Generate type mappings using the TypeScript Compiler API
                                                       [boolean] [default: true]
      --version  Show version number                                   [boolean]
  -h             Show help                                             [boolean]
```

### phpastgen

```text
node phpastgen.js --help

Usage: phpastgen [operations] file1.php [file2.php ...]
   or: phpastgen [operations] "<?php code"
Turn PHP source code into an abstract syntax tree.

Operations is a list of the following options (--dump by default):

    -d, --dump              Dump nodes using NodeDumper
    -p, --pretty-print      Pretty print file using PrettyPrinter\Standard
    -j, --json-dump         Print json_encode() result
        --var-dump          var_dump() nodes (for exact structure)
    -N, --resolve-names     Resolve names using NodeVisitor\NameResolver
    -c, --with-column-info  Show column numbers for errors (if available)
    -P, --with-positions    Show positions in node dumps
    -r, --with-recovery     Use parsing with error recovery
    -h, --help              Display this page

```

### rbastgen

```text
node rbastgen.js --help
Usage:
  -i, --input      The input file or directory (required)
  -o, --output     The output directory (default: '.ast')
  -e, --exclude    The exclusion regex (default: '^(tests?|vendor|spec)')
  -d, --debug      Enable debug logging
      --version    Print the version
      --help       Print usage
```

### scalasem

```text
scalasem <directory> <slices_file>
```

Example:

```shell
scalasem $(pwd) slices.json
```

## License

MIT
