$(document).ready(function() {
  addInputMasks();
  console.log("braintreeClientToken: ",braintreeClientToken);
});


function addInputMasks() {
  $('#ccNumber').inputmask({
    mask: ccMask,
    greedy: false,
    insertMode: false,
    definitions: { '#': { validator: "[0-9]", cardinality: 1}} });

  $('#cvcNumber').inputmask({
    mask: cvcMask,
    greedy: false,
    insertMode: false,
    definitions: { '#': { validator: "[0-9]", cardinality: 1}} });
}

var ccMask = [{ "mask": "#### #### #### ####"}];
var cvcMask = [{ "mask": "####"}];


function handlePayViaBraintree() {
  var ccNumber = getCCNumberFromForm('ccNumber');
  var cvcNumber = getCVCNumberFromForm('cvcNumber');
  var expMonthNumber = document.getElementById('expMonth').value;
  var expYearNumber = document.getElementById('expYear').value;
  var holderName = document.getElementById('nameOnCCCard').value;
  var expirationDate = expMonthNumber + '/' + expYearNumber;
  braintree.client.create({
    authorization: braintreeClientToken
  }, function (createErr, clientInstance) {
    var data = {
      creditCard: {
        number: ccNumber,
        cvv: cvcNumber,
        expirationDate: expirationDate,
        cardholderName: holderName,
        options: {
          validate: true
        }
      }
    };
    console.log("data: ",data);

    // Warning: For a merchant to be eligible for the easiest level of PCI compliance (SAQ A),
    // payment fields cannot be hosted on your checkout page.
    // For an alternative to the following, use Hosted Fields.
    clientInstance.request({
      endpoint: 'payment_methods/credit_cards',
      method: 'post',
      data: data
    }, function (requestErr, response) {
      // More detailed example of handling API errors: https://codepen.io/braintree/pen/MbwjdM
      if (requestErr) {
        return;
      } else {
        var token = response.creditCards[0].nonce;
        console.log("token: ",token);
        postPayment(token);
      }
    });
  });
}

function postPayment(token) {
  var data =  new Object();
  data.paymentNonce = token;
  jsRoutes.controllers.Application.payByBraintree().ajax({
    type: "POST",
    data: JSON.stringify(data),
    contentType: "application/json; charset=utf-8",
    async: true,
    success: function (data, status, request) {
      console.log("payment done: ",data);
    },
    error: function (data) {
      console.log("payment error: ",data)
    }
  });
}

function getCCNumberFromForm(id) {
  return document.getElementById(id).value.replace(/\s/g, '').replace(/_/g, '');
}

function getCVCNumberFromForm(id) {
  return document.getElementById(id).value.trim().replace(/_/g, '');
}
