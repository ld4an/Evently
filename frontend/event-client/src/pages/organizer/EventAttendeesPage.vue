<template>
  <q-page padding>
    <div class="row items-center q-mb-lg">
      <q-btn
        flat
        icon="arrow_back"
        label="Back to Dashboard"
        color="primary"
        @click="router.push({ name: 'organizer-dashboard' })"
      />
    </div>
    <div v-if="loading" class="row justify-center q-my-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>
    <div v-else-if="!event" class="text-center q-my-xl text-grey-6">
      <q-icon name="error_outline" size="4rem" />
      <div class="text-h6 q-mt-sm">Event not found</div>
    </div>
    <div v-else>
      <div class="text-h4 text-weight-bold text-primary q-mb-sm">{{ event.name }}</div>
      <div class="text-subtitle1 text-grey-7 q-mb-lg">
        <q-icon name="place" /> {{ event.location }} • 
        <q-icon name="event" /> {{ new Date(event.date).toLocaleDateString() }}
      </div>
      <!-- Stats Cards -->
      <div class="row q-col-gutter-md q-mb-xl">
        <div class="col-12 col-md-4">
          <q-card flat bordered>
            <q-card-section>
              <div class="text-h6">Total Requests</div>
              <div class="text-h3 text-primary">{{ attendees.length }}</div>
            </q-card-section>
          </q-card>
        </div>
        <div class="col-12 col-md-4">
          <q-card flat bordered class="bg-positive text-white">
            <q-card-section>
              <div class="text-h6">Approved</div>
              <div class="text-h3">{{ approvedCount }}</div>
            </q-card-section>
          </q-card>
        </div>
        <div class="col-12 col-md-4">
          <q-card flat bordered class="bg-warning text-white">
            <q-card-section>
              <div class="text-h6">Pending</div>
              <div class="text-h3">{{ pendingCount }}</div>
            </q-card-section>
          </q-card>
        </div>
      </div>
      <!-- Attendees Table -->
      <q-card class="shadow-2">
        <q-card-section>
          <div class="text-h5 q-mb-md">Attendee Requests</div>
          
          <q-table
            :rows="attendees"
            :columns="columns"
            row-key="id"
            :loading="loading"
            flat
            bordered
          >
            <template v-slot:body-cell-status="props">
              <q-td :props="props">
                <q-badge
                  :color="getStatusColor(props.row.status)"
                  :label="props.row.status"
                />
              </q-td>
            </template>
            <template v-slot:body-cell-actions="props">
              <q-td :props="props">
                <template v-if="props.row.status === 'PENDING'">
                  <q-btn
                    flat
                    round
                    color="positive"
                    icon="check"
                    :loading="processingIds.has(props.row.id)"
                    :disable="processingIds.has(props.row.id)"
                    @click="approveAttendee(props.row)"
                  >
                    <q-tooltip>Approve</q-tooltip>
                  </q-btn>
                  <q-btn
                    flat
                    round
                    color="negative"
                    icon="close"
                    :loading="processingIds.has(props.row.id)"
                    :disable="processingIds.has(props.row.id)"
                    @click="rejectAttendee(props.row)"
                  >
                    <q-tooltip>Reject</q-tooltip>
                  </q-btn>
                </template>
                <span v-else class="text-grey-6">-</span>
              </q-td>
            </template>
          </q-table>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import type { QTableColumn } from 'quasar';
import api from 'src/services/api';
const route = useRoute();
const router = useRouter();
const $q = useQuasar();
interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
}
interface Attendee {
  id: number;
  name: string;
  email: string;
  status: string;
}
const event = ref<Event | null>(null);
const attendees = ref<Attendee[]>([]);
const loading = ref(true);
const processingIds = ref<Set<number>>(new Set());
const eventId = Number(route.params.id);
const columns: QTableColumn[] = [
  { name: 'name', label: 'Name', field: 'name', align: 'left', sortable: true },
  { name: 'email', label: 'Email', field: 'email', align: 'left', sortable: true },
  { name: 'status', label: 'Status', field: 'status', align: 'left', sortable: true },
  { name: 'actions', label: 'Actions', field: 'actions', align: 'center' },
];
const approvedCount = computed(() => attendees.value.filter(a => a.status === 'APPROVED').length);
const pendingCount = computed(() => attendees.value.filter(a => a.status === 'PENDING').length);
onMounted(async () => {
  await Promise.all([fetchEvent(), fetchAttendees()]);
  loading.value = false;
});
async function fetchEvent() {
  try {
    const response = await api.get(`/events/${eventId}`);
    event.value = response.data;
  } catch (error) {
    console.error('Failed to fetch event', error);
    $q.notify({ type: 'negative', message: 'Failed to load event' });
  }
}
async function fetchAttendees() {
  try {
    const response = await api.get(`/events/${eventId}/attendees`);
    attendees.value = response.data;
  } catch (error) {
    console.error('Failed to fetch attendees', error);
    $q.notify({ type: 'negative', message: 'Failed to load attendees' });
  }
}
async function approveAttendee(attendee: Attendee) {
  // Add to processing set to show loading state
  processingIds.value.add(attendee.id);
  
  // Optimistic update: immediately update the UI
  const attendeeItem = attendees.value.find(a => a.id === attendee.id);
  if (attendeeItem) {
    attendeeItem.status = 'APPROVED';
  }
  
  try {
    await api.post(`/events/${eventId}/requests/${attendee.id}/approve`);
    $q.notify({ type: 'positive', message: `Approved ${attendee.name || attendee.email}` });
    // Refresh the list immediately
    // Refresh the list to ensure consistency with backend
    await fetchAttendees();
  } catch (error) {
    console.error(error);
    // Revert the optimistic update on error
    if (attendeeItem) {
      attendeeItem.status = 'PENDING';
    }
    $q.notify({ type: 'negative', message: 'Failed to approve attendee' });
  } finally {
    processingIds.value.delete(attendee.id);
  }
}
async function rejectAttendee(attendee: Attendee) {
  // Add to processing set to show loading state
  processingIds.value.add(attendee.id);
  
  // Optimistic update: immediately update the UI
  const attendeeItem = attendees.value.find(a => a.id === attendee.id);
  if (attendeeItem) {
    attendeeItem.status = 'REJECTED';
  }
  
  try {
    await api.post(`/events/${eventId}/requests/${attendee.id}/reject`);
    $q.notify({ type: 'warning', message: `Rejected ${attendee.name || attendee.email}` });
    // Refresh the list immediately
    // Refresh the list to ensure consistency with backend
    await fetchAttendees();
  } catch (error) {
    console.error(error);
    // Revert the optimistic update on error
    if (attendeeItem) {
      attendeeItem.status = 'PENDING';
    }
    $q.notify({ type: 'negative', message: 'Failed to reject attendee' });
  } finally {
    processingIds.value.delete(attendee.id);
  }
}
function getStatusColor(status: string): string {
  switch (status) {
    case 'APPROVED': return 'positive';
    case 'PENDING': return 'warning';
    case 'REJECTED': return 'negative';
    default: return 'grey';
  }
}
</script>
