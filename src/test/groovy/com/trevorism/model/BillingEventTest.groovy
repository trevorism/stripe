package com.trevorism.model

import com.google.gson.Gson
import org.junit.jupiter.api.Test

class BillingEventTest {

    private static final String sampleEventOne = """{
    "id": "pi_3PSTceKUPlXay6LP0NNoiJsy",
    "object": "payment_intent",
    "amount": 2000,
    "amount_capturable": 0,
    "amount_details": {
      "tip": {
      }
    },
    "amount_received": 2000,
    "application": null,
    "application_fee_amount": null,
    "automatic_payment_methods": null,
    "canceled_at": null,
    "cancellation_reason": null,
    "capture_method": "automatic",
    "charges": {
      "object": "list",
      "data": [
        {
          "id": "ch_3PSTceKUPlXay6LP08nQOiED",
          "object": "charge",
          "amount": 2000,
          "amount_captured": 2000,
          "amount_refunded": 0,
          "application": null,
          "application_fee": null,
          "application_fee_amount": null,
          "balance_transaction": "txn_3PSTceKUPlXay6LP0sIPFIp3",
          "billing_details": {
            "address": {
              "city": null,
              "country": null,
              "line1": null,
              "line2": null,
              "postal_code": null,
              "state": null
            },
            "email": null,
            "name": null,
            "phone": null
          },
          "calculated_statement_descriptor": "TREVORISM LLC",
          "captured": true,
          "created": 1718584837,
          "currency": "usd",
          "customer": null,
          "description": "(created by Stripe CLI)",
          "destination": null,
          "dispute": null,
          "disputed": false,
          "failure_balance_transaction": null,
          "failure_code": null,
          "failure_message": null,
          "fraud_details": {
          },
          "invoice": null,
          "livemode": false,
          "metadata": {
          },
          "on_behalf_of": null,
          "order": null,
          "outcome": {
            "network_status": "approved_by_network",
            "reason": null,
            "risk_level": "normal",
            "risk_score": 58,
            "seller_message": "Payment complete.",
            "type": "authorized"
          },
          "paid": true,
          "payment_intent": "pi_3PSTceKUPlXay6LP0NNoiJsy",
          "payment_method": "pm_1PSTceKUPlXay6LPvtkp1Bbm",
          "payment_method_details": {
            "card": {
              "amount_authorized": 2000,
              "brand": "visa",
              "checks": {
                "address_line1_check": null,
                "address_postal_code_check": null,
                "cvc_check": "pass"
              },
              "country": "US",
              "exp_month": 6,
              "exp_year": 2025,
              "extended_authorization": {
                "status": "disabled"
              },
              "fingerprint": "2MylrefBqSDO9nhl",
              "funding": "credit",
              "incremental_authorization": {
                "status": "unavailable"
              },
              "installments": null,
              "last4": "4242",
              "mandate": null,
              "multicapture": {
                "status": "unavailable"
              },
              "network": "visa",
              "network_token": {
                "used": false
              },
              "overcapture": {
                "maximum_amount_capturable": 2000,
                "status": "unavailable"
              },
              "three_d_secure": null,
              "wallet": null
            },
            "type": "card"
          },
          "radar_options": {
          },
          "receipt_email": null,
          "receipt_number": null,
          "receipt_url": "https://pay.stripe.com/receipts/payment/CAcaFwoVYWNjdF8xSGJjekdLVVBsWGF5NkxQKIWMvrMGMgZ2yk8eD3s6LBYR4CBXnf14vYm1B5na3RzAS3gYkxZK-jHG_ze_drtlmaoAsNEMcl95pEvA",
          "refunded": false,
          "refunds": {
            "object": "list",
            "data": [
            ],
            "has_more": false,
            "total_count": 0,
            "url": "/v1/charges/ch_3PSTceKUPlXay6LP08nQOiED/refunds"
          },
          "review": null,
          "shipping": {
            "address": {
              "city": "San Francisco",
              "country": "US",
              "line1": "510 Townsend St",
              "line2": null,
              "postal_code": "94103",
              "state": "CA"
            },
            "carrier": null,
            "name": "Jenny Rosen",
            "phone": null,
            "tracking_number": null
          },
          "source": null,
          "source_transfer": null,
          "statement_descriptor": null,
          "statement_descriptor_suffix": null,
          "status": "succeeded",
          "transfer_data": null,
          "transfer_group": null
        }
      ],
      "has_more": false,
      "total_count": 1,
      "url": "/v1/charges?payment_intent=pi_3PSTceKUPlXay6LP0NNoiJsy"
    },
    "client_secret": "pi_3PSTceKUPlXay6LP0NNoiJsy_secret_nuKWNfGbuWso6MGfHLxNIVmzt",
    "confirmation_method": "automatic",
    "created": 1718584836,
    "currency": "usd",
    "customer": null,
    "description": "(created by Stripe CLI)",
    "invoice": null,
    "last_payment_error": null,
    "latest_charge": "ch_3PSTceKUPlXay6LP08nQOiED",
    "livemode": false,
    "metadata": {
    },
    "next_action": null,
    "on_behalf_of": null,
    "payment_method": "pm_1PSTceKUPlXay6LPvtkp1Bbm",
    "payment_method_configuration_details": null,
    "payment_method_options": {
      "card": {
        "installments": null,
        "mandate_options": null,
        "network": null,
        "request_three_d_secure": "automatic"
      }
    },
    "payment_method_types": [
      "card"
    ],
    "processing": null,
    "receipt_email": null,
    "review": null,
    "setup_future_usage": null,
    "shipping": {
      "address": {
        "city": "San Francisco",
        "country": "US",
        "line1": "510 Townsend St",
        "line2": null,
        "postal_code": "94103",
        "state": "CA"
      },
      "carrier": null,
      "name": "Jenny Rosen",
      "phone": null,
      "tracking_number": null
    },
    "source": null,
    "statement_descriptor": null,
    "statement_descriptor_suffix": null,
    "status": "succeeded",
    "transfer_data": null,
    "transfer_group": null
  }"""
    private static final String sampleEventTwo = """{
    "id": "pi_3PXVudKUPlXay6LP0fEuw854",
    "object": "payment_intent",
    "amount": 1000,
    "amount_capturable": 0,
    "amount_details": {
      "tip": {
      }
    },
    "amount_received": 1000,
    "application": null,
    "application_fee_amount": null,
    "automatic_payment_methods": null,
    "canceled_at": null,
    "cancellation_reason": null,
    "capture_method": "automatic",
    "charges": {
      "object": "list",
      "data": [
        {
          "id": "ch_3PXVudKUPlXay6LP01k6lo2y",
          "object": "charge",
          "amount": 1000,
          "amount_captured": 1000,
          "amount_refunded": 0,
          "application": null,
          "application_fee": null,
          "application_fee_amount": null,
          "balance_transaction": "txn_3PXVudKUPlXay6LP0rxHq0Ru",
          "billing_details": {
            "address": {
              "city": null,
              "country": "US",
              "line1": null,
              "line2": null,
              "postal_code": "22022",
              "state": null
            },
            "email": "another@trevorism.com",
            "name": "Charles Trevor",
            "phone": null
          },
          "calculated_statement_descriptor": "TREVORISM LLC",
          "captured": true,
          "created": 1719785280,
          "currency": "usd",
          "customer": "cus_QOIVPo30vRL80c",
          "description": "Subscription creation",
          "destination": null,
          "dispute": null,
          "disputed": false,
          "failure_balance_transaction": null,
          "failure_code": null,
          "failure_message": null,
          "fraud_details": {
          },
          "invoice": "in_1PXVudKUPlXay6LP3HFgQVN3",
          "livemode": false,
          "metadata": {
          },
          "on_behalf_of": null,
          "order": null,
          "outcome": {
            "network_status": "approved_by_network",
            "reason": null,
            "risk_level": "normal",
            "risk_score": 62,
            "seller_message": "Payment complete.",
            "type": "authorized"
          },
          "paid": true,
          "payment_intent": "pi_3PXVudKUPlXay6LP0fEuw854",
          "payment_method": "pm_1PXVucKUPlXay6LPjuEAwyjW",
          "payment_method_details": {
            "card": {
              "amount_authorized": 1000,
              "brand": "visa",
              "checks": {
                "address_line1_check": null,
                "address_postal_code_check": "pass",
                "cvc_check": "pass"
              },
              "country": "US",
              "exp_month": 12,
              "exp_year": 2034,
              "extended_authorization": {
                "status": "disabled"
              },
              "fingerprint": "2MylrefBqSDO9nhl",
              "funding": "credit",
              "incremental_authorization": {
                "status": "unavailable"
              },
              "installments": null,
              "last4": "4242",
              "mandate": null,
              "multicapture": {
                "status": "unavailable"
              },
              "network": "visa",
              "network_token": {
                "used": false
              },
              "overcapture": {
                "maximum_amount_capturable": 1000,
                "status": "unavailable"
              },
              "three_d_secure": null,
              "wallet": {
                "dynamic_last4": null,
                "link": {
                },
                "type": "link"
              }
            },
            "type": "card"
          },
          "radar_options": {
          },
          "receipt_email": null,
          "receipt_number": null,
          "receipt_url": "https://pay.stripe.com/receipts/invoices/CAcaFwoVYWNjdF8xSGJjekdLVVBsWGF5NkxQKMKuh7QGMgYdnPHhRdE6LBZCDJmmfzB3OnVyK_ljKuZdq5AzPpm3oEIUJ2Lh29MIa1xBOO1riMOnPPk8?s=ap",
          "refunded": false,
          "refunds": {
            "object": "list",
            "data": [
            ],
            "has_more": false,
            "total_count": 0,
            "url": "/v1/charges/ch_3PXVudKUPlXay6LP01k6lo2y/refunds"
          },
          "review": null,
          "shipping": null,
          "source": null,
          "source_transfer": null,
          "statement_descriptor": null,
          "statement_descriptor_suffix": null,
          "status": "succeeded",
          "transfer_data": null,
          "transfer_group": null
        }
      ],
      "has_more": false,
      "total_count": 1,
      "url": "/v1/charges?payment_intent=pi_3PXVudKUPlXay6LP0fEuw854"
    },
    "client_secret": "pi_3PXVudKUPlXay6LP0fEuw854_secret_jggeUcaG6muAeodC0ledeVq9j",
    "confirmation_method": "automatic",
    "created": 1719785279,
    "currency": "usd",
    "customer": "cus_QOIVPo30vRL80c",
    "description": "Subscription creation",
    "invoice": "in_1PXVudKUPlXay6LP3HFgQVN3",
    "last_payment_error": null,
    "latest_charge": "ch_3PXVudKUPlXay6LP01k6lo2y",
    "livemode": false,
    "metadata": {
    },
    "next_action": null,
    "on_behalf_of": null,
    "payment_method": "pm_1PXVucKUPlXay6LPjuEAwyjW",
    "payment_method_configuration_details": null,
    "payment_method_options": {
      "card": {
        "installments": null,
        "mandate_options": null,
        "network": null,
        "request_three_d_secure": "automatic",
        "setup_future_usage": "off_session"
      },
      "cashapp": {
      }
    },
    "payment_method_types": [
      "card",
      "cashapp"
    ],
    "processing": null,
    "receipt_email": null,
    "review": null,
    "setup_future_usage": "off_session",
    "shipping": null,
    "source": null,
    "statement_descriptor": null,
    "statement_descriptor_suffix": null,
    "status": "succeeded",
    "transfer_data": null,
    "transfer_group": null
  }"""
    private static final String sampleEventThree = """{
    "id": "pi_3PSeUoKUPlXay6LP16myN6Ch",
    "object": "payment_intent",
    "amount": 499,
    "amount_capturable": 0,
    "amount_details": {
      "tip": {
      }
    },
    "amount_received": 499,
    "application": null,
    "application_fee_amount": null,
    "automatic_payment_methods": null,
    "canceled_at": null,
    "cancellation_reason": null,
    "capture_method": "automatic_async",
    "charges": {
      "object": "list",
      "data": [
        {
          "id": "ch_3PSeUoKUPlXay6LP1yf9EoTF",
          "object": "charge",
          "amount": 499,
          "amount_captured": 499,
          "amount_refunded": 0,
          "application": null,
          "application_fee": null,
          "application_fee_amount": null,
          "balance_transaction": null,
          "billing_details": {
            "address": {
              "city": null,
              "country": "US",
              "line1": null,
              "line2": null,
              "postal_code": "22003",
              "state": null
            },
            "email": "trevorism@gmail.com",
            "name": "Trevor Brooks",
            "phone": null
          },
          "calculated_statement_descriptor": "TREVORISM LLC",
          "captured": true,
          "created": 1718626634,
          "currency": "usd",
          "customer": null,
          "description": null,
          "destination": null,
          "dispute": null,
          "disputed": false,
          "failure_balance_transaction": null,
          "failure_code": null,
          "failure_message": null,
          "fraud_details": {
          },
          "invoice": null,
          "livemode": true,
          "metadata": {
          },
          "on_behalf_of": null,
          "order": null,
          "outcome": {
            "network_status": "approved_by_network",
            "reason": null,
            "risk_level": "normal",
            "seller_message": "Payment complete.",
            "type": "authorized"
          },
          "paid": true,
          "payment_intent": "pi_3PSeUoKUPlXay6LP16myN6Ch",
          "payment_method": "pm_1PSeUnKUPlXay6LP2uSQ2TKj",
          "payment_method_details": {
            "card": {
              "amount_authorized": 499,
              "brand": "amex",
              "checks": {
                "address_line1_check": null,
                "address_postal_code_check": "pass",
                "cvc_check": "pass"
              },
              "country": "US",
              "exp_month": 12,
              "exp_year": 2024,
              "extended_authorization": {
                "status": "disabled"
              },
              "fingerprint": "7LTwPgpJDtndXZDQ",
              "funding": "credit",
              "incremental_authorization": {
                "status": "unavailable"
              },
              "installments": null,
              "last4": "1008",
              "mandate": null,
              "multicapture": {
                "status": "unavailable"
              },
              "network": "amex",
              "network_token": {
                "used": false
              },
              "overcapture": {
                "maximum_amount_capturable": 499,
                "status": "unavailable"
              },
              "three_d_secure": null,
              "wallet": null
            },
            "type": "card"
          },
          "radar_options": {
          },
          "receipt_email": null,
          "receipt_number": null,
          "receipt_url": "https://pay.stripe.com/receipts/payment/CAcQARoXChVhY2N0XzFIYmN6R0tVUGxYYXk2TFAozNLAswYyBleA9IrhmzosFhs7idNVGYpCRNTqF8tKAlHrIuxBzwmg-7xhmhYr_8nMoUx3hV10oaekFJY",
          "refunded": false,
          "refunds": {
            "object": "list",
            "data": [
            ],
            "has_more": false,
            "total_count": 0,
            "url": "/v1/charges/ch_3PSeUoKUPlXay6LP1yf9EoTF/refunds"
          },
          "review": null,
          "shipping": null,
          "source": null,
          "source_transfer": null,
          "statement_descriptor": null,
          "statement_descriptor_suffix": null,
          "status": "succeeded",
          "transfer_data": null,
          "transfer_group": null
        }
      ],
      "has_more": false,
      "total_count": 1,
      "url": "/v1/charges?payment_intent=pi_3PSeUoKUPlXay6LP16myN6Ch"
    },
    "client_secret": "pi_3PSeUoKUPlXay6LP16myN6Ch_secret_M86vp8gvXfU3DfocBD9lr1l6g",
    "confirmation_method": "automatic",
    "created": 1718626634,
    "currency": "usd",
    "customer": null,
    "description": null,
    "invoice": null,
    "last_payment_error": null,
    "latest_charge": "ch_3PSeUoKUPlXay6LP1yf9EoTF",
    "livemode": true,
    "metadata": {
    },
    "next_action": null,
    "on_behalf_of": null,
    "payment_method": "pm_1PSeUnKUPlXay6LP2uSQ2TKj",
    "payment_method_configuration_details": null,
    "payment_method_options": {
      "card": {
        "installments": null,
        "mandate_options": null,
        "network": null,
        "request_three_d_secure": "automatic"
      }
    },
    "payment_method_types": [
      "card"
    ],
    "processing": null,
    "receipt_email": null,
    "review": null,
    "setup_future_usage": null,
    "shipping": null,
    "source": null,
    "statement_descriptor": null,
    "statement_descriptor_suffix": null,
    "status": "succeeded",
    "transfer_data": null,
    "transfer_group": null
  }"""

