<template>
  <q-page padding class="flex flex-center bg-grey-2">
    <q-card style="width: 440px; max-width: 92vw;" bordered>
      <q-card-section>
        <div class="text-h6">Welcome back</div>
        <div class="text-caption text-grey-7">Sign in to continue.</div>
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

          <div class="text-caption text-grey-7 q-mt-md text-center">
            No account?
            <q-btn flat color="primary" size="sm" label="Register" @click="router.push({ name: 'register' })" />
          </div>
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
