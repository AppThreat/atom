---
title: Convert
---

## Convert

The convert command can be used to output an atom slice in a different format. The current
capabilities are limited to processing usages in order to generate endpoints for an openapi 3.x
paths object. Future iterations will populate the path item objects with more details based on
atom slices.

```
Description:
  Convert an atom slice to a different format

Usage:
  convert [options]

Options:
  -f, --format=FORMAT              Destination format [default: "openapi3.0.1"]
  -i, --input-slice=INPUT-SLICE  Usages slice file
  -t, --type=TYPE                  Origin type of source on which the atom slice was generated. [default: "java"]
  -o, --output-file=OUTPUT-FILE    Output file [default: "openapi_from_slice.json"]
  -s, --server=SERVER              The server url to be included in the server object.
  -h, --help                       Display help for the given command. When no command is given display help for the list command.
  -q, --quiet                      Do not output any message.
  -V, --version                    Display this application version.
      --ansi                       Force ANSI output.
      --no-ansi                    Disable ANSI output.
  -n, --no-interaction             Do not ask any interactive question.
  -v|vv|vvv, --verbose             Increase the verbosity of messages: 1 for normal output, 2 for more verbose output and 3 for debug.

Help:
  The convert command converts an atom slice to a different format.
      Currently supports outputting an OpenAPI 3.x document based on a usages
      slice.
```

**Example**

> `atom-tools convert -i usages.slices.json -f openapi3.0.1 -o openapi_usages.json -t java -s https://myserver.com`

