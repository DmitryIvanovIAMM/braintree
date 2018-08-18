package util

import controllers._
import javax.inject.Inject
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.Logging


class JsonFormatters @Inject()() extends Logging {

  // compiler do not compiles custom Read with single field
  // http://stackoverflow.com/questions/40679540/overloaded-method-value-read-cannot-be-applied-to-string-searchcontroller
  implicit val braintreePaymentNonceRead: Reads[BraintreePaymentNonce] = {
    ((JsPath \ "paymentNonce").read[String]
      .map(BraintreePaymentNonce.apply _))
  }

}
