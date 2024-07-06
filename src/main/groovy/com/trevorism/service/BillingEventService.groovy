package com.trevorism.service

import com.trevorism.model.BillingEvent
import com.trevorism.model.BillingSubscription
import io.micronaut.security.authentication.Authentication

interface BillingEventService {

    BillingEvent processBillingEvent(BillingEvent event)
    BillingSubscription getSubscription(Authentication authentication)
    boolean cancelSubscription(Authentication authentication)
}