package com.trevorism.service

import com.trevorism.model.BillingEvent
import io.micronaut.security.authentication.Authentication

interface BillingEventService {

    BillingEvent processBillingEvent(BillingEvent event)
    boolean cancelSubscription(Authentication authentication)
}