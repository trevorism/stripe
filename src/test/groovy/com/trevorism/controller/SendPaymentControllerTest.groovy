package com.trevorism.controller

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ServerAuthentication
import org.junit.jupiter.api.Test

class SendPaymentControllerTest {

    @Test
    void testCreatePaymentIntentMetadata() {
        Authentication authentication = createSampleAuthentication()
        def map = SendPaymentController.createIdentityMetadata(authentication)
        assert map["userId"] == "test_id"
        assert map["tenantId"] == "test_tenant"
    }

    @Test
    void testCreatePaymentIntentMetadataEmptyAuth() {
        def map = SendPaymentController.createIdentityMetadata({ } as Authentication)
        assert !map
    }

    @Test
    void testCreatePaymentIntentMetadataAnonymous() {
        def map = SendPaymentController.createIdentityMetadata(null)
        assert !map
    }

    static Authentication createSampleAuthentication() {
        new ServerAuthentication("trevor", ["ROLE_USER"], [id:"test_id","tenant":"test_tenant"])
    }
}
