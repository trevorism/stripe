package com.trevorism.model

class PaymentRequest {

    String name
    double dollars
    String successCallbackUrl = "https://trevorism.com"
    String failureCallbackUrl = "https://trevorism.com/contact"

}
