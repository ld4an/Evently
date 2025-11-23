import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/IndexPage.vue') },
      { path: 'events', name: 'events', component: () => import('pages/EventListPage.vue') },
      { path: 'forbidden', name: 'forbidden', component: () => import('pages/ForbiddenPage.vue') },
      {
        path: 'events/:id',
        name: 'event-details',
        component: () => import('pages/EventDetailsPage.vue'),
      },
      {
        path: 'admin/dashboard',
        name: 'admin-dashboard',
        component: () => import('pages/admin/AdminDashboardPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
      },


      // Auth
      {
        path: 'login',
        name: 'login',
        component: () => import('pages/LoginPage.vue'),
        meta: { guestOnly: true },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('pages/RegisterPage.vue'),
        meta: { guestOnly: true },
      },

      // Attendee
      {
        path: 'my-events',
        name: 'my-events',
        component: () => import('pages/organizer/MyEventsPage.vue'),
        meta: { requiresAuth: true, roles: ['ATTENDEE', 'ORGANIZER', 'ADMIN'] },
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('pages/NotificationsPage.vue'),
        meta: { requiresAuth: true },
      },

      // Organizer
      {
        path: 'organizer/dashboard',
        name: 'organizer-dashboard',
        component: () => import('pages/organizer/OrganizerDashboardPage.vue'),
        meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] },
      },
      {
        path: 'organizer/events',
        name: 'organizer-events',
        component: () => import('pages/organizer/MyEventsPage.vue'),
        meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] },
      },
      {
        path: 'organizer/events/new',
        name: 'organizer-event-new',
        component: () => import('pages/organizer/EditEventPage.vue'),
        meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] },
      },
      {
        path: 'organizer/events/:id/edit',
        name: 'organizer-event-edit',
        component: () => import('pages/organizer/EditEventPage.vue'),
        meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] },
      },
      {
        path: 'organizer/events/:id/attendees',
        name: 'organizer-event-attendees',
        component: () => import('pages/organizer/EventAttendeesPage.vue'),
        meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] },
      },

      // etc.
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];
export default routes;
