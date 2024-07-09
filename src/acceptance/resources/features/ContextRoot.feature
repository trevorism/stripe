Feature: Context Root of this API
  In order to use the Stripe API, it must be available

  Scenario: ContextRoot https
    Given the stripe application is alive
    When I navigate to https://stripe.trade.trevorism.com
    Then the API returns a link to the help page

  Scenario: Ping https
    Given the stripe application is alive
    When I navigate to /ping on https://stripe.trade.trevorism.com
    Then pong is returned, to indicate the service is alive

