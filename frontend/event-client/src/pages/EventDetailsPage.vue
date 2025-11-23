<template>
  <q-page padding>
    <div class="text-h5 q-mb-md">Event Details</div>
    <div class="text-caption q-mb-md">
      This page will show details for event with ID: {{ $route.params.id }}
    </div>
    <q-btn label="Back to events" flat @click="$router.push({ name: 'events' })" />
  </q-page>
</template>
