package com.trevorism.service

import com.trevorism.model.BillingEvent
import com.trevorism.model.BillingSubscription
import io.micronaut.security.authentication.Authentication

interface BillingEventService {

    BillingEvent processBillingEvent(BillingEvent event)
    BillingSubscription getSubscription(Authentication authentication)
    BillingSubscription getSubscriptionForCustomer(String customerId)
    boolean cancelSubscription(Authentication authentication)
    Map createPortalSession(Authentication authentication, String returnUrl)
}