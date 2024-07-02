package com.trevorism.controller

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.trevorism.PropertiesProvider
import com.trevorism.model.PaymentRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.*

@Controller("/api/payment")
class SendPaymentController {

    private static final Logger log = LoggerFactory.getLogger(SendPaymentController)

    @Inject
    private PropertiesProvider propertiesProvider

    @Tag(name = "Payment Operations")
    @Operation(summary = "Create a new Stripe Payment Session")
    @Post(value = "/session", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map createSession(@Body PaymentRequest paymentRequest, Authentication authentication) {
        Stripe.apiKey = propertiesProvider.getProperty("apiKey")
        if (paymentRequest.dollars < 0.99) {
            log.warn("Failed payment attempt of ${paymentRequest.dollars}")
            throw new RuntimeException("Unable to process; insufficient funds for payment")
        }

        ProductData productData = ProductData.builder().setName(paymentRequest.name).build()
        SessionCreateParams.LineItem.PriceData priceData = builder()
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

        if (authentication?.getAttributes()?.get("id")) {
            builder.putMetadata("userId", authentication.getAttributes().get("id"))
        }
        if (authentication?.getAttributes()?.get("tenant")) {
            builder.putMetadata("tenantId", authentication.getAttributes().get("tenant"))
        }

        Session session = Session.create(builder.build())
        return [id: session.getId()]
    }


}
