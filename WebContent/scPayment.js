let payment_form  = $("#paymentForm");

function handleTotalResult(resultData) {
    let totalAmount = jQuery("#amt");

    totalAmount.text("$" + resultData);
    console.log("result: ", resultData);

}

function handleInsertResult(resultData) {
    console.log("success");
    window.location.replace("confirmation.html");
}

function handlePaymentResult (resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        jQuery.ajax({
            // dataType: "json",  // Setting return data type
            method: "GET",// Setting request method
            url: "api/insert", // Setting request url, which is mapped by StarsServlet in Stars.java
            success: (resultData) => handleInsertResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
        });
    }

    else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);
    }
}
function submitPaymentForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    )
}

jQuery.ajax({
    // dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/total", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleTotalResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

payment_form.submit(submitPaymentForm);