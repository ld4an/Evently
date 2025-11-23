<template>
  <q-page padding>
    <div class="text-h5 q-mb-md">
      {{ isEdit ? 'Edit Event' : 'Create Event' }}
    </div>
    <div class="text-caption q-mb-md">
      Later we’ll add a full form here.
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isEdit = computed(() => !!route.params.id);
</script>
