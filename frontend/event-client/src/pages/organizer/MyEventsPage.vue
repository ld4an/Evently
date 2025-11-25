<template>
  <q-page padding>
    <div class="row items-center justify-between q-mb-lg">
      <div>
        <div class="text-h4 text-weight-bold text-primary">My Events</div>
        <div class="text-subtitle1 text-grey-7">
          <span v-if="isOrganizerOrAdmin">Manage everything you organize.</span>
          <span v-else>Events you are attending or requested to join.</span>
        </div>
      </div>
      <q-btn
        v-if="isOrganizerOrAdmin"
        label="Create New Event"
        icon="add"
        color="primary"
        unelevated
        @click="router.push({ name: 'organizer-event-new' })"
      />
    </div>

    <div v-if="loading" class="row justify-center q-my-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>

    <div v-else-if="!isOrganizerOrAdmin && !isAttendee" class="text-center q-my-xl text-grey-6">
      <q-icon name="lock" size="4rem" />
      <div class="text-h6 q-mt-sm">Access restricted</div>
      <div class="text-caption q-mt-xs">Please log in with an attendee or organizer account.</div>
    </div>

    <div v-else-if="events.length === 0" class="text-center q-my-xl text-grey-6">
      <q-icon name="event_note" size="4rem" />
      <div class="text-h6 q-mt-sm">
        <span v-if="isOrganizerOrAdmin">You haven't created any events yet.</span>
        <span v-else>You are not attending any events yet.</span>
      </div>
      <q-btn
        v-if="isOrganizerOrAdmin"
        label="Create your first event"
        color="primary"
        flat
        class="q-mt-sm"
        @click="router.push({ name: 'organizer-event-new' })"
      />
      <q-btn
        v-else
        label="Browse events"
        color="primary"
        flat
        class="q-mt-sm"
        @click="router.push({ name: 'events' })"
      />
    </div>

    <div v-else class="row q-col-gutter-md">
      <div v-for="event in events" :key="event.id" class="col-12 col-md-6 col-lg-4">
        <q-card class="shadow-2 hover-shadow transition-swing">
          <q-img :src="event.imageUrl || 'https://cdn.quasar.dev/img/parallax2.jpg'" :ratio="16/9">
            <div class="absolute-bottom text-subtitle2">
              {{ new Date(event.date).toLocaleDateString() }}
            </div>
          </q-img>

          <q-card-section>
            <div class="row items-center no-wrap">
              <div class="col">
                <div class="text-h6 ellipsis">{{ event.name }}</div>
                <div class="text-subtitle2 text-grey-7 ellipsis">{{ event.location }}</div>
              </div>
            </div>
          </q-card-section>

          <q-separator />

          <q-card-actions align="right">
            <q-btn flat color="primary" label="View" @click="router.push({ name: 'event-details', params: { id: event.id } })" />
            <template v-if="isOrganizerOrAdmin">
              <q-btn flat color="warning" label="Edit" @click="router.push({ name: 'organizer-event-edit', params: { id: event.id } })" />
              <q-btn flat color="secondary" label="Attendees" @click="router.push({ name: 'organizer-event-attendees', params: { id: event.id } })" />
            </template>
          </q-card-actions>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from 'src/services/api';
import { useQuasar } from 'quasar';
import { useAuthStore } from 'src/stores/auth-store';

interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
  imageUrl?: string;
}

const router = useRouter();
const $q = useQuasar();
const events = ref<Event[]>([]);
const loading = ref(true);
const auth = useAuthStore();

const isOrganizerOrAdmin = computed(() => auth.role === 'ORGANIZER' || auth.role === 'ADMIN');
const isAttendee = computed(() => auth.role === 'ATTENDEE');

onMounted(async () => {
  try {
    const endpoint = isAttendee.value ? '/me/attending-events' : '/me/events';
    const response = await api.get(endpoint);
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
.hover-shadow:hover {
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}
</style>
