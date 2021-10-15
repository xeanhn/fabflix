
jQuery.ajax({
    // dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/getGenres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => displayGenres(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

jQuery.ajax({
    // dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/is-employee", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleIsEmployeeResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


let genreList = $("#genreList");
function displayGenres(resultData){
    let rowHTML = ""
    for (let i = 0; i < resultData["genres"].length; i++){
        rowHTML += "<a href='index.html?genre=" + resultData["genres"][i] +"'>";
        rowHTML += resultData["genres"][i];
        rowHTML += "</a>";
    }

    genreList.append(rowHTML);
    // <a href="index.html?genre=Action">Action</a>

}

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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleIsEmployeeResult(resultData) {
    if (resultData === true || resultData === "true") {
        document.getElementById("scBtn").classList.add("disabled");
        document.getElementsByClassName("cartImg")[0].classList.add("disabledImg");
    }

    else {
        document.getElementById("dbBtn").classList.add("disabled");
        document.getElementsByClassName("dbImg")[0].classList.add("disabledImg");
    }

}

function handleMovieResult(resultData) {
    if (resultData.length === 0) {
        return;
    }
    let movieTitle = jQuery("#title");
    let movieDirector = jQuery("#director");
    let movieYear = jQuery("#year");
    let movieRating = jQuery("#rating");
    let boxesDiv = jQuery("#boxes");

    let currentURL = window.location.href;
    let queryList = "&" + currentURL.split("?")[1]

    let movieCount = movieNum != null ? movieNum : resultData.length;
    let totalQueryCount = resultData[resultData.length - 1]["count"];

    // disable next button if no more queries after current page
    document.getElementById("nextBtn").disabled = totalQueryCount <= pageNum * movieNum;

    //disable previous button if current page is page 1
    document.getElementById("prevBtn").disabled = pageNum === "1" || pageNum === 1;

    for (let i = 0; i < Math.min(movieCount, resultData.length - 1); i++) {
        let movieBox = "";
        if(i+1 === Math.min(movieCount, resultData.length)){
            movieBox += "<div class=\"end movieBox\">";
        }
        else{
            movieBox += "<div class=\"movieBox\">";
        }

        movieBox += "<div class=\"topSection\">";
        movieBox += "<span class=\"top\">";
        movieBox += '<a class="movieTitle" href="single-movie.html?id=' + resultData[i]['movie_id'] + queryList + '">'
        movieBox += resultData[i]["movie_title"];
        movieBox += '</a>';
        movieBox += "</span>";
        movieBox += "<span class=\"top\"><button class='addToCart' id='" + resultData[i]['movie_id'] + "'>Add to Cart" + "</button></span>";
        movieBox += "</div>";
        movieBox += "<div class=\"middleSection\">";
        movieBox += "<span class=\"middle\">" + resultData[i]["movie_director"] + "</span>";
        movieBox += "<span class=\"middle divider\">|</span>";
        movieBox += "<span class=\"middle\">" + resultData[i]["movie_year"] + "</span>";
        movieBox += "<span class=\"middle divider\">|</span>";
        if(resultData[i]["rating"] != null){
            movieBox += "<span class=\"middle\">" + resultData[i]["rating"] + "</span>";
        }
        else {
            movieBox += "<span class=\"middle\">N/A</span>";
        }
        movieBox += "<span class=\"middle divider\">|</span>";


        movieBox += "<span class=\"middle\">";
        resultData[i]["movie_genres"].sort();
        for (let k = 0; k < resultData[i]["movie_genres"].length; k++){
            movieBox += '<a href="index.html?genre=' + resultData[i]["movie_genres"][k] + '">';
            movieBox += resultData[i]["movie_genres"][k];
            movieBox += '</a>';
        }

        movieBox += "</span>";


        movieBox += "</div>";

        for(let j = 0; j < resultData[i]["movie_stars"].length; j++){
            movieBox += "<span>";
            movieBox += '<a href="single-star-page.html?id=' + resultData[i]['movie_star_ids'][j] + queryList + '">'
            movieBox +=  resultData[i]["movie_stars"][j];
            movieBox += '</a>'
            movieBox += "</span>";
        }

        movieBox += "</div>";

        boxesDiv.append(movieBox);
    }

    // Handling adding to the cart
    const addToCartButtons = document.getElementsByClassName("addToCart");
    for (let i = 0; i < addToCartButtons.length; i++) {
        addToCartButtons[i].addEventListener('click', function() {
            jQuery.ajax({
                method: "GET",// Setting request method
                url: "api/items?newItem=" + addToCartButtons[i].id,
                success: (resultData) => handleAddToCartSuccess(resultData)// Setting callback function to handle data returned successfully by the SingleStarServlet
            });
        })
    }
}

function handleAddToCartSuccess(resultData) {
    alert("success");
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
let movieNum = getParameterByName('n');
let genre = getParameterByName('genre');
let char = getParameterByName('char');
let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let star = getParameterByName('star');
let sort = getParameterByName('sort');
let pageNum = getParameterByName('pg');
let ftsTitle = getParameterByName('ftstitle');


if (movieNum == null || pageNum == null) {
    if (window.location.href.includes("?")) {
        window.location.href += "&n=10&pg=1";
    }
    else {
        window.location.href += "?n=10&pg=1";
    }
}
function updateURL(paramToUpdate, paramValue){
    let currentURL = window.location.href;
    if (currentURL.includes("?")){
        let staticURL = currentURL.split("?")[0];
        let queryList = currentURL.split("?")[1].split("&");
        let didChange = false;
        let pgParamIndex = 0;
        for (let i = 0; i < queryList.length; i++){

            if ( (paramToUpdate === "n" && queryList[i].split("=")[0] === "n") ||
                (paramToUpdate === "sort" && queryList[i].split("=")[0] === "sort") ||
                (paramToUpdate === "pg" && queryList[i].split("=")[0] === "pg")) {

                let queryToChange = queryList[i];
                let newQuery = queryToChange.split("=");
                newQuery[1] = paramValue;
                newQuery = newQuery.join("=");
                queryList[i] = newQuery;
                didChange = true;
            }

            if (queryList[i].split("=")[0] === "pg" ) {
                pgParamIndex = i;
            }
        }

        if (!didChange){
            return staticURL + "?" + queryList.join("&") + "&" + paramToUpdate + "=" + paramValue;
        }
        else{
            if (paramToUpdate === "n") {
                let pgQuery = queryList[pgParamIndex].split("=");
                pgQuery[1] = "1";
                pgQuery = pgQuery.join("=");
                queryList[pgParamIndex] = pgQuery;
            }
            return staticURL + "?" + queryList.join("&");
        }

    } else{
        return currentURL + "?" + paramToUpdate + "=" + paramValue;
    }
}

const sortOptions = document.getElementById("sortOptions");
let nValue = movieNum != null ? movieNum : 10;

document.getElementById('sortOptions').value = nValue;
sortOptions.addEventListener("change", function(){
    let newURL = updateURL("n", sortOptions.value);
    window.location.href = newURL;
});

const sortDropDown = document.getElementById("sort");
document.getElementById('sort').value = sort != null ? sort : "null";
sortDropDown.addEventListener("change", function() {
    let newSortedURL = updateURL("sort", sortDropDown.value);
    window.location.href = newSortedURL;
});


let pageValue = pageNum != null ? pageNum : 1;
const prevBtn = document.getElementById("prevBtn");
prevBtn.addEventListener("click", function() {
    pageValue = parseInt(pageValue) - 1;
    let newURL = updateURL("pg", pageValue);
    window.location.href = newURL;
});
const nextBtn = document.getElementById("nextBtn");
nextBtn.addEventListener("click", function(){
    pageValue = parseInt(pageValue) + 1;
    let newURL = updateURL("pg", pageValue);
    window.location.href = newURL;
});

function createURL(){
    // let currentURL = window.location.href;
    if (movieNum === null){
        movieNum = 10;
    }
    return "api/movies?title=" + title + "&year=" + year + "&director=" + director
        + "&star=" + star + "&char=" + char + "&genre=" + genre + "&n="+ movieNum + "&sort=" + sort + "&pg=" + pageValue;
}



// Makes the HTTP GET request and registers on success callback function handleResult

if (ftsTitle !== null) {
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/ftsearch?title=" + ftsTitle +
            "&n="+ movieNum + "&sort=" + sort + "&pg=" + pageValue, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else {
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: createURL(), // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}


// --------- PROJECT 4 STUFF ---------


function handleLookup(query, doneCallback) {
    console.log("autocomplete search initiated: " + query)


    // TODO: if you want to check past query results first, you can do it here
    let unParsedData = localStorage.getItem(query);
    if (unParsedData != null) {
        console.log("using cached results")
        let jsonData = JSON.parse(unParsedData);

        let movieSuggestions = [];
        for (let i = 0; i < jsonData.length; i++) {
            movieSuggestions.push(jsonData[i]["value"])
        }

        console.log(movieSuggestions);

        doneCallback( {suggestions: jsonData})
    }
    else {
        console.log("sending AJAX request to backend Java Servlet")
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/autocomplete?query=" + escape(query),
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {

    // parse the string into JSON
    let jsonData = JSON.parse(data);

    let movieSuggestions = [];
    for (let i = 0; i < jsonData.length; i++) {
        movieSuggestions.push(jsonData[i]["value"])
    }

    console.log(movieSuggestions);

    // TODO: if you want to cache the result into a global variable you can do it here
    localStorage.setItem(query, JSON.stringify(jsonData))
    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieId"]
}

// function handleNormalSearch(query) {
//     // TODO: you should do normal search here
//     jQuery.ajax({
//         dataType: "json",  // Setting return data type
//         method: "GET",// Setting request method
//         url: "api/ftsearch?title=" + query +
//         "&n="+ movieNum + "&sort=" + sort + "&pg=" + pageValue, // Setting request url, which is mapped by StarsServlet in Stars.java
//         success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
//     });
// }

$('#ftsTitle').autocomplete({

    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },

    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,

});

// $('#ftsTitle').keypress(function(event) {
//     // keyCode 13 is the enter key
//     if (event.keyCode === 13) {
//         // pass the value of the input box to the handler function
//         handleNormalSearch($('#ftsTitle').val())
//     }
// })

// const ftsForm = document.getElementById('ftsForm');
// ftsForm.addEventListener('submit', function(){
//     handleNormalSearch($('#ftsTitle').val())
// })
