package controllers

import javax.inject._
import play.api.data.validation.ValidationError
import utils.Logging

//import akka.actor.ActorSystem
import play.api.mvc._

import com.braintreegateway.{BraintreeGateway, Environment, Transaction, TransactionRequest}
import javax.inject.Inject
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc
import play.api.mvc.{AbstractController, ControllerComponents}
import _root_.util.JsonFormatters
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import com.braintreegateway._
import com.braintreegateway.CustomerRequest
import com.braintreegateway.PaymentMethodRequest
import _root_.org.joda.time.DateTime
import play.api.mvc._
import play.api.Mode
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConversions._

case class BraintreePaymentNonce (
  val paymentNonce: String
)

@Singleton
class Application @Inject()(val jsonFormatters: JsonFormatters,
                            val cc: ControllerComponents) extends AbstractController(cc)
  with Logging {

  import jsonFormatters._

  def index = Action { implicit request =>
    val clientBraintreeToken = queryClientToken()

    Ok(views.html.index(clientBraintreeToken))
  }

  def queryClientToken(): String = {
    val braintreeClientToken: String = try {
      val gateway = getBraintreeGateway()
      gateway.clientToken().generate()
    } catch {
      case e: Exception => ""
    }
    braintreeClientToken
  }

  private def queryBraintreeEnvironmentMode(): Environment = {
    //Environment.PRODUCTION
    Environment.SANDBOX
  }

  private def getBraintreeGateway() = {
    new BraintreeGateway(
      queryBraintreeEnvironmentMode,
      "zxb9qhkjv6dcwsmn",                // merchantId
      "d8pc8t5r3hwcq4hx",                 // publicKey
      "d38c82fa28c30aec451e8d325a869f25"  // privateKey
    )
  }

  def payByBraintree() = Action { implicit request =>
    info("request.body.asJson: "+request.body.asJson)
    request.body.asJson match {
      case None => BadRequest("enter data")
      case Some(paymentDataJson) =>
        paymentDataJson.validate[BraintreePaymentNonce]match {
          case paymentData: JsSuccess[BraintreePaymentNonce] =>
            val resultMessage = processBraintreePayment(paymentData.get.paymentNonce)
            Ok(resultMessage)
          case e: JsError => BadRequest(JsError.toJson(e))
        }
    }
  }

  private def processBraintreePayment(nonceFromTheClient: String): String = {
    val gateway = getBraintreeGateway()

    // case 1
    // direct copy from
    // https://github.com/braintree/braintree_java
    // results:
    // with test data: payment success
    // with real data: Error processing transaction. Status: PROCESSOR_DECLINED.  Code: 2026.  Text: Invalid Merchant ID
    /*val request = new TransactionRequest()
      .amount(BigDecimal(1.0).bigDecimal)
      .paymentMethodNonce(nonceFromTheClient)
      .options
        .submitForSettlement(true)
        .done

    val result = gateway.transaction().sale(request)

    if ( !result.isSuccess() ) {
      if (result.getTransaction() != null) {
        val transaction: Transaction = result.getTransaction()
        info(s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}")
        s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}"
      } else {
        val errors = result.getErrors.getAllDeepValidationErrors.map(_.getMessage).mkString(", ")
        errors
      }
    } else {
      "payment success"
    }*/

    // case 2
    // copy from
    // https://github.com/braintree/braintree_java
    // but add customer fields
    // results:
    // with test data: payment success
    // with real data: Error processing transaction. Status: PROCESSOR_DECLINED.  Code: 2026.  Text: Invalid Merchant ID
    /*val request = new TransactionRequest()
      .amount(BigDecimal(1.0).bigDecimal)
      .paymentMethodNonce(nonceFromTheClient)
      .customer
        .firstName("CustomerTestFirstName")
        .lastName("CustomerTestLastName")
        .done
      .options
        .submitForSettlement(true)
        .done

    val result = gateway.transaction().sale(request)

    if ( !result.isSuccess() ) {
      if (result.getTransaction() != null) {
        val transaction: Transaction = result.getTransaction()
        info(s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}")
        s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}"
      } else {
        val errors = result.getErrors.getAllDeepValidationErrors.map(_.getMessage).mkString(", ")
        errors
      }
    } else {
      "payment success"
    }*/

    // case 3
    // copy from
    // https://github.com/braintree/braintree_java
    // but before payment create Braintree customer with setting default payment method
    // with test data: payment success
    // with real data: Error processing transaction. Status: PROCESSOR_DECLINED.  Code: 2026.  Text: Invalid Merchant ID
    val contactRequest = new CustomerRequest()
      .firstName("CustomerTestFirstName")
      .lastName("CustomerTestLastName")
      .paymentMethodNonce(nonceFromTheClient)

    val result = gateway.customer.create(contactRequest)

    if ( result.isSuccess() ) {
      val brainTreeCustomerId = result.getTarget.getId

      val request = new TransactionRequest()
        .amount(BigDecimal(1.0).bigDecimal)
        .customerId(brainTreeCustomerId)
        .options
          .submitForSettlement(true)
          .done

      val result2 = gateway.transaction().sale(request)

      if ( !result2.isSuccess() ) {
        if (result2.getTransaction() != null) {
          val transaction: Transaction = result2.getTransaction()
          info(s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}")
          s"Error processing transaction. Status: ${transaction.getStatus}.  Code: ${transaction.getProcessorResponseCode}.  Text: ${transaction.getProcessorResponseText}"
        } else {
          val errors = result2.getErrors.getAllDeepValidationErrors.map(_.getMessage).mkString(", ")
          errors
        }
      } else {
        "payment success"
      }
    } else {
      "Failed to create Braintree Customer"
    }

  }

  def javascriptRoutes() = mvc.Action { implicit request =>
    Ok(
      play.api.routing.JavaScriptReverseRouter("jsRoutes")(
        controllers.routes.javascript.Application.payByBraintree
      )
    ).as(JAVASCRIPT)
  }

}
