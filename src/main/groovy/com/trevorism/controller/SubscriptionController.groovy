package com.trevorism.controller

import com.google.gson.Gson
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.model.PaymentRequest
import com.trevorism.model.StripeCallback
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/api/subscription")
class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController)

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Create a new Stripe Subscription")
    @Post(value = "/session", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Map createSession(@Body PaymentRequest paymentRequest, Authentication authentication) {
        if (paymentRequest.dollars != 10.00) {
            throw new RuntimeException("Unable to process; insufficient funds for payment")
        }
        PropertiesProvider propertiesProvider = new ClasspathBasedPropertiesProvider()
        Stripe.apiKey = propertiesProvider.getProperty("apiKey")

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
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(paymentRequest.successCallbackUrl)
                .setCancelUrl(paymentRequest.failureCallbackUrl)
                .addLineItem(lineItem)

        if (authentication?.getAttributes()?.get("id")) {
            builder.putMetadata("userId", authentication.getAttributes().get("id").toString())
        }
        if (authentication?.getAttributes()?.get("tenant")) {
            builder.putMetadata("tenantId", authentication.getAttributes().get("tenant").toString())
        }

        Session session = Session.create(builder.build())
        return [id: session.getId()]
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Handle Stripe payment callback")
    @Post(value = "/webhook", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map processStripeEvent(@Body StripeCallback callback) {
        Gson gson = new Gson()
        log.info("Subscription webhook received")
        log.info(gson.toJson(callback))
        return [id: callback.id]
    }
}
