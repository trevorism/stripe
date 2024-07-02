package com.trevorism.controller

import com.google.gson.Gson
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.model.PaymentRequest
import com.trevorism.model.StripeCallbackEvent
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.BillingEventService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.*

@Controller("/api/subscription")
class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController)

    @Inject
    private PropertiesProvider propertiesProvider

    @Inject
    BillingEventService billingEventService

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Create a new Stripe Subscription")
    @Post(value = "/session", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    Map createSession(@Body PaymentRequest paymentRequest, Authentication authentication) {
        Stripe.apiKey = propertiesProvider?.getProperty("apiKey")
        if (paymentRequest.dollars != 10.00d) {
            throw new RuntimeException("Unable to process; insufficient funds for payment")
        }
        ProductData productData = ProductData.builder().setName(paymentRequest.name).build()
        SessionCreateParams.LineItem.PriceData priceData = builder()
                .setCurrency("usd")
                .setUnitAmount((long) (paymentRequest.dollars * 100))
                .setRecurring(new Recurring.Builder().setInterval(Recurring.Interval.MONTH).setIntervalCount(1).build())
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
    @Operation(summary = "Remove a Stripe Subscription")
    @Delete(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean deleteSubscription(Optional<Authentication> authentication) {
        if (authentication.isPresent()) {
            Stripe.apiKey = propertiesProvider?.getProperty("apiKey")
            return billingEventService.cancelSubscription(authentication.get())
        }
        return false
    }

}
