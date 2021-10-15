
/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getIndex(resultData, movieId){
    for(let i = 0; i < resultData.length; i++){
        if(resultData[i]["movie_id"] === movieId){
            return i;
        }
    }

}

function handleAddToCartSuccess(resultData) {
    alert("success");
    console.log("success: " + resultData);
}



/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleSingleMovieResult(resultData) {
    let index = getIndex(resultData, movieId);

    let singleMovieTitle = jQuery("#title");
    let singleMovieDirector = jQuery("#director");
    let singleMovieYear = jQuery("#year");
    let singleMovieRating = jQuery("#rating");
    let singleMovieGenres = jQuery("#genres");
    let singleMovieStars = jQuery("#stars");
    let middle1 = jQuery("#middle1");
    let middle2 = jQuery("#middle2");
    let middle3 = jQuery("#middle3");
    let backBtn = document.getElementsByClassName("backBtn")[0];

    let addToCartBtn = document.getElementsByClassName("addToCartBtn")[0];
    addToCartBtn.id = resultData[0]["movie_id"];

    let currentURL = window.location.href;
    let queryList = currentURL.split("?")[1];
    let sepQueryList = queryList.split("&");
    sepQueryList.shift();
    let queryListNoID = "?" + sepQueryList.join("&");
    backBtn.setAttribute('href', "index.html" + queryListNoID);

    singleMovieTitle.text(resultData[index]["movie_title"]);
    singleMovieDirector.text(resultData[index]["movie_director"]);
    singleMovieYear.text(resultData[index]["movie_year"]);
    if (resultData[index]["rating"] != null) {
        singleMovieRating.text(resultData[index]["rating"]);
    }
    else {
        singleMovieRating.text("N/A");
    }
    middle1.text("|");
    middle2.text("|");
    middle3.text("|");

    let genreStr = "";
    resultData[index]["movie_genres"].sort()
    for (let j=0; j < resultData[index]["movie_genres"].length; j++){
        genreStr += '<a href="index.html?genre=' + resultData[index]['movie_genres'][j] + '">';
        genreStr += resultData[index]['movie_genres'][j];
        genreStr += '</a>';
    }

    singleMovieGenres.append(genreStr);

    for (let i = 0; i < resultData[index]["movie_stars"].length; i++){
        let rowHTML = "";
        rowHTML += '<a href="single-star-page.html?id=' + resultData[index]['movie_star_ids'][i] + '&' + queryListNoID.substring(1) + '">';
        rowHTML += resultData[index]["movie_stars"][i];
        rowHTML += '</a>';

        singleMovieStars.append(rowHTML);
    }


    // console.log("handleResult: populating movie table from resultData");
    let addToCartBtnEL = document.getElementsByClassName("addToCartBtn")[0];
    addToCartBtnEL.addEventListener("click", function(){
        jQuery.ajax({
            method: "GET",// Setting request method
            url: "api/items?newItem=" + addToCartBtn.id,
            success: (resultData) => handleAddToCartSuccess(resultData)// Setting callback function to handle data returned successfully by the SingleStarServlet
        });
    });

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSingleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});