let add_star = $("#add_star");
add_star.submit(submitAddStarForm);

let add_movie = $("#add_movie");
add_movie.submit(submitAddMovieForm);

function handleAddStar(resultDataString){
    console.log(resultDataString);
    alert(resultDataString["message"][0]);
}


function handleAddMovie(resultDataString){
    console.log(resultDataString);
    alert(resultDataString["message"][0]);
}



function submitAddMovieForm(formSubmitEvent){
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/DBAddMovie", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: add_movie.serialize(),
            success: handleAddMovie
        }
    );
}


function submitAddStarForm(formSubmitEvent){
    console.log("add star submit form");
    formSubmitEvent.preventDefault();

    console.log(add_star.serialize());

    $.ajax(
        "api/DBAddStar", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: add_star.serialize(),
            success: handleAddStar
        }
    );
}



function loadMetadata(resultDataString){
    console.log(resultDataString);
    let metadataTable = jQuery("#metadataBody");
    for (let i = 0; i < resultDataString[0]["tableNames"].length; i++) {

        if(resultDataString[0]['tableNames'][i] !== "customers_backup" && resultDataString[0]['tableNames'][i] != "employees_backup") {
            let rowHTML = "";
            rowHTML += "<tr>";
            rowHTML +=
                "<td>" + resultDataString[0]['tableNames'][i] + "</td>";

            let fields = resultDataString[0]["tableSchemas"][i].join(", ");

            rowHTML += "<td>" + fields + "</td>";
            rowHTML += "</tr>";

            // Append the row created to the table body, which will refresh the page
            metadataTable.append(rowHTML);
        }

    }
}






// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/_dashboard", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => loadMetadata(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});