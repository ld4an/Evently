<template>
  <q-page padding>
    <div class="text-h4 q-mb-md">
      Welcome to the Event Management System
    </div>
    <div class="text-subtitle1 q-mb-lg">
      Browse events, register as an attendee, or manage your events as an organizer.
    </div>

    <q-btn
      color="primary"
      label="View Events"
      @click="$router.push({ name: 'events' })"
    />
  </q-page>
</template>
