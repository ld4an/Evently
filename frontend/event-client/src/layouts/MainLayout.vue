<template>
  <q-layout view="hHh lpR fFf">
    <!-- HEADER -->
    <q-header elevated>
      <q-toolbar>

        <!-- Left: burger button on mobile -->
        <q-btn
          flat
          dense
          round
          icon="menu"
          class="q-mr-sm"
          @click="leftDrawerOpen = !leftDrawerOpen"
        />

        <!-- App title -->
        <q-toolbar-title>
          Event Management System
        </q-toolbar-title>

        <!-- Desktop navigation -->
        <div class="gt-sm row items-center q-gutter-sm">
          <q-btn flat label="Home" @click="goTo('home')" />
          <q-btn flat label="Events" @click="goTo('events')" />

          <!-- Attendee-only -->
          <q-btn
            v-if="isAuthenticated && (role === 'ATTENDEE' || role === 'ORGANIZER' || role === 'ADMIN')"
            flat
            label="My Events"
            @click="goTo('my-events')"
          />

          <!-- Organizer-only -->
          <q-btn
            v-if="isAuthenticated && (role === 'ORGANIZER' || role === 'ADMIN')"
            flat
            label="Organizer Dashboard"
            @click="goTo('organizer-dashboard')"
          />

          <!-- Admin-only -->
          <q-btn
            v-if="isAuthenticated && role === 'ADMIN'"
            flat
            label="Admin Panel"
            @click="goTo('admin-dashboard')"
          />

          <!-- Right side: auth controls -->
          <template v-if="!isAuthenticated">
            <q-btn flat label="Login" @click="goTo('login')" />
            <q-btn
              color="primary"
              unelevated
              label="Register"
              @click="goTo('register')"
            />
          </template>
          <template v-else>
            <q-btn
              flat
              :label="auth.user?.name || 'Account'"
              icon="account_circle"
            />
            <q-btn flat icon="logout" @click="logout" />
          </template>
        </div>
      </q-toolbar>
    </q-header>

    <!-- LEFT DRAWER (mobile nav) -->
    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      side="left"
      bordered
    >
      <q-list>
        <q-item clickable v-ripple @click="goToAndClose('home')">
          <q-item-section>Home</q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="goToAndClose('events')">
          <q-item-section>Events</q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="goToAndClose('my-events')">
          <q-item-section>My Events</q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="goToAndClose('login')">
          <q-item-section>Login</q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="goToAndClose('register')">
          <q-item-section>Register</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <!-- MAIN CONTENT -->
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from 'src/stores/auth-store';

const leftDrawerOpen = ref(false);
const router = useRouter();
const auth = useAuthStore();

const isAuthenticated = computed(() => auth.isAuthenticated);
const role = computed(() => auth.role);

function goTo(name: string) {
  void router.push({ name });
}

function goToAndClose(name: string) {
  leftDrawerOpen.value = false;
  void router.push({ name });
}

function logout() {
  auth.logout();
  void router.push({ name: 'home' });
}
</script>
