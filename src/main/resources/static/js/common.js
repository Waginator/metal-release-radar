function registerLogoutListener() {
    document.getElementById('logout-link').addEventListener('click', function(event) {
        event.preventDefault();
        document.getElementById('logout-form').submit();
    });
}

/**
 * Send ajax request to follow an artist
 * @param artistName    Artist to follow
 * @param artistId      Artist's discogs id
 * @param el            Button that was clicked
 * @returns {boolean}
 */
function followArtist(artistName,artistId,el){
    const followArtistRequest =
        {
            "artistName" : artistName,
            "artistDiscogsId" : artistId
        };
    const followArtistRequestJson = JSON.stringify(followArtistRequest);
    const csrfToken  = $("input[name='_csrf']").val();

    $.ajax({
        method: "POST",
        url: "/rest/v1/follow-artist",
        contentType: 'application/json',
        headers: {"X-CSRF-TOKEN": csrfToken},
        data: followArtistRequestJson,
        success: function(){
            el.childNodes[0].nodeValue = 'Unfollow';
            el.onclick = createOnClickFunctionFollowArtist(artistName,artistId,true,el);
            },
        error: function(e){
            console.log(e.message);
        }
    });

    return false;
}

/**
 * Send ajax request to unfollow an artist
 * @param artistName    Artist to unfollow
 * @param artistId      Artist's discogs id
 * @param el            Button that was clicked
 * @returns {boolean}
 */
function unfollowArtist(artistName,artistId,el){
    const followArtistRequest =
        {
            "artistName" : artistName,
            "artistDiscogsId" : artistId
        };
    const followArtistRequestJson = JSON.stringify(followArtistRequest);
    const csrfToken  = $("input[name='_csrf']").val();

    $.ajax({
        method: "DELETE",
        url: "/rest/v1/follow-artist",
        contentType: 'application/json',
        data: followArtistRequestJson,
        headers: {"X-CSRF-TOKEN": csrfToken},
        success: function(){
            el.childNodes[0].nodeValue = 'Follow';
            el.onclick = createOnClickFunctionFollowArtist(artistName,artistId,false,el);
        },
        error: function(e){
            console.log(e.message);
        }
    });

    return false;
}

/**
 * Send ajax request to search for an artist
 * @param page          Requested page
 * @param size          Requested page size
 * @returns {boolean}
 */
function searchArtist(page,size){
    const artistName = document.getElementById('artistName').value;
    const searchArtistRequest =
        {
            "artistName" : artistName,
            "page" : page,
            "size" : size
        };
    const searchArtistRequestJson = JSON.stringify(searchArtistRequest);
    const csrfToken  = $("input[name='_csrf']").val();

    $.ajax({
        method: "POST",
        url: "/rest/v1/artist",
        contentType: 'application/json',
        headers: {"X-CSRF-TOKEN": csrfToken},
        data: searchArtistRequestJson,
        dataType: "json",
        success: function(artistNameSearchResponse){
            buildResults(artistNameSearchResponse);
        },
        error: function(e){
            console.log(e.message);
        }
    });

    return false;
}

/**
 * Builds html with results or the message for an empty result
 * @param artistNameSearchResponse  JSON response
 */
const buildResults = function(artistNameSearchResponse) {
    clear();

    if (artistNameSearchResponse.artistSearchResults.length > 0) {
        createResultCards(artistNameSearchResponse);
        createPagination(artistNameSearchResponse);
    } else {
        createNoResultsMessage(artistNameSearchResponse);
    }
};

/**
 * Clears all containers for new search responses
 */
const clear = function () {
    $("#searchResultsContainer").empty();
    $("#paginationContainer").empty();

    const noResultsMessageElement = document.getElementById('noResultsMessageElement');
    if (noResultsMessageElement != null) {
        const noResultsMessageContainer = document.getElementById('noResultsMessageContainer');
        while (noResultsMessageContainer.firstChild) {
            noResultsMessageContainer.removeChild(noResultsMessageContainer.firstChild);
        }
    }
};

/**
 * Builds HTML for the result cards
 * @param artistNameSearchResponse
 */
