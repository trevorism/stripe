package com.trevorism.model

class BillingEvent {
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
        if(!data){
            return null
        }
        return new BillingEvent([
                userId: data?.metadata?.get("userId") as String,
                tenantId: data?.metadata?.get("tenantId") as String,
                billingId: data.id,
                billingDate: new Date(data.created * 1000),
                billingAmount: ((double)data.amount / 100d),
                billingCustomer: data.customer,
                billingStatus: data.status
        ])
    }
}