    @Test
    void testSampleOne() {
        Gson gson = new Gson()
        StripeCallbackEventDataObject stripeCallback = gson.fromJson(sampleEventOne, StripeCallbackEventDataObject)
        StripeCallbackEvent sce = new StripeCallbackEvent(data: new StripeCallbackEventData(object: stripeCallback))
        BillingEvent billingEvent = BillingEvent.from(sce)
        assert billingEvent != null
        assert !billingEvent.userId
        assert !billingEvent.tenantId
        assert billingEvent.billingId == "pi_3PSTceKUPlXay6LP0NNoiJsy"
        assert billingEvent.billingDate
        assert billingEvent.billingAmount == 20.0d
        assert billingEvent.billingCustomer == null
        assert billingEvent.billingStatus == "succeeded"
    }

    @Test
    void testSampleTwo(){
        Gson gson = new Gson()
        StripeCallbackEventDataObject stripeCallback = gson.fromJson(sampleEventTwo, StripeCallbackEventDataObject)
        StripeCallbackEvent sce = new StripeCallbackEvent(data: new StripeCallbackEventData(object: stripeCallback))
        BillingEvent billingEvent = BillingEvent.from(sce)
        assert billingEvent != null
        assert !billingEvent.userId
        assert !billingEvent.tenantId
        assert billingEvent.billingId == "pi_3PXVudKUPlXay6LP0fEuw854"
        assert billingEvent.billingDate
        assert billingEvent.billingAmount == 10.0d
        assert billingEvent.billingCustomer == "cus_QOIVPo30vRL80c"
        assert billingEvent.billingStatus == "succeeded"
    }

    @Test
    void testSampleThree(){
        Gson gson = new Gson()
        StripeCallbackEventDataObject stripeCallback = gson.fromJson(sampleEventThree, StripeCallbackEventDataObject)
        StripeCallbackEvent sce = new StripeCallbackEvent(data: new StripeCallbackEventData(object: stripeCallback))
        BillingEvent billingEvent = BillingEvent.from(sce)
        assert billingEvent != null
        assert !billingEvent.userId
        assert !billingEvent.tenantId
        assert billingEvent.billingId == "pi_3PSeUoKUPlXay6LP16myN6Ch"
        assert billingEvent.billingDate
        assert billingEvent.billingAmount == 4.99d
        assert billingEvent.billingCustomer == null
        assert billingEvent.billingStatus == "succeeded"
    }
}
