---
title: Query Endpoints
---

## Query Endpoints
Query endpoints generates a list of endpoints and returns the output directly to the console.

>Note: To suppress logging messages and ONLY output the results, use --quiet/-q

**_Examples_**

Query returning all endpoints, including filenames and line numbers

`query-endpoints -i usages.slices -t js`

Query returning all endpoints without filenames and line numbers

`query-endpoints --sparse -i usages.slices -t js`

Query filtering by line number or line number range

`query-endpoints -i usages.slices -t js -f 50`

`query-endpoints -i usages.slices -t js -f 50-70`

Query using filter command to target by both filename and line number range

`filter -i usages.slices -t js -c filename=server.ts -e "query-endpoints -f 50-70"`
