<template>
  <q-page padding>
    <div v-if="loading" class="row justify-center q-my-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>
    <div v-else-if="!event" class="text-center q-my-xl text-grey-6">
      <q-icon name="error_outline" size="4rem" />
      <div class="text-h6 q-mt-sm">Event not found</div>
      <q-btn label="Back to Events" color="primary" flat @click="router.push({ name: 'events' })" />
    </div>
    <div v-else class="row justify-center">
      <div class="col-12 col-md-10 col-lg-8">
        <q-btn
          flat
          icon="arrow_back"
          label="Back to Events"
          color="primary"
          class="q-mb-md"
          @click="router.push({ name: 'events' })"
        />
        <q-card class="shadow-10 overflow-hidden">
          <q-img :src="event.imageUrl || 'https://cdn.quasar.dev/img/parallax2.jpg'" :ratio="21/9">
            <div class="absolute-bottom text-h4 text-weight-bold q-pa-md">
              {{ event.name }}
            </div>
          </q-img>
          <q-card-section class="q-pa-lg">
            <div class="row q-col-gutter-lg">
              <div class="col-12 col-md-8">
                <div class="text-h6 text-primary q-mb-sm">About this Event</div>
                <div class="text-body1 text-grey-8" style="white-space: pre-line;">
                  {{ event.description || 'No description provided.' }}
                </div>
              </div>
              <div class="col-12 col-md-4">
                <q-card flat bordered class="bg-grey-1">
                  <q-card-section>
                    <div class="text-subtitle2 text-grey-7 q-mb-xs">Date & Time</div>
                    <div class="text-body1 q-mb-md text-weight-medium">
                      <q-icon name="event" color="primary" class="q-mr-xs" />
                      {{ new Date(event.date).toLocaleString() }}
                    </div>
                    <div class="text-subtitle2 text-grey-7 q-mb-xs">Location</div>
                    <div class="text-body1 q-mb-md text-weight-medium">
                      <q-icon name="place" color="primary" class="q-mr-xs" />
                      {{ event.location }}
                    </div>
                    <div class="text-subtitle2 text-grey-7 q-mb-xs">Organizer</div>
                    <div class="text-body1 text-weight-medium">
                      <q-icon name="person" color="primary" class="q-mr-xs" />
                      {{ event.organizer?.name || 'Unknown' }}
                    </div>
                  </q-card-section>
                  <q-separator />
                  <q-card-actions vertical align="stretch" class="q-pa-md">
                    <!-- Attendee Actions -->
                    <template v-if="canJoin">
                      <q-btn
                        label="Join Event"
                        color="primary"
                        size="lg"
                        unelevated
                        :loading="joining"
                        @click="joinEvent"
                      />
                    </template>
                    <div v-else-if="isAttendee && hasJoined" class="text-center text-positive text-weight-bold q-pa-sm">
                      <q-icon name="check_circle" size="sm" /> You are attending!
                    </div>
                    <!-- Organizer/Admin Actions -->
                    <template v-if="canManage">
                      <q-btn
                        label="Edit Event"
                        color="warning"
                        flat
                        class="q-mb-sm"
                        @click="router.push({ name: 'organizer-event-edit', params: { id: event.id } })"
                      />
                      <q-btn
                        label="Delete Event"
                        color="negative"
                        flat
                        @click="confirmDelete = true"
                      />
                    </template>
                  </q-card-actions>
                </q-card>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
    <!-- Delete Confirmation -->
    <q-dialog v-model="confirmDelete">
      <q-card>
        <q-card-section class="row items-center">
          <q-avatar icon="warning" color="negative" text-color="white" />
          <span class="q-ml-sm">Are you sure you want to delete this event? This action cannot be undone.</span>
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
import { useRoute, useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import { useAuthStore } from 'src/stores/auth-store';
import api from 'src/services/api';
import { isAxiosError } from 'axios';
interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
  description?: string;
  imageUrl?: string;
  organizer?: {
    id: number;
    name: string;
    user?: {
      email: string;
    };
  };
}
const route = useRoute();
const router = useRouter();
const $q = useQuasar();
const auth = useAuthStore();
const event = ref<Event | null>(null);
const loading = ref(true);
const joining = ref(false);
const confirmDelete = ref(false);
const hasJoined = ref(false); // Ideally fetch this from backend
const eventId = Number(route.params.id);
const isAttendee = computed(() => auth.role === 'ATTENDEE');
const isAdmin = computed(() => auth.role === 'ADMIN');
const isOrganizer = computed(() => auth.role === 'ORGANIZER');
const canManage = computed(() => {
  if (!event.value) return false;
  if (isAdmin.value) return true;
  if (isOrganizer.value) {
    // Check if current user is the owner
    // Note: This requires the backend to return organizer's user email or ID
    // For now, assuming backend returns organizer.user.email
    return event.value.organizer?.user?.email === auth.user?.email;
  }
  return false;
});
const canJoin = computed(() => isAttendee.value && !hasJoined.value);
onMounted(async () => {
  await fetchEvent();
  if (isAttendee.value) {
    await checkAttendance();
  }
});
async function fetchEvent() {
  try {
    const response = await api.get(`/events/${eventId}`);
    event.value = response.data;
  } catch (error) {
    console.error('Failed to fetch event', error);
    $q.notify({ type: 'negative', message: 'Failed to load event details' });
  } finally {
    loading.value = false;
  }
}
async function checkAttendance() {
  try {
    const response = await api.get('/me/attending-events');
    const joinedIds = (response.data as Event[]).map((e) => e.id);
    hasJoined.value = joinedIds.includes(eventId);
  } catch {
    hasJoined.value = false;
  }
}
async function joinEvent() {
  if (!auth.isAuthenticated) {
    $q.notify({ type: 'warning', message: 'Please login to join events' });
    void router.push({ name: 'login', query: { redirect: route.fullPath } });
    return;
  }
  joining.value = true;
  try {
    // Backend expects Attendee with email (required field)
    // The backend will automatically link it to the authenticated user
    await api.post(`/events/${eventId}/requests`, {
      email: auth.user?.email || '',
      name: auth.user?.name || '',
    });
    hasJoined.value = true;
    $q.notify({ type: 'positive', message: 'Request sent successfully!' });
  } catch (error: unknown) {
    console.error(error);
    let message = 'Failed to join event';
    if (isAxiosError(error)) {
      // Check for duplicate join error (backend returns 500 with specific message)
      const errorMessage = error.response?.data?.message || '';
      if (error.response?.status === 500 && errorMessage.includes('already requested')) {
        message = 'You have already requested to join this event';
        hasJoined.value = true; // Mark as joined to update UI
      } else if (errorMessage) {
        message = errorMessage;
      }
    }
    $q.notify({ type: 'negative', message });
  } finally {
    joining.value = false;
  }
}
async function deleteEvent() {
  try {
    await api.delete(`/events/${eventId}`);
    $q.notify({ type: 'positive', message: 'Event deleted successfully' });
    void router.push({ name: 'events' });
  } catch (error) {
    console.error(error);
    $q.notify({ type: 'negative', message: 'Failed to delete event' });
  }
}
</script>
