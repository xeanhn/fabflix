function handleConfirmationResult(resultData) {

    console.log(resultData[resultData.length - 1]['total'])

    let itemsDiv = jQuery("#items");
    let itemBox = "";
    for (let i = 0; i < resultData.length - 1; i++) {
        itemBox +=  "<div class='item' class='items'>" +
            "<span class='movieTitle'>" + resultData[i]["movie_title"] + "</span>"+
            "<span class='qty'> Qty: " + resultData[i]["quantity"] + "</span>" +
            "<span class='saleID'>Sale ID: " + resultData[i]["sale_id"] + "</span>" +
            "</div>";
    }

    itemsDiv.append(itemBox);
    let totalDiv = jQuery("#total");
    console.log(totalDiv);
    totalDiv.text("Total: $" + String(resultData[resultData.length - 1]["total"]));
    console.log(resultData);
}

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/confirmation", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleConfirmationResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
