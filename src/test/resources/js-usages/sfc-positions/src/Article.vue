<template>
  <form @submit.prevent="submit">
    <input :disabled="busy" type="text">
    <hr>
    <Suspense>
      <template #fallback>
        <p>{{ fallbackText }}</p>
      </template>
    </Suspense>
    <time>{{ dayjs(published).format('YYYY') }}</time>
  </form>
</template>

<script setup lang="ts">
import axios from 'axios'
import dayjs from 'dayjs'

const busy = false
const fallbackText = 'loading'
const published = '2026-01-01'

function submit() {
  return axios.post('/api/article', { published })
}
</script>
