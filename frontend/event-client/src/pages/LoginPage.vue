<template>
  <q-page padding class="flex flex-center">
    <q-card style="width: 400px; max-width: 90vw;">
      <q-card-section>
        <div class="text-h6">Login</div>
      </q-card-section>

      <q-card-section>
        <q-form @submit.prevent="onSubmit">
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

          <q-btn
            label="Login"
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
import { useRouter, useRoute } from 'vue-router';
import { useQuasar } from 'quasar';
import { useAuthStore } from 'src/stores/auth-store';
import { isAxiosError } from 'axios';

const email = ref('');
const password = ref('');
const loading = ref(false);

const router = useRouter();
const route = useRoute();
const $q = useQuasar();
const auth = useAuthStore();

async function onSubmit() {
  loading.value = true;
  try {
    await auth.login(email.value, password.value);

    $q.notify({
      type: 'positive',
      message: 'Logged in successfully',
    });

    // redirect to "redirect" query if present, otherwise home
    const redirect = (route.query.redirect as string) || '/';
    void router.push(redirect);
  } catch (error: unknown) {
    console.error(error);
    let message = 'Login failed';
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
