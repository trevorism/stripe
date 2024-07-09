package com.trevorism.model

import com.stripe.model.Price
import com.stripe.model.Subscription
import com.stripe.model.SubscriptionItem
import com.stripe.model.SubscriptionItemCollection
import org.junit.jupiter.api.Test

class BillingSubscriptionTest {

    @Test
    void testFromNullSubscription() {
        BillingSubscription billingSubscription = BillingSubscription.from(null)
        assert !billingSubscription
    }

    @Test
    void testFromEmptySubscription() {
        Subscription subscription = new Subscription()
        BillingSubscription billingSubscription = BillingSubscription.from(subscription)
        assert !billingSubscription
    }

    @Test
    void testFrom(){
        Subscription subscription = new Subscription()
        subscription.id = "sub_123"
        subscription.customer = "cus_123"
        subscription.items = new SubscriptionItemCollection()
        subscription.items.data = [new SubscriptionItem()]
        subscription.items.data[0].price = new Price()
        subscription.items.data[0].price.unitAmountDecimal = 1000
        subscription.created = 1234567890
        subscription.currentPeriodEnd = 1234567890
        subscription.status = "active"

        BillingSubscription billingSubscription = BillingSubscription.from(subscription)
        assert billingSubscription.subscriptionId == "sub_123"
        assert billingSubscription.customerId == "cus_123"
        assert billingSubscription.amount == 10.0
        assert billingSubscription.createdDate == new Date(1234567890000)
        assert billingSubscription.renewalDate == new Date(1234567890000)
        assert billingSubscription.active
    }
}
