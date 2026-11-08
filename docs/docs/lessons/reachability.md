# Lesson 6: Interprocedural Reachability Slicing (`reachables`)

### Learning Objective

Compute interprocedural paths from tagged untrusted-input sources to sensitive sinks, accelerated by method flow summaries and the Flux engine, with sanitizer boundaries removed.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A pre-compiled `app.atom`** whose taggers have run (Lesson 3) — reachables anchors on tags.
- **Optional**: a validator/sanitizer config (`--validation-config chennai.json`).

### Conceptual Background

Reachable slicing runs interprocedural taint analysis between **source-tagged** nodes (e.g. `framework-input`) and **sink-tagged** nodes (e.g. `framework-output`, or a category like `sql`). It explores the DDG backwards from sinks across call boundaries. Three strategies keep it scalable:

- **Flux engine** (`useFluxEngine`, default on) — the low-allocation reaching-def engine, reducing GC pressure on large graphs.
- **Flow summaries** (on by default as part of the Flux bundle) — precomputed per-method `MethodFlowSummary` facts. If a method cannot propagate taint from a parameter to its return/output, or its return value can carry no taint, the engine prunes traversal into its body. There is no separate flag; `--legacy-dataflow` disables both the Flux engine and summaries.
- **Sanitization** — paths passing through nodes tagged `sanitizer` are dropped from the final result.

Configuration is `ReachablesConfig`:

```scala
case class ReachablesConfig(
  sourceTag: Seq[String],            // --source-tag (default "framework-input")
  sinkTag: Seq[String],              // --sink-tag (default "framework-output")
  sliceDepth: Int,                   // --slice-depth
  includeCryptoFlows: Boolean,       // --include-crypto
  useSummaries: Boolean = true       // follows the Flux bundle (no separate flag)
) extends BaseConfig
```

### Real Commands

Reachable paths from `framework-input` to `sql` sinks in a Python project (summaries are built and used automatically):

```bash
./atom.sh reachables -l python -o app.atom \
  --source-tag "framework-input" --sink-tag "sql" \
  --slice-outfile reachables.json /path/to/python/project
```

### Code Example

The real logic in [ReachableSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/ReachableSlicing.scala) `calculateReachableSliceAndPersist`:

```scala
def calculateReachableSliceAndPersist(
  atom: Cpg,
  config: ReachablesConfig,
  outputBasePath: String,
  chunkSize: Int = DEFAULT_CHUNK_SIZE
): Unit =
  val summaries =
    if config.useSummaries then
      // Prefer the `flow-summary` tags embedded in the atom during enhancement; fall back to the
      // JSON sidecar when the atom carries none.
      val tagged = FlowSummaryTags.fromCpg(atom)
      if tagged.nonEmpty then tagged
      else
        val cacheDir = Option(new JFile(outputBasePath).getAbsoluteFile.getParent).getOrElse(".")
        FlowSummaryComputer.loadOrCompute(atom, cacheDir, semantics)
    else Map.empty

  context = EngineContext(
    semantics,
    EngineConfig(
      useSummaries = config.useSummaries,
      summaries    = summaries
    )
  )

  val flowIterator = collectFlowSlices(atom, config, language)
    .iterator.flatten
    .filterNot(isSanitized(_, sanitizerCalls))   // drop paths through sanitizers
    .filter(isUniqueFlow(_, seenSignatures, seenEndpoints))
    .map(toSlice)

  persistChunks(flowIterator, outputBasePath, chunkSize)  // chunked JSON output
```

`FlowSummaryComputer.loadOrCompute` reads cached summaries when present, otherwise computes them callee-before-caller over call-graph SCCs. Each emitted `ReachableFlows` carries the ordered `flows` plus the `purls` of packages the flow passes through, enabling SBOM/reachability correlation.

### Try It: Request Values a Client Can Choose Through Forwarded Headers

Behind a reverse proxy that passes the `Forwarded` and `X-Forwarded-*` headers through unchanged,
the client IP, host and scheme a framework reports can be chosen by the client. The taggers mark
those accessors with `proxy-derived` (value `client-ip`, `host` or `scheme`), the configuration
that turns on forwarded-header handling with `trusted-proxy-config`, and direct reads of the
headers by name with `forwarded-header-read`. These tags are not among the default source tags:
pass `--source-tag proxy-derived` to ask for the flows that start at them. A `proxy-derived` value is only
spoofable behind a proxy that passes the headers through; the tag names where the application
reads the value, not that the deployment is vulnerable.

This Express handler trusts the proxy (`app.set('trust proxy', 1)`), reads `req.ip` and passes it
to a shell:

```js
app.get('/whoami', (req, res) => {
  const ip = req.ip;
  exec('ping -c 1 ' + ip, (err, stdout) => res.send(stdout));
});
```

Find the flow from the forwarded-header-derived client IP to the command execution:

<!-- command: forwarded-headers -->
```bash
atom reachables -l js --source-tag proxy-derived \
  -o /tmp/ex-forwarded-headers/app.atom -s /tmp/ex-forwarded-headers/reach.json \
  /tmp/ex-forwarded-headers/input
```

Every record of the excerpt below is in the real output (node ids differ between runs; the excerpt
omits them):

<!-- output: forwarded-headers -->
```json
{"label": "CALL", "name": "<operator>.fieldAccess", "code": "req.ip", "parentFileName": "server.js", "lineNumber": 10, "tags": "framework-input, proxy-derived"}
{"label": "CALL", "name": "exec", "fullName": "child_process", "code": "exec('ping -c 1 ' + ip, (err, stdout) => {\n    res.send(stdout);\n  })", "parentFileName": "server.js", "lineNumber": 11, "tags": "shell-exec, node:child_process"}
```

The first record is the source: `req.ip`, carrying `proxy-derived` (its value, `client-ip`, rides
on the tag pair in the atom). The second is the sink: `exec` from `child_process`, tagged
`shell-exec`. `req.ip` also carries `framework-input`, because it reads the handler's request
parameter, so a default run reports this flow as well. `--source-tag proxy-derived` narrows the
run to the flows that start at a value a forwarded header can set, which is the question this
example asks.
