<template>
  <q-page padding>
    <div class="row items-center justify-between q-mb-lg">
      <div>
        <div class="text-h4 text-weight-bold text-primary">Admin Dashboard</div>
        <div class="text-subtitle1 text-grey-7">Oversee all events and keep the platform healthy</div>
      </div>
      <div class="row q-gutter-sm">
        <q-btn color="primary" flat icon="refresh" label="Refresh" @click="fetchEvents" :loading="loading" />
        <q-btn color="secondary" unelevated icon="visibility" label="Go to organizer view" @click="router.push({ name: 'organizer-dashboard' })" />
      </div>
    </div>

    <div class="row q-col-gutter-md q-mb-xl">
      <div class="col-12 col-md-4">
        <q-card class="bg-primary text-white">
          <q-card-section>
            <div class="text-h6">Total Events</div>
            <div class="text-h3">{{ events.length }}</div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-md-4">
        <q-card class="bg-secondary text-white">
          <q-card-section>
            <div class="text-h6">Unique Organizers</div>
            <div class="text-h3">{{ organizerCount }}</div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-md-4">
        <q-card class="bg-positive text-white">
          <q-card-section>
            <div class="text-h6">Upcoming (next 30d)</div>
            <div class="text-h3">{{ upcomingCount }}</div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <div class="text-h5 q-mb-md">Manage Events</div>
    
    <q-table
      title="All Events"
      :rows="events"
      :columns="columns"
      row-key="id"
      :loading="loading"
    >
      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat round color="primary" icon="visibility" @click="router.push({ name: 'event-details', params: { id: props.row.id } })" />
          <q-btn flat round color="warning" icon="edit" @click="router.push({ name: 'organizer-event-edit', params: { id: props.row.id } })" />
          <q-btn flat round color="negative" icon="delete" @click="confirmDelete(props.row)" />
        </q-td>
      </template>
    </q-table>

    <q-dialog v-model="showDeleteDialog">
      <q-card>
        <q-card-section class="row items-center">
          <q-avatar icon="warning" color="negative" text-color="white" />
          <span class="q-ml-sm">Are you sure you want to delete event "{{ eventToDelete?.name }}"?</span>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancel" color="primary" v-close-popup />
          <q-btn flat label="Delete" color="negative" @click="deleteEvent" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import type { QTableColumn } from 'quasar';
import api from 'src/services/api';

const router = useRouter();
const $q = useQuasar();

interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
  organizer?: { name: string };
}

const events = ref<Event[]>([]);
const loading = ref(true);
const showDeleteDialog = ref(false);
const eventToDelete = ref<Event | null>(null);
const organizerCount = computed(() => new Set(events.value.map((e) => e.organizer?.name || '')).size);
const upcomingCount = computed(() =>
  events.value.filter((e) => {
    const d = new Date(e.date);
    const now = new Date();
    const inThirty = new Date();
    inThirty.setDate(now.getDate() + 30);
    return d >= now && d <= inThirty;
  }).length
);

const columns: QTableColumn[] = [
  { name: 'id', label: 'ID', field: 'id', sortable: true, align: 'left' },
  { name: 'name', label: 'Name', field: 'name', sortable: true, align: 'left' },
  { name: 'date', label: 'Date', field: (row: Event) => new Date(row.date).toLocaleDateString(), sortable: true, align: 'left' },
  { name: 'location', label: 'Location', field: 'location', sortable: true, align: 'left' },
  { name: 'organizer', label: 'Organizer', field: (row: Event) => row.organizer?.name || 'Unknown', sortable: true, align: 'left' },
  { name: 'actions', label: 'Actions', field: 'actions', align: 'center' },
];

onMounted(async () => {
  await fetchEvents();
});

async function fetchEvents() {
  loading.value = true;
  try {
    const response = await api.get('/events');
    events.value = response.data;
  } catch (error) {
    console.error('Failed to fetch events', error);
    $q.notify({ type: 'negative', message: 'Failed to load events' });
  } finally {
    loading.value = false;
  }
}

function confirmDelete(event: Event) {
  eventToDelete.value = event;
  showDeleteDialog.value = true;
}

async function deleteEvent() {
  if (!eventToDelete.value) return;
  try {
    await api.delete(`/events/${eventToDelete.value.id}`);
    $q.notify({ type: 'positive', message: 'Event deleted successfully' });
    await fetchEvents();
  } catch (error) {
    console.error(error);
    $q.notify({ type: 'negative', message: 'Failed to delete event' });
  }
}
</script>
