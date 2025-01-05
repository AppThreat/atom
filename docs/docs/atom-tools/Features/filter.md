---
title: Filter
---

## Filter

The filter command can be run on its own to produce a filtered slice or used before another command
to filter a slice before executing another command against the results.

>**Filters operate on an inclusive-or basis. If you want to operate on an 'and' basis, 
> [chain](#chaining-filter-commands) the filter commands.**

**Mode**

The default mode creates a regular expression from the value given. Fuzzy mode is specified using
the -f option and a number between 0-100 indicating how close the result must be to be a match. 
Note that to exactly match the specified input, you need to either include regex anchors at the 
beginning and end or use -f 100 (to specify a 100% match).

`filter -f 100 --criteria filename=path/to/file/server.ts -i usages.json`

`filter --criteria filename=^path/to/file/server.ts$ -i usages.json`

Regex word boundaries can be used if you only want to be exact about the filename.

`filter --criteria filename=\bserver.ts$ -i usages.json`

This will filter files named server.ts - without the \b, files like ftpserver.ts would also be matched.

>Note: You can search for a file name without including the path if needed and fuzzing ratios will be computed based 
> only on the file name.

##### Chaining filter commands

The filter command can act on itself by specifying an additional filter command as an argument.
This may desirable for certain use cases where one wishes some criteria to be required.

**Example**

`atom-tools filter -i slices.json --criteria filename=myfile -e "filter --criteria resolvedMethod=mymethod,resolvedMethod=mymethod2 convert"`

This would be equivalent to

`if fileName.contains('myfile') and (resolvedMethod.contains('mymethod') or resolvedMethod.contains('mymethod2')):`

##### Available attributes (not case-sensitive):

*For usages slices*
- callName
- fileName
- fullName
- name
- resolvedMethod
- signature

| attribute      | locations searched                                                                                                                                                      | reachables locations                       |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------|
| callName       | objectSlices.usages.argToCalls<br/>objectSlices.usages.invokedCalls<br/>userDefinedTypes.procedures,                                                                    |                                            |
| fileName       | objectSlices<br/>userDefinedTypes                                                                                                               |                                            |                                                                                                                          |
| fullName       | objectSlices                                                                                                                                                            |                                            |
| name           | objectSlices.usages.targetObj<br/>objectSlices.usages.definedBy<br/>userDefinedTypes.fields                                                                             |                                            |
| purl           |                                                                                                                                                                         | reachables.purls<br/>reachables.flows.tags |
| resolvedMethod | objectSlices.usages.targetObj<br/>objectSlices.usages.definedBy<br/>objectSlices.usages.argToCalls<br/>objectSlices.usages.invokedCalls<br/>userDefinedTypes.procedures |                                            |
| signature      | objectSlices                                                                                                                                                            |                                            |                                                                                                                                                         |                      |

#### Searching reachables for package name/version

This option filters reachables to the given package name and version in the format of name:version

`--package mypackage:1.0.0`

#### Criteria syntax

Multiple criteria can be given by using a comma as a separator (no space)

`--criteria [attribute]=[value],[attribute2]=[value],...`

#### Usage

```
Description:
  Filter an atom slice based on specified criteria.

Usage:
  filter [options]

Options:
  -i, --input-slice=INPUT-SLICE  Slice file to filter.
  -c, --criteria=CRITERIA        Filter based on an attribute of the slice. May be a Python regular expression. Please see documentation for syntax.
  -o, --outfile=OUTFILE          File to re-export filtered slice to.
  -f, --fuzz=FUZZ                Minimum percentage to match with the given criteria INSTEAD of using a regex. Must be a number between 0 and 100.
  -e, --execute=EXECUTE          Command to execute after filtering. [default: "export"]
  -h, --help                     Display help for the given command. When no command is given display help for the list command.
  -q, --quiet                    Do not output any message.
  -V, --version                  Display this application version.
      --ansi                     Force ANSI output.
      --no-ansi                  Disable ANSI output.
  -n, --no-interaction           Do not ask any interactive question.
  -v|vv|vvv, --verbose           Increase the verbosity of messages: 1 for normal output, 2 for more verbose output and 3 for debug.

```

#### Examples

_**Filter a query**_

The below will produce endpoints from the server.ts file located within the line number range of
50-70.

`atom-tools filter -i usages.slices.json --criteria fileName=server.ts -e "query-endpoints -l 50-70"`

_**Filter with the convert command.**_

`atom-tools filter -i usages.slices.json --criteria fileName=server.ts -e "convert -f openapi3.0.1 -o openapi_usages.json -t java"`

The above will produce an OpenAPI document based only on slices generated from server.ts.

_**Filter based on another attribute**_
Create a filtered json that only includes slices where the resolved method equals "validateSignup".
Since no command is specified, the filtered slice will only be written to file.

`atom-tools -i usages.slices.json filter --criteria resolvedMethod=validateSignup`

_**Filtering can also be used to exclude. The first example could be changed to exclude server.ts with
the following:**_

`atom-tools filter --criteria fileName!=server.ts usages.slices.json convert -f openapi3.0.1 -o openapi_usages.json -t java `

****_Multiple filter criteria may be included. The following example will produce a filtered slice based
only on server.ts and router.ts slices._****

`atom-tools filter --criteria fileName=server.ts,callName=router.ts usages.slices.json`
