package com.trevorism.controller

import com.stripe.model.Subscription
import com.trevorism.model.BillingSubscription
import com.trevorism.service.BillingEventService
import io.micronaut.security.authentication.Authentication
import org.apache.hc.client5.http.HttpResponseException
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
    void testGetSubscriptionForCustomer() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [getSubscriptionForCustomer: { String customerId ->
            new BillingSubscription([customerId: customerId, active: true])
        }] as BillingEventService

        def subscription = controller.getSubscriptionForCustomer("cus_abc")

        assert subscription.customerId == "cus_abc"
        assert subscription.active
    }

    @Test
    void testGetSubscriptionForCustomerReportsInactiveRatherThanFailing() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [getSubscriptionForCustomer: { String customerId ->
            new BillingSubscription([customerId: customerId, active: false])
        }] as BillingEventService

        def subscription = controller.getSubscriptionForCustomer("cus_lapsed")

        assert subscription.customerId == "cus_lapsed"
        assert !subscription.active
    }

    @Test
    void testGetSubscriptionForCustomerTranslatesFailureToNotFoundWithoutLeakingDetails() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [getSubscriptionForCustomer: { String customerId ->
            throw new RuntimeException("stripe: invalid api key sk_live_secret")
        }] as BillingEventService

        try {
            controller.getSubscriptionForCustomer("cus_missing")
            assert false
        } catch (HttpResponseException e) {
            assert e.statusCode == 404
            assert e.reasonPhrase == "Unable to look up subscription"
            assert !e.message.contains("sk_live_secret")
        }
    }

    @Test
    void testGetSubscriptionForCustomerRejectsABlankCustomerId() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [getSubscriptionForCustomer: { String customerId ->
            throw new IllegalStateException("should not be called")
        }] as BillingEventService

        try {
            controller.getSubscriptionForCustomer("   ")
            assert false
        } catch (HttpResponseException e) {
            assert e.statusCode == 400
        }
    }

    @Test
    void testDeleteSubscription() {
        SubscriptionController controller = new SubscriptionController()
        controller.billingEventService = [cancelSubscription: {auth -> true}] as BillingEventService
        def result = controller.deleteSubscription({ } as Authentication)
        assert result
    }
}
