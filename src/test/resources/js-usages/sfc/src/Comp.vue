<template>
  <div class="meta" data-testid="article-body">
    <a href=""><img :src="profile.image" /></a>
    <span class="name">{{ profile.bio }}</span>
    <button class="btn" :class="{ active: isOn }" @click="save(profile)">
      {{ label }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useProfile } from '@/composables/useProfile'
import axios from 'axios'

const { profile } = useProfile()
const label = computed(() => profile.value.bio)
const isOn = computed(() => profile.value.following)

function save(p: { username: string }) {
  return axios.post(`/profiles/${p.username}/follow`)
}
</script>

<style scoped>
.meta { display: flex; }
</style>
