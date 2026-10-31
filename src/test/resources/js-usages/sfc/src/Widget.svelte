<script lang="ts">
import { derived } from 'svelte/store'
import { page } from '$app/stores'

export const title = derived(page, (p) => p.data.title)
</script>

<h1>{title}</h1>
<a href={page}>open</a>
