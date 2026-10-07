package com.trevorism.controller

import com.google.gson.Gson
import com.stripe.exception.SignatureVerificationException
import com.stripe.net.Webhook
import com.trevorism.PropertiesProvider
import com.trevorism.model.BillingEvent
import com.trevorism.model.StripeCallbackEvent
import com.trevorism.service.BillingEventService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.exceptions.HttpStatusException
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
        String payload = request.getBody(String.class)
                .orElseThrow { new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to process; no payload found") }
        validateStripeEvent(request, payload)

        StripeCallbackEvent stripeCallback = gson.fromJson(payload, StripeCallbackEvent)
        BillingEvent billingEvent = BillingEvent.from(stripeCallback)
        if(!billingEvent){
            log.debug("Ignoring event of type: ${stripeCallback?.data?.object?.object}")
            return false
        }

        billingEventService.processBillingEvent(billingEvent)
        return true
    }

    private void validateStripeEvent(HttpRequest<String> request, String payload) {
        String endpointSecret = propertiesProvider.getProperty("apiSecret")
        if (!endpointSecret) {
            throw new IllegalStateException("Stripe webhook secret is not configured")
        }
        String sigHeader = request.getHeaders().get("Stripe-Signature")
        if (!sigHeader) {
            log.warn("Rejected Stripe webhook with no Stripe-Signature header")
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to process; invalid signature")
        }

        try {
            Webhook.constructEvent(payload, sigHeader, endpointSecret)
        } catch (SignatureVerificationException e) {
            log.warn("Rejected Stripe webhook: ${e.message}")
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to process; invalid signature")
        }
    }
}
