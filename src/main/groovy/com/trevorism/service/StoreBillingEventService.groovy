package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stripe.Stripe
import com.stripe.model.Subscription
import com.trevorism.PropertiesProvider
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.ComplexFilter
import com.trevorism.data.model.filtering.FilterBuilder
import com.trevorism.data.model.filtering.FilterConstants
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.https.SecureHttpClientBase
import com.trevorism.https.token.ObtainTokenFromAuthServiceFromPropertiesFile
import com.trevorism.https.token.ObtainTokenFromParameter
import com.trevorism.model.BillingEvent
import com.trevorism.model.InternalTokenRequest
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StoreBillingEventService implements BillingEventService {

    private static final Logger log = LoggerFactory.getLogger(StoreBillingEventService)

    @Inject
    private PropertiesProvider propertiesProvider

    private HttpClient singletonClient = new JsonHttpClient()
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    @Override
    BillingEvent processBillingEvent(BillingEvent event) {
        try {
            log.info("Processing billing event ${event?.billingId}")
            Repository<BillingEvent> repository = createBillingEventRepository(event.tenantId)
            return repository.create(event)
        } catch (Exception e) {
            log.error("Unable to process billing event", e)
        }
        return null
    }

    private Repository<BillingEvent> createBillingEventRepository(String tenantId) {
        String token = getInternalToken(tenantId)
        SecureHttpClient internalHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromParameter(token)) { }
        Repository<BillingEvent> repository = new FastDatastoreRepository<>(BillingEvent.class, internalHttpClient)
        return repository
    }

    @Override
    boolean cancelSubscription(Authentication authentication) {
        Stripe.apiKey = propertiesProvider.getProperty("apiKey")
        String tenantId = authentication?.attributes?.get("tenant")
        String userId = authentication.attributes.get("id")
        Repository<BillingEvent> repository = createBillingEventRepository(tenantId)
        ComplexFilter complexFilter = new FilterBuilder().addFilter(new SimpleFilter("userId", FilterConstants.OPERATOR_EQUAL, userId)).build()
        List<BillingEvent> billingEventList = repository.filter(complexFilter)
        try {
            if (billingEventList) {
                Subscription subscription = Subscription.retrieve(billingEventList[0].billingCustomer)
                subscription.cancel()
                return true
            }
        } catch (Exception e) {
            log.error("Unable to cancel subscription", e)
        }
        return false
    }

    private String getInternalToken(String tenantId) {
        try {
            SecureHttpClient secureHttpClient = new SecureHttpClientBase(singletonClient, new ObtainTokenFromAuthServiceFromPropertiesFile()) {
            }
            String subject = propertiesProvider.getProperty("clientId")
            InternalTokenRequest tokenRequest = new InternalTokenRequest(subject: subject, tenantId: tenantId)
            return secureHttpClient.post("https://auth.trevorism.com/token/internal", gson.toJson(tokenRequest))
        } catch (Exception e) {
            log.error("Unable to get token", e)
        }
        return null
    }
}
