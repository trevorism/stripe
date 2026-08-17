package com.trevorism.model

import com.google.gson.Gson
import org.junit.jupiter.api.Test

class CheckoutSessionBillingEventTest {

    private static final String paidSubscriptionSession = """{
    "id": "cs_test_a1H8jKlMnOpQrStUvWxYz",
    "object": "checkout.session",
    "amount_subtotal": 1000,
    "amount_total": 1000,
    "created": 1719785270,
    "currency": "usd",
    "customer": "cus_QOIVPo30vRL80c",
    "livemode": false,
    "metadata": {
      "userId": "5154038974775296",
      "tenantId": "b54ef675-4d6b-4ac8-8d6e-b0345bcd02ba"
    },
    "mode": "subscription",
    "payment_status": "paid",
    "status": "complete",
    "subscription": "sub_1PXVudKUPlXay6LPZ8kQrTuv"
  }"""

    private static final String unpaidSubscriptionSession = """{
    "id": "cs_test_unpaid",
    "object": "checkout.session",
    "amount_total": 1000,
    "created": 1719785270,
    "customer": "cus_QOIVPo30vRL80c",
    "metadata": {
      "userId": "5154038974775296"
    },
    "mode": "subscription",
    "payment_status": "unpaid",
    "status": "open"
  }"""

    private static final String paidOneTimePaymentSession = """{
    "id": "cs_test_onetime",
    "object": "checkout.session",
    "amount_total": 499,
    "created": 1719785270,
    "currency": "usd",
    "customer": null,
    "metadata": {
      "userId": "5154038974775296"
    },
    "mode": "payment",
    "payment_status": "paid",
    "status": "complete"
  }"""

    private static final String unsupportedObject = """{
    "id": "sub_1PXVudKUPlXay6LPZ8kQrTuv",
    "object": "subscription",
    "created": 1719785270,
    "customer": "cus_QOIVPo30vRL80c",
    "status": "active"
  }"""

    @Test
    void testPaidCheckoutSessionCarriesIdentity() {
        BillingEvent billingEvent = BillingEvent.from(buildCallback(paidSubscriptionSession))
        assert billingEvent != null
        assert billingEvent.userId == "5154038974775296"
        assert billingEvent.tenantId == "b54ef675-4d6b-4ac8-8d6e-b0345bcd02ba"
        assert billingEvent.billingId == "cs_test_a1H8jKlMnOpQrStUvWxYz"
        assert billingEvent.billingDate
        assert billingEvent.billingAmount == 10.0d
        assert billingEvent.billingCustomer == "cus_QOIVPo30vRL80c"
        assert billingEvent.billingStatus == "paid"
    }

    @Test
    void testUnpaidCheckoutSessionIsIgnored() {
        assert BillingEvent.from(buildCallback(unpaidSubscriptionSession)) == null
    }

    @Test
    void testPaidOneTimeSessionIsIgnoredSoPaymentsAreNotRecordedTwice() {
        assert BillingEvent.from(buildCallback(paidOneTimePaymentSession)) == null
    }

    @Test
    void testUnsupportedObjectIsIgnored() {
        assert BillingEvent.from(buildCallback(unsupportedObject)) == null
    }

    @Test
    void testMissingDataIsIgnored() {
        assert BillingEvent.from(new StripeCallbackEvent()) == null
        assert BillingEvent.from(null) == null
    }

    private static StripeCallbackEvent buildCallback(String json) {
        StripeCallbackEventDataObject data = new Gson().fromJson(json, StripeCallbackEventDataObject)
        return new StripeCallbackEvent(data: new StripeCallbackEventData(object: data))
    }
}
