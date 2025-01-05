---
sidebar_position: 3
title: CLI Usage
---

## CLI Usage

Atom-tools uses py-poetry/cleo to construct its command-line interface and therefore uses the same
sorts of conventions as the Python package management utility poetry.

To access the commands help menu, enter `atom-tools list` for a list of available commands.

Individual command options can be accessed with `atom-tools help ` and the command name (
e.g. `atom-tools help
convert`).

```
Atom Tools (version 0.6.0)

Usage:
  command [options] [arguments]

Options:
  -h, --help            Display help for the given command. When no command is given display help for the list command.
  -q, --quiet           Do not output any message.
  -V, --version         Display this application version.
      --ansi            Force ANSI output.
      --no-ansi         Disable ANSI output.
  -n, --no-interaction  Do not ask any interactive question.
  -v|vv|vvv, --verbose  Increase the verbosity of messages: 1 for normal output, 2 for more verbose output and 3 for debug.

Available commands:
  check-reachable  Find out if there are hits for a given package:version or file:linenumber in an atom slice.
  convert          Convert an atom slice to a different format.
  filter           Filter an atom slice based on specified criteria.
  help             Displays help for a command.
  list             Lists commands.
  query-endpoints  List elements to display in the console.
  validate-lines   Check the accuracy of the line numbers in an atom slice.
```