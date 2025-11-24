<template>
  <q-page padding>
    <div class="row justify-center">
      <div class="col-12 col-md-8 col-lg-6">
        <q-card class="shadow-10">
          <q-card-section class="bg-primary text-white">
            <div class="text-h5 text-weight-bold">{{ isEdit ? 'Edit Event' : 'Create New Event' }}</div>
          </q-card-section>

          <q-card-section class="q-pt-lg">
            <q-form @submit.prevent="onSubmit" class="q-gutter-md">
              <q-input
                v-model="form.name"
                label="Event Name"
                outlined
                :rules="[val => !!val || 'Name is required']"
              />

              <div class="row q-col-gutter-md">
                <div class="col-12 col-md-6">
                  <q-input
                    v-model="form.date"
                    label="Date & Time"
                    type="datetime-local"
                    outlined
                    stack-label
                    :rules="[val => !!val || 'Date is required']"
                  />
                </div>
                <div class="col-12 col-md-6">
                  <q-input
                    v-model="form.location"
                    label="Location"
                    outlined
                    :rules="[val => !!val || 'Location is required']"
                  />
                </div>
              </div>

              <q-input
                v-model="form.description"
                label="Description"
                type="textarea"
                outlined
                rows="4"
              />

              <q-input
                v-model="form.imageUrl"
                label="Image URL (optional)"
                outlined
                hint="Link to an image for the event banner"
              />

              <q-input
                v-model.number="form.maxAttendees"
                label="Max Attendees"
                type="number"
                outlined
              />

              <div class="row justify-end q-mt-lg">
                <q-btn label="Cancel" flat color="grey" class="q-mr-sm" @click="router.back()" />
                <q-btn
                  :label="isEdit ? 'Update Event' : 'Create Event'"
                  type="submit"
                  color="primary"
                  unelevated
                  :loading="submitting"
                />
              </div>
            </q-form>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import api from 'src/services/api';

const route = useRoute();
const router = useRouter();
const $q = useQuasar();

const isEdit = computed(() => !!route.params.id);
const submitting = ref(false);

const form = ref({
  name: '',
  date: '',
  location: '',
  description: '',
  imageUrl: '',
  maxAttendees: 100,
});

onMounted(async () => {
  if (isEdit.value) {
    await fetchEvent();
  }
});

async function fetchEvent() {
  try {
    const eventId = String(route.params.id);
    const response = await api.get(`/events/${eventId}`);
    const event = response.data;
    // Format date for datetime-local input (YYYY-MM-DDTHH:mm)
    const dateObj = new Date(event.date);
    // Adjust for timezone offset to show local time correctly in input
    const tzOffset = dateObj.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(dateObj.getTime() - tzOffset)).toISOString().slice(0, 16);

    form.value = {
      name: event.name,
      date: localISOTime,
      location: event.location,
      description: event.description || '',
      imageUrl: event.imageUrl || '',
      maxAttendees: event.maxAttendees || 100,
    };
  } catch (error) {
    console.error('Failed to fetch event', error);
    $q.notify({ type: 'negative', message: 'Failed to load event data' });
  }
}

async function onSubmit() {
  submitting.value = true;
  try {
    const payload = {
      ...form.value,
      date: new Date(form.value.date).toISOString(),
    };

    if (isEdit.value) {
      const eventId = String(route.params.id);
      await api.put(`/events/${eventId}`, payload);
      $q.notify({ type: 'positive', message: 'Event updated successfully' });
    } else {
      await api.post('/me/events', payload);
      $q.notify({ type: 'positive', message: 'Event created successfully' });
    }
    router.back();
  } catch (error: unknown) {
    console.error(error);
    $q.notify({ type: 'negative', message: 'Failed to save event' });
  } finally {
    submitting.value = false;
  }
}
</script>
