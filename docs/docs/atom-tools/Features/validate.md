---
title: Validate Lines
---

## Validate Lines

The validate-lines command checks the accuracy of the line numbers reported by
atom against your source files.

```
Description:
  Check the accuracy of the line numbers in an atom slice.

Usage:
  validate-lines [options]

Options:
  -i, --input-slice=INPUT-SLICE  Slice file to validate. [default: "slices.json"]
  -t, --type=TYPE                Origin type of source on which the atom slice was generated. [default: "java"]
  -d, --base-path=BASE-PATH      This should be the same path that was used by atom when the slice was generated.
  -l, --interval=INTERVAL        Try matching within a range. Ex. slice has line number 567, with interval of 5, we check lines 562-572. Use 0 for exact matching. [default: 5]
  -r, --report=REPORT            Output summary to file.  [default: "output.txt"]
  -j, --export-json=EXPORT-JSON  JSON report file to store invalid lines. Include valid lines as well using -v flag.
  -h, --help                     Display help for the given command. When no command is given display help for the list command.
  -q, --quiet                    Do not output any message.
  -V, --version                  Display this application version.
      --ansi                     Force ANSI output.
      --no-ansi                  Disable ANSI output.
  -n, --no-interaction           Do not ask any interactive question.
  -v|vv|vvv, --verbose           Increase the verbosity of messages: 1 for normal output, 2 for more verbose output and 3 for debug.
  
Help:
  Validate source file line numbers in an atom usages or reachables slice.
```

**Example**
> `atom-tools validate-lines -t java -j project_json_report.json -i usages.slices.json -d /home/my_project_dir`
