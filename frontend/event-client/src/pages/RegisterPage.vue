<template>
  <q-page padding class="flex flex-center bg-grey-2">
    <q-card style="width: 480px; max-width: 92vw;" bordered>
      <q-card-section>
        <div class="text-h6">Create your account</div>
        <div class="text-caption text-grey-7">Pick a role to get the right workspace.</div>
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
            label="Register & Login"
            type="submit"
            color="primary"
            class="full-width q-mt-sm"
            :loading="loading"
          />
        </q-form>
      </q-card-section>

      <q-separator />
      <q-card-actions align="center">
        <span class="text-caption text-grey-7">Already have an account?</span>
        <q-btn flat color="primary" size="sm" label="Login" @click="router.push({ name: 'login' })" />
      </q-card-actions>
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
      message: 'Account created and logged in!',
    });

    void router.push({ name: 'home' });
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
