package com.trevorism.controller

import com.trevorism.PropertiesProvider
import com.trevorism.model.BillingEvent
import com.trevorism.service.BillingEventService
import io.micronaut.http.HttpRequest
import org.junit.jupiter.api.Test

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class EventControllerTest {

    private static final String ENDPOINT_SECRET = "whsec_acceptance_only_test_secret"

    private List<BillingEvent> recorded = []

    @Test
    void testSignedSubscriptionSessionIsRecordedWithTheIdentityMetadata() {
        EventController controller = buildController()
        String payload = eventPayload(subscriptionSession())

        assert controller.processStripeEvent(signedRequest(payload))

        assert recorded.size() == 1
        assert recorded[0].userId == "5154038974775296"
        assert recorded[0].tenantId == "b54ef675-4d6b-4ac8-8d6e-b0345bcd02ba"
        assert recorded[0].billingCustomer == "cus_QOIVPo30vRL80c"
        assert recorded[0].billingAmount == 10.0d
    }

    @Test
    void testSignedOneTimeSessionIsIgnoredSoPaymentsAreNotRecordedTwice() {
        EventController controller = buildController()
        String payload = eventPayload(oneTimeSession())

        assert !controller.processStripeEvent(signedRequest(payload))
        assert recorded.isEmpty()
    }

    @Test
    void testSignedPaymentIntentIsStillRecorded() {
        EventController controller = buildController()
        String payload = eventPayload(paymentIntent())

        assert controller.processStripeEvent(signedRequest(payload))

        assert recorded.size() == 1
        assert recorded[0].billingId == "pi_3PSeUoKUPlXay6LP16myN6Ch"
        assert recorded[0].billingAmount == 4.99d
    }

    @Test
    void testUnsignedRequestIsRejected() {
        EventController controller = buildController()
        String payload = eventPayload(subscriptionSession())

        try {
            controller.processStripeEvent(HttpRequest.POST("/api/billing/webhook", payload))
            assert false
        } catch (RuntimeException e) {
            assert e.message.contains("invalid signature")
        }
        assert recorded.isEmpty()
    }

    @Test
    void testTamperedPayloadIsRejected() {
        EventController controller = buildController()
        String payload = eventPayload(subscriptionSession())
        String signature = stripeSignature(payload)
        String tampered = payload.replace('"amount_total": 1000', '"amount_total": 1')

        try {
            controller.processStripeEvent(
                    HttpRequest.POST("/api/billing/webhook", tampered).header("Stripe-Signature", signature))
            assert false
        } catch (RuntimeException e) {
            assert e.message.contains("invalid signature")
        }
        assert recorded.isEmpty()
    }

    @Test
    void testSignatureFromADifferentSecretIsRejected() {
        EventController controller = buildController()
        String payload = eventPayload(subscriptionSession())
        String foreignSignature = stripeSignature(payload, "whsec_a_different_secret")

        try {
            controller.processStripeEvent(
                    HttpRequest.POST("/api/billing/webhook", payload).header("Stripe-Signature", foreignSignature))
            assert false
        } catch (RuntimeException e) {
            assert e.message.contains("invalid signature")
        }
        assert recorded.isEmpty()
    }

    private EventController buildController() {
        EventController controller = new EventController()
        controller.propertiesProvider = [getProperty: { String key ->
            key == "apiSecret" ? ENDPOINT_SECRET : "sk_test_unused"
        }] as PropertiesProvider
        controller.billingEventService = [processBillingEvent: { BillingEvent event ->
            recorded << event
            return event
        }] as BillingEventService
        return controller
    }

    private static HttpRequest<String> signedRequest(String payload) {
        return HttpRequest.POST("/api/billing/webhook", payload).header("Stripe-Signature", stripeSignature(payload))
    }

    private static String stripeSignature(String payload, String secret = ENDPOINT_SECRET) {
        long timestamp = System.currentTimeMillis() / 1000
        Mac mac = Mac.getInstance("HmacSHA256")
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"))
        String digest = mac.doFinal("${timestamp}.${payload}".getBytes("UTF-8")).encodeHex().toString()
        return "t=${timestamp},v1=${digest}"
    }

    private static String eventPayload(String dataObject) {
        return """{
    "id": "evt_test_webhook",
    "object": "event",
    "api_version": "2024-06-20",
    "created": 1719785270,
    "type": "checkout.session.completed",
    "data": { "object": ${dataObject} }
  }"""
    }

    private static String subscriptionSession() {
        return """{
      "id": "cs_test_subscription",
      "object": "checkout.session",
      "amount_total": 1000,
      "created": 1719785270,
      "currency": "usd",
      "customer": "cus_QOIVPo30vRL80c",
      "metadata": {
        "userId": "5154038974775296",
        "tenantId": "b54ef675-4d6b-4ac8-8d6e-b0345bcd02ba"
      },
      "mode": "subscription",
      "payment_status": "paid",
      "status": "complete",
      "subscription": "sub_1PXVudKUPlXay6LPZ8kQrTuv"
    }"""
    }

    private static String oneTimeSession() {
        return """{
      "id": "cs_test_onetime",
      "object": "checkout.session",
      "amount_total": 499,
      "created": 1719785270,
      "currency": "usd",
      "metadata": { "userId": "5154038974775296" },
      "mode": "payment",
      "payment_status": "paid",
      "status": "complete"
    }"""
    }

    private static String paymentIntent() {
        return """{
      "id": "pi_3PSeUoKUPlXay6LP16myN6Ch",
      "object": "payment_intent",
      "amount": 499,
      "created": 1718626634,
      "currency": "usd",
      "customer": null,
      "metadata": {},
      "status": "succeeded"
    }"""
    }
}
