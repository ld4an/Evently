<template>
  <q-layout view="hHh lpR fFf">
    <!-- HEADER -->
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <!-- Left: burger button (now on all screens) -->
        <q-btn
          flat
          dense
          round
          icon="menu"
          class="q-mr-sm"
          @click="leftDrawerOpen = !leftDrawerOpen"
        />

        <!-- App title -->
        <q-toolbar-title class="row items-center">
          <q-avatar square size="32px" class="q-mr-sm bg-white text-primary">
            <q-icon name="event" />
          </q-avatar>
          <div class="column">
            <span class="text-weight-bold">Event Manager</span>
            <span class="text-caption text-grey-2">Organize &amp; join events</span>
          </div>
        </q-toolbar-title>

        <q-space />

        <!-- DESKTOP NAV -->
        <div class="gt-sm row items-center q-gutter-sm">
          <q-btn flat label="Home" @click="goTo('home')" />
          <q-btn flat label="Events" @click="goTo('events')" />

          <q-btn
            v-if="isAuthenticated && (role === 'ORGANIZER' || role === 'ADMIN')"
            flat
            icon="dashboard"
            label="Organizer"
            @click="goTo('organizer-dashboard')"
          />

          <q-btn
            v-if="isAuthenticated && role === 'ADMIN'"
            flat
            icon="admin_panel_settings"
            label="Admin"
            @click="goTo('admin-dashboard')"
          />

          <q-separator spaced vertical />

          <!-- Desktop auth buttons -->
          <template v-if="!isAuthenticated">
            <q-btn flat label="Login" @click="goTo('login')" />
            <q-btn
              color="secondary"
              unelevated
              label="Register"
              @click="goTo('register')"
            />
          </template>
          <template v-else>
            <q-chip color="white" text-color="primary" size="md" class="text-weight-bold">
              <q-avatar icon="person" color="primary" text-color="white" />
              {{ userDisplay }}
              <q-tooltip>Signed in as {{ userDisplay }} ({{ role || 'role' }})</q-tooltip>
            </q-chip>
            <q-btn flat round icon="manage_accounts" @click="goTo('profile')">
              <q-tooltip>Account</q-tooltip>
            </q-btn>
            <q-btn flat icon="logout" label="Logout" @click="logout">
              <q-tooltip>Sign out</q-tooltip>
            </q-btn>
          </template>
        </div>
      </q-toolbar>
    </q-header>

    <!-- LEFT DRAWER -->
    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      side="left"
      bordered
      :mini="miniState"
      class="bg-grey-1"
    >
      <!-- Drawer header with mini toggle -->
      <q-toolbar class="bg-primary text-white">
        <q-btn
          flat
          dense
          round
          :icon="miniState ? 'chevron_right' : 'chevron_left'"
          @click="miniState = !miniState"
        />
        <q-toolbar-title v-if="!miniState" class="text-subtitle2">
          Menu
        </q-toolbar-title>
      </q-toolbar>

      <q-list padding>
        <q-item clickable v-ripple @click="goToAndClose('home')">
          <q-item-section avatar>
            <q-icon name="home" />
          </q-item-section>
          <q-item-section>Home</q-item-section>
        </q-item>

        <q-item clickable v-ripple @click="goToAndClose('events')">
          <q-item-section avatar>
            <q-icon name="event" />
          </q-item-section>
          <q-item-section>Events</q-item-section>
        </q-item>

        <!-- Authenticated User Options -->
        <template v-if="isAuthenticated">
          <q-separator class="q-my-md" />

          <q-item clickable v-ripple @click="goToAndClose('my-events')">
            <q-item-section avatar>
              <q-icon name="bookmark" />
            </q-item-section>
            <q-item-section>My Events</q-item-section>
          </q-item>

          <q-item
            v-if="role === 'ORGANIZER' || role === 'ADMIN'"
            clickable
            v-ripple
            @click="goToAndClose('organizer-dashboard')"
          >
            <q-item-section avatar>
              <q-icon name="dashboard" />
            </q-item-section>
            <q-item-section>Organizer Dashboard</q-item-section>
          </q-item>

          <q-item
            v-if="role === 'ADMIN'"
            clickable
            v-ripple
            @click="goToAndClose('admin-dashboard')"
          >
            <q-item-section avatar>
              <q-icon name="admin_panel_settings" />
            </q-item-section>
            <q-item-section>Admin Panel</q-item-section>
          </q-item>

          <q-separator class="q-my-md" />

          <q-item clickable v-ripple @click="logout" class="text-negative">
            <q-item-section avatar>
              <q-icon name="logout" color="negative" />
            </q-item-section>
            <q-item-section>Logout</q-item-section>
          </q-item>
          <q-item clickable v-ripple @click="goToAndClose('profile')">
            <q-item-section avatar>
              <q-icon name="manage_accounts" />
            </q-item-section>
            <q-item-section>Account</q-item-section>
          </q-item>
        </template>

        <!-- Guest Options -->
        <template v-else>
          <q-separator class="q-my-md" />

          <q-item clickable v-ripple @click="goToAndClose('login')">
            <q-item-section avatar>
              <q-icon name="login" />
            </q-item-section>
            <q-item-section>Login</q-item-section>
          </q-item>

          <q-item clickable v-ripple @click="goToAndClose('register')">
            <q-item-section avatar>
              <q-icon name="person_add" />
            </q-item-section>
            <q-item-section>Register</q-item-section>
          </q-item>
        </template>
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

const router = useRouter();
const auth = useAuthStore();

const leftDrawerOpen = ref(false);
const miniState = ref(true);

const isAuthenticated = computed(() => auth.isAuthenticated);
const role = computed(() => auth.role);
const userDisplay = computed(() => auth.user?.name || auth.user?.email || 'User');

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
