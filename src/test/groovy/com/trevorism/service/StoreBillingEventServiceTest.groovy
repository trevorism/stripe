package com.trevorism.service

import com.stripe.Stripe
import com.stripe.model.Subscription
import com.stripe.param.SubscriptionListParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.model.BillingEvent
import com.trevorism.model.BillingSubscription
import io.micronaut.security.authentication.Authentication
import org.junit.jupiter.api.Test

class StoreBillingEventServiceTest {

    @Test
    void testProcessBillingEvent() {
        //StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        BillingEvent event = createSampleBillingEvent()
        //BillingEvent created = storeBillingEventService.processBillingEvent(event)
        assert event != null
    }

    private static BillingEvent createSampleBillingEvent() {
        BillingEvent billingEvent = new BillingEvent()
        billingEvent.userId = "5070662116835328"
        billingEvent.tenantId = null
        billingEvent.billingId = "evt_1H1J9vLzj6ZJ9Q9Q9"
        billingEvent.billingDate = new Date()
        billingEvent.billingAmount = 100.0
        billingEvent.billingCustomer = "cus_J9Q9Q9Q9Q9Q9Q9Q9"
        billingEvent.billingStatus = "succeeded"
        return billingEvent
    }

    @Test
    void testGetSubscription() {
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = new ClasspathBasedPropertiesProvider()
        Authentication authentication = [getAttributes: { -> [id: "5070662116835328"]}] as Authentication
        BillingSubscription subscription = storeBillingEventService.getSubscription(authentication)
        println subscription

    }

}
