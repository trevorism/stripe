Feature: Money-handling guards on the deployed Stripe API
  Every scenario here is refused by a guard that runs before any Stripe call,
  so this suite never creates a checkout session and never moves money.
  Each asserts the specific rejection status, so a renamed or removed route
  fails with a 404 instead of passing as "something went wrong".

  Scenario: The webhook refuses an unsigned payload
    Given the stripe application is alive
    When an unsigned billing event is posted to the webhook
    Then the billing request is rejected with status 500

  Scenario: The webhook refuses a forged signature
    Given the stripe application is alive
    When a billing event with a forged signature is posted to the webhook
    Then the billing request is rejected with status 500

  Scenario: A subscription may only be created at the supported price
    Given the stripe application is alive
    When an authenticated caller requests a subscription session at the wrong price
    Then the billing request is rejected with status 500

  Scenario: A one time payment below the floor is refused
    Given the stripe application is alive
    When an authenticated caller requests a payment session below the minimum
    Then the billing request is rejected with status 500

  Scenario: Subscription details are not readable anonymously
    Given the stripe application is alive
    When an anonymous caller reads the current subscription
    Then the billing request is rejected with status 401

  Scenario: Subscription lookup by billing customer is not readable anonymously
    Given the stripe application is alive
    When an anonymous caller looks up the subscription for a billing customer
    Then the billing request is rejected with status 401
