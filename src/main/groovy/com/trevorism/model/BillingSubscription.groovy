package com.trevorism.model

import com.stripe.model.Subscription

class BillingSubscription {
    String customerId
    String subscriptionId
    double amount
    Date createdDate
    Date renewalDate
    boolean active

    static BillingSubscription from(Subscription subscription) {
        if (subscriptionIsMalformed(subscription))
            return null

        return new BillingSubscription([
                subscriptionId: subscription.id,
                customerId    : subscription.customer,
                amount        : subscription.items?.data?.get(0)?.price?.unitAmountDecimal / 100d,
                createdDate   : new Date(subscription.created * 1000),
                renewalDate   : new Date(subscription.currentPeriodEnd * 1000),
                active        : subscription.status == "active"
        ])
    }

    private static boolean subscriptionIsMalformed(Subscription subscription) {
        return subscription == null ||
                subscription.items?.data?.get(0)?.price?.unitAmountDecimal == null ||
                !subscription.created ||
                !subscription.currentPeriodEnd
    }
}
