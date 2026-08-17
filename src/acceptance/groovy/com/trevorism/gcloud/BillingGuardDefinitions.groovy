package com.trevorism.gcloud

import com.trevorism.http.HttpClient
import com.trevorism.http.JsonHttpClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.http.util.InvalidRequestException
import com.trevorism.https.SecureHttpClient
import org.apache.hc.client5.http.HttpResponseException

this.metaClass.mixin(io.cucumber.groovy.Hooks)
this.metaClass.mixin(io.cucumber.groovy.EN)

HttpClient httpClient = new JsonHttpClient()
SecureHttpClient appClientSecureHttpClient = new AppClientSecureHttpClient()
String guardBaseUrl = System.getenv("ACCEPTANCE_BASE_URL") ?: "https://stripe.trade.trevorism.com"

String samplePayload = """{"id":"evt_acceptance","object":"event","type":"checkout.session.completed",""" +
        """"data":{"object":{"id":"cs_acceptance","object":"checkout.session","mode":"subscription",""" +
        """"payment_status":"paid","amount_total":1000,"created":1719785270,"metadata":{}}}}"""

int lastStatus

Closure<Integer> statusOf = { Closure call ->
    try {
        call.call()
        return 200
    } catch (InvalidRequestException e) {
        return e.statusCode
    } catch (HttpResponseException e) {
        return e.statusCode
    } catch (Exception ignored) {
        return -1
    }
}

When(/an unsigned billing event is posted to the webhook/) { ->
    lastStatus = statusOf { httpClient.post("${guardBaseUrl}/api/billing/webhook", samplePayload) }
}

When(/a billing event with a forged signature is posted to the webhook/) { ->
    Map<String, String> headers = ["Stripe-Signature": "t=1719785270,v1=" + ("0" * 64),
                                   "Content-Type"    : "application/json"]
    lastStatus = statusOf { httpClient.post("${guardBaseUrl}/api/billing/webhook", samplePayload, headers) }
}

When(/an authenticated caller requests a subscription session at the wrong price/) { ->
    String body = """{"name":"Acceptance Guard Check","dollars":5.00}"""
    lastStatus = statusOf { appClientSecureHttpClient.post("${guardBaseUrl}/api/subscription/session", body) }
}

When(/an authenticated caller requests a payment session below the minimum/) { ->
    String body = """{"name":"Acceptance Guard Check","dollars":0.50}"""
    lastStatus = statusOf { appClientSecureHttpClient.post("${guardBaseUrl}/api/payment/session", body) }
}

When(/an anonymous caller reads the current subscription/) { ->
    lastStatus = statusOf { httpClient.get("${guardBaseUrl}/api/subscription") }
}

When(/an anonymous caller looks up the subscription for a billing customer/) { ->
    lastStatus = statusOf { httpClient.get("${guardBaseUrl}/api/subscription/customer/cus_acceptance_not_real") }
}

Then(/the billing request is rejected with status {int}/) { Integer expected ->
    assert lastStatus == expected
}
