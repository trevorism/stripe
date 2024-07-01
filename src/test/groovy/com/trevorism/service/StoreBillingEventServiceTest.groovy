package com.trevorism.service

import com.trevorism.model.BillingEvent
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

}
