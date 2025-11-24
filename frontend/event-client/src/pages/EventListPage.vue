<template>
  <q-page padding>
    <div class="row q-col-gutter-md items-center q-mb-lg">
      <div class="col-12 col-md-8">
        <div class="text-h4 text-weight-bold text-primary">Discover Events</div>
        <div class="text-subtitle1 text-grey-7">Find and join amazing events near you</div>
        <div class="q-mt-sm">
          <q-chip color="secondary" text-color="white" icon="event_available">
            {{ events.length }} events
          </q-chip>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <q-input
          v-model="search"
          outlined
          dense
          placeholder="Search events..."
          class="bg-white"
        >
          <template v-slot:prepend>
            <q-icon name="search" />
          </template>
        </q-input>
        <div class="q-mt-sm flex justify-end" v-if="isOrganizerOrAdmin">
          <q-btn color="primary" icon="add" label="Create Event" unelevated @click="router.push({ name: 'organizer-event-new' })" />
        </div>
      </div>
    </div>
    <div v-if="loading" class="row justify-center q-my-xl">
      <q-spinner-dots color="primary" size="40px" />
    </div>
    <div v-else-if="filteredEvents.length === 0" class="text-center q-my-xl text-grey-6">
      <q-icon name="event_busy" size="4rem" />
      <div class="text-h6 q-mt-sm">No events found</div>
    </div>
    <div v-else class="row q-col-gutter-lg">
      <div v-for="event in filteredEvents" :key="event.id" class="col-12 col-sm-6 col-md-4 col-lg-3">
        <q-card class="my-card cursor-pointer shadow-hover transition-swing" @click="router.push({ name: 'event-details', params: { id: event.id } })">
          <q-img :src="event.imageUrl || 'https://cdn.quasar.dev/img/parallax2.jpg'" :ratio="16/9">
            <div class="absolute-bottom text-subtitle2 text-center">
              {{ new Date(event.date).toLocaleDateString() }}
            </div>
          </q-img>
          <q-card-section>
            <div class="text-h6 text-ellipsis">{{ event.name }}</div>
            <div class="text-subtitle2 text-grey-7 text-ellipsis">
              <q-icon name="place" size="xs" /> {{ event.location }}
            </div>
          </q-card-section>
          <q-card-section class="q-pt-none">
            <div class="text-caption text-grey-6 ellipsis-2-lines">
              {{ event.description || 'No description available.' }}
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from 'src/services/api';
import { useAuthStore } from 'src/stores/auth-store';
interface Event {
  id: number;
  name: string;
  date: string;
  location: string;
  description?: string;
  imageUrl?: string;
}
const router = useRouter();
const events = ref<Event[]>([]);
const loading = ref(true);
const search = ref('');
const auth = useAuthStore();
const isOrganizerOrAdmin = computed(() => auth.role === 'ORGANIZER' || auth.role === 'ADMIN');
const filteredEvents = computed(() => {
  if (!search.value) return events.value;
  const term = search.value.toLowerCase();
  return events.value.filter(e => 
    e.name.toLowerCase().includes(term) || 
    e.location.toLowerCase().includes(term)
  );
});
onMounted(async () => {
  try {
    const response = await api.get('/events');
    events.value = response.data;
  } catch (error) {
    console.error('Failed to fetch events', error);
  } finally {
    loading.value = false;
  }
});
</script>
<style scoped>
.my-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s, box-shadow 0.2s;
}
.my-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ellipsis-2-lines {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
