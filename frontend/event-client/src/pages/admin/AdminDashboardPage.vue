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

    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="text-subtitle1 text-weight-bold q-mb-sm">Create user / admin account</div>
        <div class="text-caption text-grey-7 q-mb-md">Admins can bootstrap new accounts. Organizers will get an organizer profile automatically.</div>
        <q-form @submit.prevent="createAccount" class="row q-col-gutter-md">
          <div class="col-12 col-md-4">
            <q-input v-model="newUser.email" label="Email" type="email" outlined dense />
          </div>
          <div class="col-12 col-md-4">
            <q-input v-model="newUser.password" label="Password" type="password" outlined dense />
          </div>
          <div class="col-12 col-md-3">
            <q-select
              v-model="newUser.role"
              :options="roleOptions"
              label="Role"
              outlined
              dense
              emit-value
              map-options
            />
          </div>
          <div class="col-12 col-md-1 flex items-end">
            <q-btn color="primary" unelevated type="submit" label="Add" :loading="creatingAccount" />
          </div>
        </q-form>
      </q-card-section>
    </q-card>
    
    <q-table
      title="All Events"
      :rows="events"
      :columns="columns"
      row-key="id"
      :loading="loading"
    >
      <template v-slot:body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat round color="info" icon="group" @click="openAttendees(props.row)" />
          <q-btn flat round color="primary" icon="visibility" @click="router.push({ name: 'event-details', params: { id: props.row.id } })" />
          <q-btn flat round color="warning" icon="edit" @click="router.push({ name: 'organizer-event-edit', params: { id: props.row.id } })" />
          <q-btn flat round color="negative" icon="delete" @click="confirmDelete(props.row)" />
        </q-td>
      </template>
    </q-table>

    <q-dialog v-model="showAttendeesDialog" persistent maximized>
      <q-card>
        <q-bar class="bg-primary text-white">
          <div class="text-subtitle1">{{ activeEvent?.name || 'Event' }} — Attendees</div>
          <q-space />
          <q-btn dense flat icon="close" v-close-popup />
        </q-bar>
        <q-card-section>
          <div class="text-caption text-grey-7 q-mb-md">
            Manage attendees for this event. You can remove or ban (marks as banned and removes from the event).
          </div>
          <q-table
            :rows="attendees"
            :columns="attendeeColumns"
            row-key="id"
            :loading="attendeesLoading"
            flat
            bordered
          >
            <template v-slot:body-cell-actions="props">
              <q-td :props="props">
                <q-btn dense flat round icon="logout" color="warning" @click="removeAttendee(props.row)" :loading="actionLoadingId === props.row.id && actionType === 'remove'" />
                <q-btn dense flat round icon="block" color="negative" @click="banAttendee(props.row)" :loading="actionLoadingId === props.row.id && actionType === 'ban'" />
                <q-btn
                  v-if="props.row.status === 'BANNED'"
                  dense
                  flat
                  round
                  icon="lock_open"
                  color="positive"
                  @click="unbanAttendee(props.row)"
                  :loading="actionLoadingId === props.row.id && actionType === 'unban'"
                />
              </q-td>
            </template>
          </q-table>
        </q-card-section>
      </q-card>
    </q-dialog>

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
import { isAxiosError } from 'axios';
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

