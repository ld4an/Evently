<template>
  <q-page padding>
    <div class="text-h4 text-weight-bold text-primary q-mb-lg">Notifications</div>
    
    <div class="row justify-center">
      <div class="col-12 col-md-8">
        <q-card class="shadow-2">
          <q-card-section class="text-center q-pa-xl">
            <q-icon name="mark_email_unread" size="6rem" color="primary" class="q-mb-md" />
            <div class="text-h5 text-weight-bold q-mb-sm">Check your Inbox!</div>
            <div class="text-body1 text-grey-8">
              We send all important updates directly to your email address.
            </div>
          </q-card-section>
          
          <q-separator />
          
          <q-card-section>
            <div class="text-subtitle1 text-weight-bold q-mb-sm">What you'll receive:</div>
            <q-list bordered separator>
              <q-item>
                <q-item-section avatar>
                  <q-icon name="check_circle" color="positive" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Booking Approvals</q-item-label>
                  <q-item-label caption>Get notified immediately when an organizer approves your request.</q-item-label>
                </q-item-section>
              </q-item>
              
              <q-item>
                <q-item-section avatar>
                  <q-icon name="cancel" color="negative" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>Booking Rejections</q-item-label>
                  <q-item-label caption>We'll let you know if your request couldn't be accommodated.</q-item-label>
                </q-item-section>
              </q-item>
              
              <q-item>
                <q-item-section avatar>
                  <q-icon name="smart_toy" color="accent" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>AI-Powered Updates</q-item-label>
                  <q-item-label caption>Our AI assistant crafts personalized messages just for you.</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
// No script needed for this static informational page
</script>
