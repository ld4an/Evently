<template>
  <q-page padding class="flex flex-center">
    <div class="column items-center">
      <div class="text-h4 q-mb-sm">403 – Forbidden</div>
      <div class="text-subtitle2 q-mb-md">
        You don’t have permission to access this page.
      </div>
      <q-btn color="primary" label="Go home" @click="$router.push({ name: 'home' })" />
    </div>
  </q-page>
</template>