const createResultCards = function(artistNameSearchResponse){
    jQuery.each(artistNameSearchResponse.artistSearchResults, function (i, artistSearchResult) {

        const card = document.createElement('div');
        card.className = "card";

        const cardBody = document.createElement('div');
        cardBody.className = "card-body";
        card.append(cardBody);

        if (artistSearchResult.thumb !== ""){
            const thumbElement = document.createElement('img');
            thumbElement.alt = 'Thumb for ' + artistSearchResult.artistName;
            thumbElement.src = artistSearchResult.thumb;
            cardBody.append(thumbElement);
        }

        const artistIdElement = document.createElement('p');
        artistIdElement.innerText = artistSearchResult.id;
        cardBody.append(artistIdElement);

        const artistNameElement = document.createElement('p');
        artistNameElement.innerText = artistSearchResult.artistName;
        cardBody.append(artistNameElement);

        const artistDetailsElement = document.createElement('a');
        artistDetailsElement.href = "/artist-details?artistName=" + artistSearchResult.artistName + "&id=" + artistSearchResult.id;
        artistDetailsElement.text = "Details for " + artistSearchResult.artistName;
        cardBody.append(artistDetailsElement);

        const breakElement = document.createElement('br');
        cardBody.append(breakElement);

        const followArtistButtonElement = document.createElement('button');
        followArtistButtonElement.id = "followArtistButton" + artistSearchResult.id;
        followArtistButtonElement.type = "button";
        followArtistButtonElement.className = "btn btn-primary btn-dark font-weight-bold";
        followArtistButtonElement.textContent = artistSearchResult.isFollowed ? "Unfollow" : "Follow";
        followArtistButtonElement.onclick =createOnClickFunctionFollowArtist(artistSearchResult.artistName,
            artistSearchResult.id,artistSearchResult.isFollowed,followArtistButtonElement);
        cardBody.append(followArtistButtonElement);

        document.getElementById('searchResultsContainer').appendChild(card);
    });
};

/**
 * Builds the onclick function
 * @param artistName    Artist to follow
 * @param artistId      Artist's discogs id
 * @param isFollowed    true if user follows given artist
 * @param button        Button that was clicked
 * @returns {Function}
 */
function createOnClickFunctionFollowArtist(artistName, artistId, isFollowed, button) {
    return function () {
        if (isFollowed)
            unfollowArtist(artistName,artistId,button);
        else
            followArtist(artistName,artistId,button);
    };
}

/**
 * Builds HTML for pagination links
 * @param artistNameSearchResponse  JSON response
 */
const createPagination = function (artistNameSearchResponse) {
    if (artistNameSearchResponse.pagination.currentPage > 1) {
        const previousElement = document.createElement('a');
        previousElement.href = "#";
        previousElement.text = "Previous";
        previousElement.onclick = (function (page, size) {
            return function () {
                searchArtist(page, size)
            };
        })(artistNameSearchResponse.pagination.nextPage, artistNameSearchResponse.pagination.size);

        document.getElementById('paginationContainer').appendChild(previousElement);
    }

    if (artistNameSearchResponse.pagination.totalPages > 1) {
        for (let index = 1; index <= artistNameSearchResponse.pagination.totalPages; index++) {
            const pageNumberElement = document.createElement('a');
            pageNumberElement.href = "#";
            pageNumberElement.text = index;
            pageNumberElement.onclick = (function (page, size) {
                return function () {
                    searchArtist(page, size)
                };
            })(index, artistNameSearchResponse.pagination.size);

            document.getElementById('paginationContainer').appendChild(pageNumberElement);
        }
    }

    if (artistNameSearchResponse.pagination.currentPage < artistNameSearchResponse.pagination.totalPages) {
        const nextElement = document.createElement('a');
        nextElement.href = "#";
        nextElement.text = "Next";
        nextElement.onclick = (function (page, size) {
            return function () {
                searchArtist(page, size)
            };
        })(artistNameSearchResponse.pagination.nextPage, artistNameSearchResponse.pagination.size);

        document.getElementById('paginationContainer').appendChild(nextElement);
    }
};

/**
 * Builds HTML for the message for an empty result
 * @param artistNameSearchResponse  JSON response
 */
const createNoResultsMessage = function (artistNameSearchResponse) {
    const noResultsMessageElement = document.createElement('div');
    noResultsMessageElement.className = "mb-3 alert alert-danger";
    noResultsMessageElement.role = "alter";
    noResultsMessageElement.id = "noResultsMessageElement";
    noResultsMessageElement.innerText =  "No artists could be found for the given name: " + artistNameSearchResponse.requestedArtistName;

    document.getElementById('noResultsMessageContainer').appendChild(noResultsMessageElement);
};
