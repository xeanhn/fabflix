
function handleMovieResult(resultData) {
    let receiptBox = jQuery("#receiptBox");
    receiptBox.append(resultData);

}

function updatePg(resultData){
    let receiptBox = jQuery("#receiptBox");
    receiptBox.empty();
}


function addQty(qtySpan){
    let movie_id = qtySpan.id;
    jQuery.ajax({
        method: "GET",// Setting request method
        url: "api/items?newItem=" + movie_id,
        success: (resultData) => updatePg(resultData)// Setting callback function to handle data returned successfully by the SingleStarServlet
    });
    jQuery.ajax({
        method: "GET",// Setting request method
        url: "api/shoppingCart", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet

    });


}

function subtractQty(qtySpan){

    let movie_id = qtySpan.id;
    jQuery.ajax({
        method: "GET",// Setting request method
        url: "api/items?removeItem=" + movie_id,
        success: (resultData) => updatePg(resultData)// Setting callback function to handle data returned successfully by the SingleStarServlet
    });
    jQuery.ajax({
        method: "GET",// Setting request method
        url: "api/shoppingCart", // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}

// isEmpty();
// updatePg();
jQuery.ajax({
    method: "GET",// Setting request method
    url: "api/shoppingCart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});