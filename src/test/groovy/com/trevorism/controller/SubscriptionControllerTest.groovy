package com.trevorism.controller

import com.stripe.model.Subscription
import com.trevorism.model.BillingSubscription
import com.trevorism.service.BillingEventService
import io.micronaut.security.authentication.Authentication
import org.junit.jupiter.api.Test

class SubscriptionControllerTest {

    @Test
    void testGetSubscription() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [getSubscription: {auth -> new BillingSubscription([amount: 10])}] as BillingEventService
        def subscription = controller.getSubscription({ } as Authentication)
        assert subscription.amount == 10d
    }

    @Test
    void testDeleteSubscription() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [cancelSubscription: {auth -> true}] as BillingEventService
        def result = controller.deleteSubscription({ } as Authentication)
        assert result
    }
}