interface Attendee {
  id: number;
  name?: string;
  email: string;
  status: string;
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
const newUser = ref({ email: '', password: '', role: 'ATTENDEE' });
const creatingAccount = ref(false);
const roleOptions = [
  { label: 'Attendee', value: 'ATTENDEE' },
  { label: 'Organizer', value: 'ORGANIZER' },
  { label: 'Admin', value: 'ADMIN' },
];

const columns: QTableColumn[] = [
  { name: 'id', label: 'ID', field: 'id', sortable: true, align: 'left' },
  { name: 'name', label: 'Name', field: 'name', sortable: true, align: 'left' },
  { name: 'date', label: 'Date', field: (row: Event) => new Date(row.date).toLocaleDateString(), sortable: true, align: 'left' },
  { name: 'location', label: 'Location', field: 'location', sortable: true, align: 'left' },
  { name: 'organizer', label: 'Organizer', field: (row: Event) => row.organizer?.name || 'Unknown', sortable: true, align: 'left' },
  { name: 'actions', label: 'Actions', field: 'actions', align: 'center' },
];

const attendeeColumns: QTableColumn[] = [
  { name: 'id', label: 'ID', field: 'id', align: 'left' },
  { name: 'name', label: 'Name', field: (row: Attendee) => row.name || '—', align: 'left' },
  { name: 'email', label: 'Email', field: 'email', align: 'left' },
  { name: 'status', label: 'Status', field: 'status', align: 'left' },
  { name: 'actions', label: 'Actions', field: 'actions', align: 'center' },
];

const attendees = ref<Attendee[]>([]);
const attendeesLoading = ref(false);
const showAttendeesDialog = ref(false);
const activeEvent = ref<Event | null>(null);
const actionLoadingId = ref<number | null>(null);
const actionType = ref<'remove' | 'ban' | 'unban' | null>(null);

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

async function openAttendees(event: Event) {
  activeEvent.value = event;
  showAttendeesDialog.value = true;
  attendeesLoading.value = true;
  try {
    const response = await api.get(`/events/${event.id}/attendees`);
    attendees.value = response.data;
  } catch (error) {
    console.error('Failed to fetch attendees', error);
    $q.notify({ type: 'negative', message: 'Failed to load attendees' });
  } finally {
    attendeesLoading.value = false;
  }
}

async function removeAttendee(attendee: Attendee) {
  if (!attendee?.id) return;
  actionLoadingId.value = attendee.id;
  actionType.value = 'remove';
  try {
    await api.put(`/attendees/${attendee.id}/remove-event`);
    $q.notify({ type: 'positive', message: 'Attendee removed from event' });
    if (activeEvent.value) {
      await openAttendees(activeEvent.value);
    }
  } catch (error) {
    console.error('Failed to remove attendee', error);
    $q.notify({ type: 'negative', message: 'Failed to remove attendee' });
  } finally {
    actionLoadingId.value = null;
    actionType.value = null;
  }
}

async function banAttendee(attendee: Attendee) {
  if (!attendee?.id) return;
  actionLoadingId.value = attendee.id;
  actionType.value = 'ban';
  try {
    await api.put(`/attendees/${attendee.id}/ban`);
    $q.notify({ type: 'positive', message: 'Attendee banned' });
    if (activeEvent.value) {
      await openAttendees(activeEvent.value);
    }
  } catch (error) {
    console.error('Failed to ban attendee', error);
    let message = 'Failed to ban attendee';
    if (isAxiosError(error)) {
      const data = error.response?.data as { message?: string; error?: string } | undefined;
      message = data?.message || data?.error || message;
    } else if (error instanceof Error) {
      message = error.message;
    }
    $q.notify({ type: 'negative', message });
  } finally {
    actionLoadingId.value = null;
    actionType.value = null;
  }
}

async function unbanAttendee(attendee: Attendee) {
  if (!attendee?.id) return;
  actionLoadingId.value = attendee.id;
  actionType.value = 'unban';
  try {
    await api.put(`/attendees/${attendee.id}/unban`);
    $q.notify({ type: 'positive', message: 'Attendee unbanned' });
    if (activeEvent.value) {
      await openAttendees(activeEvent.value);
    }
  } catch (error) {
    console.error('Failed to unban attendee', error);
    let message = 'Failed to unban attendee';
    if (isAxiosError(error)) {
      const data = error.response?.data as { message?: string; error?: string } | undefined;
      message = data?.message || data?.error || message;
    } else if (error instanceof Error) {
      message = error.message;
    }
    $q.notify({ type: 'negative', message });
  } finally {
    actionLoadingId.value = null;
    actionType.value = null;
  }
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

async function createAccount() {
  creatingAccount.value = true;
  try {
    await api.post('/auth/register', {
      email: newUser.value.email,
      password: newUser.value.password,
      role: newUser.value.role,
    });
    $q.notify({ type: 'positive', message: 'Account created' });
    newUser.value = { email: '', password: '', role: 'ATTENDEE' };
  } catch (error) {
    console.error(error);
    $q.notify({ type: 'negative', message: 'Failed to create account' });
  } finally {
    creatingAccount.value = false;
  }
}
</script>
