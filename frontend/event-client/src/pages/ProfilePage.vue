<template>
  <q-page padding class="bg-grey-1">
    <div class="row justify-center">
      <div class="col-12 col-md-6 col-lg-5">
        <q-card bordered>
          <q-card-section>
            <div class="text-h6">Account Settings</div>
            <div class="text-caption text-grey-7">Update your email or password.</div>
          </q-card-section>
          <q-separator />
          <q-card-section>
            <q-form @submit.prevent="onSubmit" class="q-gutter-md">
              <q-input
                v-model="email"
                label="Email"
                type="email"
                outlined
                :rules="[val => !!val || 'Email is required']"
              />
              <q-input
                v-model="password"
                label="New Password"
                type="password"
                outlined
                hint="Leave blank to keep current password"
              />
              <q-btn
                label="Save changes"
                color="primary"
                unelevated
                class="full-width"
                :loading="saving"
                type="submit"
              />
            </q-form>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useQuasar } from 'quasar';
import api from 'src/services/api';
import { useAuthStore } from 'src/stores/auth-store';
import { isAxiosError } from 'axios';

const $q = useQuasar();
const auth = useAuthStore();

const email = ref(auth.user?.email || '');
const password = ref('');
const saving = ref(false);

async function onSubmit() {
  saving.value = true;
  try {
    const response = await api.put('/me/credentials', {
      email: email.value,
      password: password.value || undefined,
    });
    // Update auth token/user with the new email
    auth.setAuth(response.data.token, {
      id: auth.user?.id ?? null,
      name: auth.user?.name || email.value.split('@')[0] || 'User',
      email: email.value,
      role: auth.role ?? 'ATTENDEE',
    });
    password.value = '';
    $q.notify({ type: 'positive', message: 'Account updated' });
  } catch (error) {
    let message = 'Failed to update account';
    if (isAxiosError(error) && error.response?.data?.message) {
      message = error.response.data.message;
    }
    $q.notify({ type: 'negative', message });
  } finally {
    saving.value = false;
  }
}
</script>
