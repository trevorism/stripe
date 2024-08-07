package com.trevorism.controller

import com.google.gson.Gson
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.ClasspathBasedPropertiesProvider
import com.trevorism.PropertiesProvider
import com.trevorism.model.BillingSubscription
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
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.apache.hc.client5.http.HttpResponseException
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
        SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
                .putAllMetadata(SendPaymentController.createPaymentIntentMetadata(authentication))
                .build()

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(paymentRequest.successCallbackUrl)
                .setCancelUrl(paymentRequest.failureCallbackUrl)
                .addLineItem(lineItem)
                .setSubscriptionData(subscriptionData)

        Session session = Session.create(builder.build())
        return [id: session.getId()]
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Get a Stripe Subscription for this user **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    BillingSubscription getSubscription(Authentication authentication) {
        try{
            return billingEventService.getSubscription(authentication)
        }catch (Exception e){
            throw new HttpResponseException(404, e.message)
        }
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Cancel a Stripe Subscription for this user **Secure")
    @Delete(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    boolean deleteSubscription(Authentication authentication) {
        return billingEventService.cancelSubscription(authentication)
    }

}
