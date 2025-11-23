<template>
  <q-page padding class="flex flex-center">
    <q-card style="width: 400px; max-width: 90vw;">
      <q-card-section>
        <div class="text-h6">Register</div>
      </q-card-section>

      <q-card-section>
        <q-form @submit.prevent="onSubmit">
          <q-input
            v-model="name"
            label="Name"
            class="q-mb-md"
            outlined
            required
          />
          <q-input
            v-model="email"
            label="Email"
            type="email"
            class="q-mb-md"
            outlined
            required
          />
          <q-input
            v-model="password"
            label="Password"
            type="password"
            class="q-mb-md"
            outlined
            required
          />

          <!-- optional: choose role on register -->
          <q-select
            v-model="role"
            :options="roleOptions"
            label="Role"
            outlined
            class="q-mb-md"
            emit-value
            map-options
          />

          <q-btn
            label="Register"
            type="submit"
            color="primary"
            class="full-width q-mt-sm"
            :loading="loading"
          />
        </q-form>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import { useAuthStore, type UserRole } from 'src/stores/auth-store';
import { isAxiosError } from 'axios';

const name = ref('');
const email = ref('');
const password = ref('');
const loading = ref(false);

// optional role selection
const role = ref<UserRole>('ATTENDEE');
const roleOptions = [
  { label: 'Attendee', value: 'ATTENDEE' },
  { label: 'Organizer', value: 'ORGANIZER' },
];

const router = useRouter();
const $q = useQuasar();
const auth = useAuthStore();

async function onSubmit() {
  loading.value = true;
  try {
    // default to ATTENDEE, or pass role.value if using the select above
    await auth.register(name.value, email.value, password.value, role.value);

    $q.notify({
      type: 'positive',
      message: 'Registered successfully. You can now log in.',
    });

    void router.push({ name: 'login' });
  } catch (error: unknown) {
    console.error(error);
    let message = 'Registration failed';
    if (isAxiosError(error) && error.response?.data?.message) {
      message = error.response.data.message;
    }
    $q.notify({
      type: 'negative',
      message,
    });
  } finally {
    loading.value = false;
  }
}
</script>
