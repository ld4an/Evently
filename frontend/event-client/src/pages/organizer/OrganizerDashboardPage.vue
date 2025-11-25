<template>
  <q-page padding>
    <!-- Header -->
    <div class="row items-center justify-between q-mb-lg">
      <div>
        <div class="text-h4 text-weight-bold text-primary">Organizer Dashboard</div>
        <div class="text-subtitle1 text-grey-7">
          Overview of your events and attendees
        </div>
      </div>

      <q-btn
        color="primary"
        unelevated
        icon="add"
        label="Create New Event"
        @click="router.push({ name: 'organizer-event-new' })"
      />
    </div>

    <!-- Stats cards -->
    <div class="row q-col-gutter-md q-mb-lg">
      <div class="col-12 col-md-4">
        <q-card class="shadow-2">
          <q-card-section>
            <div class="text-caption text-grey-7">Total Events</div>
            <div class="text-h4 text-weight-bold">{{ totalEvents }}</div>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-4">
        <q-card class="shadow-2">
          <q-card-section>
            <div class="text-caption text-grey-7">Upcoming Events</div>
            <div class="text-h4 text-weight-bold">{{ upcomingEvents }}</div>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-4">
        <q-card class="shadow-2">
          <q-card-section>
            <div class="text-caption text-grey-7">Total Attendees</div>
            <div class="text-h4 text-weight-bold">{{ totalAttendees }}</div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- Loading / empty states -->
    <div v-if="loading" class="row justify-center q-my-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>

    <div v-else-if="!isOrganizerOrAdmin" class="text-center q-my-xl text-grey-6">
      <q-icon name="lock" size="4rem" />
      <div class="text-h6 q-mt-sm">Organizer access required.</div>
      <div class="text-caption q-mt-xs">Please log in with an organizer account.</div>
    </div>

    <div v-else-if="events.length === 0" class="text-center q-my-xl text-grey-6">
      <q-icon name="event_busy" size="4rem" />
      <div class="text-h6 q-mt-sm">You haven't created any events yet.</div>
      <q-btn
        color="primary"
        flat
        label="Create your first event"
        class="q-mt-sm"
        @click="router.push({ name: 'organizer-event-new' })"
      />
    </div>

    <!-- Events table -->
    <div v-else>
      <div class="text-h6 q-mb-md">Your Events</div>

      <q-table
        flat
        bordered
        :rows="events"
        :columns="columns"
        row-key="id"
        :rows-per-page-options="[5, 10, 20]"
      >
        <template #body-cell-date="props">
          <q-td :props="props">
            {{ formatDate(props.row.date) }}
          </q-td>
        </template>

        <template #body-cell-attendees="props">
          <q-td :props="props">
            {{ props.row.attendees?.length ?? 0 }}
          </q-td>
        </template>

        <template #body-cell-actions="props">
          <q-td :props="props" class="q-gutter-xs">
            <q-btn
              dense
              flat
              round
              icon="visibility"
              @click="router.push({ name: 'event-details', params: { id: props.row.id } })"
            >
              <q-tooltip>View details</q-tooltip>
            </q-btn>

            <q-btn
              dense
              flat
              round
              color="primary"
              icon="edit"
              @click="router.push({ name: 'organizer-event-edit', params: { id: props.row.id } })"
            >
              <q-tooltip>Edit</q-tooltip>
            </q-btn>

            <q-btn
              dense
              flat
              round
              color="secondary"
              icon="group"
              @click="router.push({ name: 'organizer-event-attendees', params: { id: props.row.id } })"
            >
              <q-tooltip>View attendees</q-tooltip>
            </q-btn>
          </q-td>
        </template>
      </q-table>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import type { QTableColumn } from 'quasar';
import api from 'src/services/api';
import { useAuthStore } from 'src/stores/auth-store';

interface Attendee {
  id: number;
}

interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
  maxAttendees?: number;
  attendees?: Attendee[];
}

const router = useRouter();
const $q = useQuasar();
const auth = useAuthStore();

const events = ref<Event[]>([]);
const loading = ref(true);
const isOrganizerOrAdmin = computed(() => auth.role === 'ORGANIZER' || auth.role === 'ADMIN');

const totalEvents = computed(() => events.value.length);
const upcomingEvents = computed(() =>
  events.value.filter(e => new Date(e.date) >= new Date()).length
);
const totalAttendees = computed(() =>
  events.value.reduce((sum, e) => sum + (e.attendees?.length ?? 0), 0)
);

// Properly typed QTable columns
const columns: QTableColumn<Event>[] = [
  {
    name: 'name',
    label: 'Name',
    field: 'name',
    align: 'left',
    sortable: true
  },
  {
    name: 'date',
    label: 'Date',
    field: 'date',
    align: 'left',
    sortable: true
  },
  {
    name: 'location',
    label: 'Location',
    field: 'location',
    align: 'left',
    sortable: true
  },
  {
    name: 'attendees',
    label: 'Attendees',
    field: (row: Event) => row.attendees?.length ?? 0,
    align: 'center',
    sortable: true
  },
  {
    name: 'actions',
    label: 'Actions',
    field: 'id',
    align: 'right',
    sortable: false
  }
];

function formatDate(value: string) {
  const d = new Date(value);
  return isNaN(d.getTime()) ? '-' : d.toLocaleString();
}

onMounted(async () => {
  if (!isOrganizerOrAdmin.value) {
    loading.value = false;
    $q.notify({ type: 'warning', message: 'Organizer access required' });
    return;
  }
  try {
    // adjust this endpoint if needed to match backend
    const response = await api.get('/me/events');
    events.value = response.data;
  } catch (error) {
    console.error('Failed to fetch my events', error);
    $q.notify({ type: 'negative', message: 'Failed to load your events' });
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
</style>
