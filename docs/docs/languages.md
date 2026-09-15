---
sidebar_position: 7
title: Languages supported
---

# Languages supported

- C/C++
- H (C/C++ Header and pre-processed .i files alone)
- Java (Requires compilation)
- Jar
- Android APK (Requires Android SDK. Set the environment variable `ANDROID_HOME` or use the container image.)
- JavaScript
- Flow
- TypeScript
- JavaScript frameworks: single-file components for Vue (`.vue`) and Svelte /
  SvelteKit (`.svelte`) are parsed into the JavaScript CPG. The Svelte template
  is modelled as JSX-equivalent structure (elements, attributes, expression
  containers), not as Svelte semantics: `{#if}`/`{#each}` blocks appear as
  conditional/`.map()` calls, and directive attributes such as `on:click={h}`
  appear as template DOM attributes with their exact source text. `{@html}`
  expressions are present as template expression containers, but note the
  general JSX caveat below. `<style>` blocks and HTML comments are not analysed.
- SvelteKit routes: `load` and `actions` in `+page.server.*`/`+layout.server.*`,
  the HTTP verb exports in `+server.*`, and the `hooks.server.*` handlers are
  recognised as framework entrypoints, with their request parameters treated as
  web-facing input. `$props()` in a component is treated as the component's input
  boundary, which closes props-to-`{@html}` paths inside a component.
- Python (Supports 3.x to 3.14)
- PHP (Requires PHP >= 7.4. Supports PHP 7.0 to 8.5 with limited support for PHP 5.x)
- Ruby (Requires a Ruby runtime with the `rbastgen` generator; the grammar is chosen by
  capability, not by the runtime version, so Ruby 1.8 - 4.0.x syntax is supported on any
  supported runtime)
- Scala (WIP)

## Template dataflow

Interpolated template expressions participate in dataflow: each is wrapped in an
`<operator>.interpolation` call, so the rendered value is a call argument the dataflow
engine can reach. This covers React JSX, Vue and Svelte alike - previously a bare
`<div>{bio}</div>` or `{@html bio}` had no reaching definition and was unreachable.

Raw-HTML sinks - Svelte `{@html}`, Vue `v-html`, React `dangerouslySetInnerHTML` - are
tagged `framework-output` at both the template node (an inventory of render sites) and the
interpolated expression (what a flow terminates on), so `atom reachables` reports the path
ending at the markup:

```text
+page.svelte L5   $props()       [framework-input]
+page.svelte L24  data.article   [framework-output]     <- {@html data.article.body}
```

Svelte blocks and tags are named after the construct in the graph - `SvelteHtmlTag`,
`SvelteIfBlock`, `SvelteEachBlock`, and so on - since they all share one JSX carrier:

```scala
cpg.templateDom.nameExact("SvelteHtmlTag").code.l
```

One reporting caveat: a flow whose source and sink land on the same line is suppressed as
zero-information, so the rendered value has to pass through something on another line to be
reported.
