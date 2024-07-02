package com.trevorism.controller

import com.google.gson.Gson
import com.stripe.Stripe
import com.stripe.net.Webhook
import com.trevorism.PropertiesProvider
import com.trevorism.model.BillingEvent
import com.trevorism.model.StripeCallbackEvent
import com.trevorism.service.BillingEventService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/billing")
class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController)
    private Gson gson = new Gson()

    @Inject
    private PropertiesProvider propertiesProvider
    @Inject
    BillingEventService billingEventService

    @Tag(name = "Billing Event Operations")
    @Operation(summary = "Handle Stripe payment callback")
    @Post(value = "/webhook", produces = MediaType.APPLICATION_JSON)
    boolean processStripeEvent(HttpRequest<String> request) {
        Stripe.apiKey = propertiesProvider?.getProperty("apiKey")
        String payload = request.getBody(String.class).orElseThrow { new RuntimeException("Unable to process; no payload found") }
        validateStripeEvent(request, payload)

        StripeCallbackEvent stripeCallback = gson.fromJson(payload, StripeCallbackEvent)
        if(stripeCallback?.data?.object?.object != "payment_intent"){
            log.debug("Ignoring event of type: ${stripeCallback?.data?.object?.object}")
            return false
        }

        BillingEvent billingEvent = BillingEvent.from(stripeCallback)
        billingEventService.processBillingEvent(billingEvent)
        return true
    }

    private void validateStripeEvent(HttpRequest<String> request, String payload) {
        String endpointSecret = propertiesProvider.getProperty("apiSecret")
        String sigHeader = request.getHeaders().get("Stripe-Signature")

        try {
            Webhook.constructEvent(payload, sigHeader, endpointSecret)
        } catch (Exception e) {
            log.error("Error verifying Stripe signature", e)
            throw new RuntimeException("Unable to process; invalid signature")
        }
    }
}
