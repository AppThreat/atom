---
title: Check Reachables
---

## Check Reachables

The check-reachable command takes either a package:version or filename:line_number/line_number_range

`check-reachable -i reachable_slice.json -p colors:1.0.0`
`check-reachable -i reachable_slice.json -p @colors/colors:1.0.0`
`check-reachable -i reachable_slice.json -l file:20`
`check-reachable -i reachable_slice.json -l file:20-40`

```
Description:
  Find out if there are hits for a given package:version or file:linenumber in an atom slice.

Usage:
  check-reachable [options]

Options:
  -i, --input-slice=INPUT-SLICE  Slice file
  -p, --pkg=PKG                  Package to search for in the format of <package_name>:<version>
  -l, --location=LOCATION        Filename with line number to search for in the format of <filename>:<linenumber>
  -h, --help                     Display help for the given command. When no command is given display help for the list command.
  -q, --quiet                    Do not output any message.
  -V, --version                  Display this application version.
      --ansi                     Force ANSI output.
      --no-ansi                  Disable ANSI output.
  -n, --no-interaction           Do not ask any interactive question.
  -v|vv|vvv, --verbose           Increase the verbosity of messages: 1 for normal output, 2 for more verbose output and 3 for debug.

Help:
  The check-reachables command checks for reachable flows for a package:version or file:linenumber in an atom slice.
```
