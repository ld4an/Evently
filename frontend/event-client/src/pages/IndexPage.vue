<template>
  <q-page class="page-bg">
    <div class="hero">
      <div class="hero__content">
        <div class="text-overline text-white text-weight-bold q-mb-sm">Plan. Publish. Participate.</div>
        <div class="text-h3 text-weight-bold text-white q-mb-sm">Welcome to Event Manager</div>
        <div class="text-subtitle1 text-grey-2 q-mb-md">
          Discover upcoming events, join as an attendee, or manage your own experiences as an organizer.
        </div>
        <div class="row q-col-gutter-sm">
          <div class="col-auto">
            <q-btn color="white" text-color="primary" icon="event" label="Browse Events" @click="go('events')" />
          </div>
          <div class="col-auto">
            <q-btn color="secondary" unelevated icon="dashboard" label="Organizer Console" @click="go('organizer-dashboard')" />
          </div>
          <div class="col-auto">
            <q-btn flat text-color="white" label="Admin Panel" icon-right="chevron_right" @click="go('admin-dashboard')" />
          </div>
        </div>
      </div>
      <q-card class="hero__card">
        <q-card-section>
          <div class="text-subtitle1 text-grey-7">Quick roles</div>
          <div class="text-h6 text-weight-bold q-mb-md">Choose how you want to participate</div>
          <div class="row q-col-gutter-sm">
            <q-chip color="primary" text-color="white" icon="person">Attendee</q-chip>
            <q-chip color="secondary" text-color="white" icon="badge">Organizer</q-chip>
            <q-chip color="accent" text-color="white" icon="admin_panel_settings">Admin</q-chip>
          </div>
          <div class="text-caption text-grey-7 q-mt-md">
            Sign in to unlock role-based dashboards. Admin access is restricted to admin accounts.
          </div>
        </q-card-section>
        <q-separator />
        <q-card-actions align="right">
          <q-btn color="primary" unelevated label="Get Started" @click="go('login')" />
        </q-card-actions>
      </q-card>
    </div>

    <div class="q-pa-md q-mt-xl">
      <div class="row q-col-gutter-lg">
        <div class="col-12 col-md-4" v-for="card in cards" :key="card.title">
          <q-card class="shadow-2 feature-card">
            <q-card-section class="row items-center q-gutter-sm">
              <q-avatar :icon="card.icon" color="primary" text-color="white" />
              <div class="text-h6 text-weight-bold">{{ card.title }}</div>
            </q-card-section>
            <q-card-section class="text-grey-8">
              {{ card.description }}
            </q-card-section>
            <q-card-actions align="left">
              <q-btn flat color="primary" :label="card.cta" @click="go(card.route)" />
            </q-card-actions>
          </q-card>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();

const cards = [
  {
    title: 'Explore events',
    description: 'See everything that is coming up, filter by location, and read details before you join.',
    cta: 'View events',
    icon: 'travel_explore',
    route: 'events',
  },
  {
    title: 'Host experiences',
    description: 'Create, edit, and manage events with attendee approvals from the organizer dashboard.',
    cta: 'Organizer dashboard',
    icon: 'event_available',
    route: 'organizer-dashboard',
  },
  {
    title: 'Govern the platform',
    description: 'Admins can oversee all events, remove inappropriate content, and manage users.',
    cta: 'Admin panel',
    icon: 'security',
    route: 'admin-dashboard',
  },
];

function go(name: string) {
  void router.push({ name });
}
</script>

<style scoped>
.page-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e88e5 0%, #1565c0 40%, #0d47a1 100%);
}
.hero {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 24px;
  padding: 48px 24px 16px;
}
.hero__content {
  color: white;
  max-width: 720px;
}
.hero__card {
  background: white;
  border-radius: 12px;
}
@media (max-width: 1023px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 32px 16px 8px;
  }
}
.feature-card {
  border-radius: 12px;
}
</style>
