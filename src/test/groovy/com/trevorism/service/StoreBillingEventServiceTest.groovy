package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stripe.model.oauth.TokenResponse
import com.trevorism.PropertiesProvider
import com.trevorism.data.exception.DataOperationException
import com.trevorism.controller.SendPaymentControllerTest
import com.trevorism.http.HeadersHttpResponse
import com.trevorism.http.HttpClient
import com.trevorism.model.BillingEvent
import org.junit.jupiter.api.Test

class StoreBillingEventServiceTest {

    @Test
    void testProcessBillingEvent() {
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createTestHttpClient()
        BillingEvent event = createSampleBillingEvent()
        BillingEvent created = storeBillingEventService.processBillingEvent(event)
        assert event != null
        assert created != null
    }

    @Test
    void testGetCustomerIdFromAuthentication(){
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createTestHttpClient()
        String customerId = storeBillingEventService.getCustomerIdFromAuthentication(SendPaymentControllerTest.createSampleAuthentication())
        assert customerId == "cus_J9Q9Q9Q9Q9Q9Q9Q9"
    }

    @Test
    void testCreatePortalSessionRejectsABlankReturnUrl(){
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createTestHttpClient()

        try {
            storeBillingEventService.createPortalSession(SendPaymentControllerTest.createSampleAuthentication(), "   ")
            assert false
        } catch (IllegalArgumentException e) {
            assert e.message == "A return url is required"
        }
    }

    @Test
    void testProcessBillingEventRethrowsWhenTheEventCannotBeStored() {
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createDatastoreFailingHttpClient()

        try {
            storeBillingEventService.processBillingEvent(createSampleBillingEvent())
            assert false
        } catch (DataOperationException e) {
            assert e.message.contains("billingevent")
        }
    }

    @Test
    void testRedeliveredBillingEventIsNotRecordedTwice() {
        List<String> createdUrls = []
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createRecordingHttpClient([createSampleBillingEvent()], createdUrls)

        BillingEvent result = storeBillingEventService.processBillingEvent(createSampleBillingEvent())

        assert result.billingId == "evt_1H1J9vLzj6ZJ9Q9Q9"
        assert createdUrls.isEmpty()
    }

    @Test
    void testNewBillingEventIsRecorded() {
        List<String> createdUrls = []
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createRecordingHttpClient([], createdUrls)

        storeBillingEventService.processBillingEvent(createSampleBillingEvent())

        assert createdUrls == ["https://datastore.data.trevorism.com/object/billingevent"]
    }

    @Test
    void testMissingInternalTokenFailsInsteadOfCallingTheDatastoreWithoutOne() {
        List<String> createdUrls = []
        StoreBillingEventService storeBillingEventService = new StoreBillingEventService()
        storeBillingEventService.propertiesProvider = [getProperty: {key -> "x"}] as PropertiesProvider
        storeBillingEventService.singletonClient = createRecordingHttpClient([], createdUrls, null)

        try {
            storeBillingEventService.processBillingEvent(createSampleBillingEvent())
            assert false
        } catch (IllegalStateException e) {
            assert e.message.contains("internal token")
        }
        assert createdUrls.isEmpty()
    }

    private static HttpClient createRecordingHttpClient(List<BillingEvent> existingEvents, List<String> createdUrls,
                                                        String internalToken = "internal-token") {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
        Closure<String> respond = { String url ->
            if (url.endsWith("/token/internal")) {
                return internalToken
            }
            if (url.contains("/filter/billingevent")) {
                return gson.toJson(existingEvents)
            }
            if (url.contains("/object/billingevent")) {
                createdUrls << url
                return gson.toJson(createSampleBillingEvent())
            }
            return gson.toJson(new TokenResponse())
        }
        return [
                post: { String url, String body, Map<String, String> headers = null ->
                    String response = respond(url)
                    return headers == null ? response : new HeadersHttpResponse(response, headers)
                }
        ] as HttpClient
    }

    private static HttpClient createDatastoreFailingHttpClient() {
        String tokenJson = new Gson().toJson(new TokenResponse())
        Closure<String> respond = { String url ->
            if (url.contains("datastore")) {
                throw new RuntimeException("datastore unavailable")
            }
            return tokenJson
        }
        return [
                post: { String url, String body, Map<String, String> headers = null ->
                    String response = respond(url)
                    return headers == null ? response : new HeadersHttpResponse(response, headers)
                }
        ] as HttpClient
    }

    private static createTestHttpClient(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
        new HttpClient() {
            @Override
            String get(String s) {
                return null
            }

            @Override
            HeadersHttpResponse get(String s, Map<String, String> map) {
                return null
            }

            @Override
            String post(String s, String s1) {
                if(s == "https://auth.trevorism.com/token"){
                    return gson.toJson(new TokenResponse())
                }

                return gson.toJson(createSampleBillingEvent())
            }

            @Override
            HeadersHttpResponse post(String s, String s1, Map<String, String> map) {
                if(s == "https://datastore.data.trevorism.com/filter/billingevent"){
                    return new HeadersHttpResponse(gson.toJson([createSampleBillingEvent()]), map)
                }

                return new HeadersHttpResponse(gson.toJson(new TokenResponse()), map)
            }

            @Override
            String put(String s, String s1) {
                return null
            }

            @Override
            HeadersHttpResponse put(String s, String s1, Map<String, String> map) {
                return null
            }

            @Override
            String patch(String s, String s1) {
                return null
            }

            @Override
            HeadersHttpResponse patch(String s, String s1, Map<String, String> map) {
                return null
            }

            @Override
            String delete(String s) {
                return null
            }

            @Override
            HeadersHttpResponse delete(String s, Map<String, String> map) {
                return null
            }
        }
    }

    private static BillingEvent createSampleBillingEvent() {
        BillingEvent billingEvent = new BillingEvent()
        billingEvent.userId = "5070662116835328"
        billingEvent.tenantId = null
        billingEvent.billingId = "evt_1H1J9vLzj6ZJ9Q9Q9"
        billingEvent.billingDate = new Date()
        billingEvent.billingAmount = 100.0
        billingEvent.billingCustomer = "cus_J9Q9Q9Q9Q9Q9Q9Q9"
        billingEvent.billingStatus = "succeeded"
        return billingEvent
    }

}
