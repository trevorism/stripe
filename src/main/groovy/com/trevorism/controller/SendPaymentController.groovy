package com.trevorism.controller

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.stripe.Stripe
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.PaymentIntent
import com.stripe.model.PaymentMethod
import com.stripe.model.StripeObject
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.model.PaymentRequest
import com.trevorism.model.StripeCallback
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/payment")
class SendPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SendPaymentController)
    private PropertiesProvider propertiesProvider = new ClasspathBasedPropertiesProvider()

    SendPaymentController() {
        Stripe.apiKey = propertiesProvider.getProperty("apiKey")
    }

    @Tag(name = "Payment Operations")
    @Operation(summary = "Create a new Stripe Payment Session")
    @Post(value = "/session", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map createSession(@Body PaymentRequest paymentRequest, Optional<Authentication> authentication) {
        if (paymentRequest.dollars < 0.99) {
            throw new RuntimeException("Unable to process; insufficient funds for payment")
        }

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(paymentRequest.name).build()
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount((long) (paymentRequest.dollars * 100))
                .setProductData(productData)
                .build()
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(priceData)
                .build()

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(paymentRequest.successCallbackUrl)
                .setCancelUrl(paymentRequest.failureCallbackUrl)
                .addLineItem(lineItem)

        if (authentication.orElse(null)?.getAttributes()?.get("id")) {
            builder.putMetadata("userId", authentication.getAttributes().get("id"))
        }
        if (authentication.orElse(null)?.getAttributes()?.get("tenant")) {
            builder.putMetadata("tenantId", authentication.getAttributes().get("tenant"))
        }

        Session session = Session.create(builder.build())
        return [id: session.getId()]
    }

    @Tag(name = "Payment Operations")
    @Operation(summary = "Handle Stripe payment callback")
    @Post(value = "/webhook", produces = MediaType.APPLICATION_JSON)
    Map processStripeEvent(HttpRequest<String> request) {
        Gson gson = new Gson()
        String payload = request.getBody(String.class).orElseThrow { new RuntimeException("Unable to process; no payload found") }
        String endpointSecret = propertiesProvider.getProperty("apiSecret")
        String sigHeader = request.getHeaders().get("Stripe-Signature")

        try{
            Webhook.constructEvent(payload, sigHeader, endpointSecret)
        }catch(Exception e){
            log.error("Error verifying Stripe signature", e)
            throw new RuntimeException("Unable to process; invalid signature")
        }

        StripeCallback stripeCallback = gson.fromJson(payload, StripeCallback)
        String json = gson.toJson(stripeCallback)

        log.info("Send Payment webhook received")
        log.info(json)
        return gson.fromJson(json, Map)
    }
}
