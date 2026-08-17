package com.trevorism.model

class BillingEvent {

    static final String PAYMENT_INTENT_OBJECT = "payment_intent"
    static final String CHECKOUT_SESSION_OBJECT = "checkout.session"
    static final String PAID_PAYMENT_STATUS = "paid"
    static final String SUBSCRIPTION_MODE = "subscription"

    String id
    String userId
    String tenantId

    String billingId
    Date billingDate
    double billingAmount
    String billingCustomer
    String billingStatus

    static BillingEvent from(StripeCallbackEvent stripeCallbackEvent) {
        StripeCallbackEventDataObject data = stripeCallbackEvent?.data?.object
        if (!data) {
            return null
        }

        switch (data.object) {
            case PAYMENT_INTENT_OBJECT:
                return fromPaymentIntent(data)
            case CHECKOUT_SESSION_OBJECT:
                return fromCheckoutSession(data)
            default:
                return null
        }
    }

    private static BillingEvent fromPaymentIntent(StripeCallbackEventDataObject data) {
        return new BillingEvent([
                userId: data?.metadata?.get("userId") as String,
                tenantId: data?.metadata?.get("tenantId") as String,
                billingId: data.id,
                billingDate: new Date(data.created * 1000),
                billingAmount: ((double) data.amount / 100d),
                billingCustomer: data.customer,
                billingStatus: data.status
        ])
    }

    private static BillingEvent fromCheckoutSession(StripeCallbackEventDataObject data) {
        if (data.mode != SUBSCRIPTION_MODE || data.payment_status != PAID_PAYMENT_STATUS) {
            return null
        }

        return new BillingEvent([
                userId: data?.metadata?.get("userId") as String,
                tenantId: data?.metadata?.get("tenantId") as String,
                billingId: data.id,
                billingDate: new Date(data.created * 1000),
                billingAmount: ((double) data.amount_total / 100d),
                billingCustomer: data.customer,
                billingStatus: data.payment_status
        ])
    }
}
