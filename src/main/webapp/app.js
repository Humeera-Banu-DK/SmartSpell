// ============================================================
// SMARTSPELL APP.JS
// ============================================================

var textInput = document.getElementById("textInput");
var checkButton = document.getElementById("checkButton");
var clearButton = document.getElementById("clearButton");

var result = document.getElementById("result");
var loading = document.getElementById("loading");
var wordCount = document.getElementById("wordCount");
var status = document.getElementById("status");


// ============================================================
// CREATE SUGGESTION BOX
// ============================================================

var suggestionBox = document.getElementById("suggestionBox");
var suggestions = document.getElementById("suggestions");

if (!suggestionBox) {

    suggestionBox = document.createElement("div");

    suggestionBox.id = "suggestionBox";

    suggestionBox.style.display = "none";
    suggestionBox.style.marginTop = "10px";
    suggestionBox.style.padding = "12px";
    suggestionBox.style.border = "1px solid #ddd";
    suggestionBox.style.borderRadius = "10px";
    suggestionBox.style.backgroundColor = "white";

    textInput.parentNode.insertBefore(
        suggestionBox,
        textInput.nextSibling
    );
}

if (!suggestions) {

    suggestions = document.createElement("div");

    suggestions.id = "suggestions";

    suggestionBox.appendChild(suggestions);
}


// ============================================================
// WORD COUNT
// ============================================================

function updateWordCount() {

    var text = textInput.value.trim();

    if (text === "") {

        wordCount.textContent = "0 words";

        return;
    }

    var words = text.split(/\s+/);

    var count = words.length;

    wordCount.textContent =
        count + (count === 1 ? " word" : " words");
}


// ============================================================
// GET CURRENT WORD
// ============================================================

function getCurrentWord() {

    var text = textInput.value;

    if (text.trim() === "") {

        return "";
    }

    var words = text.trim().split(/\s+/);

    var currentWord =
        words[words.length - 1];

    currentWord =
        currentWord.replace(
            /[^a-zA-Z]/g,
            ""
        );

    return currentWord;
}


// ============================================================
// HIDE SUGGESTIONS
// ============================================================

function hideSuggestions() {

    suggestionBox.style.display = "none";

    suggestions.innerHTML = "";
}


// ============================================================
// FETCH SUGGESTIONS
// ============================================================

var suggestionTimer = null;

function requestSuggestions() {

    if (suggestionTimer !== null) {

        clearTimeout(suggestionTimer);
    }

    suggestionTimer =
        setTimeout(
            fetchSuggestions,
            300
        );
}


function fetchSuggestions() {

    var word = getCurrentWord();

    if (word.length < 2) {

        hideSuggestions();

        return;
    }

    var contextPath =
        window.location.pathname.substring(
            0,
            window.location.pathname.indexOf(
                "/",
                1
            )
        );

    var url =
        contextPath +
        "/check?word=" +
        encodeURIComponent(word);


    fetch(url)

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Server error"
                );
            }

            return response.json();
        })

        .then(function(data) {

            displaySuggestions(data);
        })

        .catch(function(error) {

            console.log(
                "Suggestion error:",
                error
            );

            hideSuggestions();
        });
}


// ============================================================
// DISPLAY SUGGESTIONS
// ============================================================

function displaySuggestions(list) {

    suggestions.innerHTML = "";

    if (!list || list.length === 0) {

        hideSuggestions();

        return;
    }


    var title =
        document.createElement("div");

    title.textContent =
        "Suggestions:";

    title.style.fontWeight =
        "bold";

    title.style.marginBottom =
        "8px";

    title.style.color =
        "#555";

    suggestions.appendChild(title);


    for (var i = 0; i < list.length; i++) {

        createSuggestionButton(
            list[i]
        );
    }


    suggestionBox.style.display =
        "block";
}


// ============================================================
// CREATE SUGGESTION BUTTON
// ============================================================

function createSuggestionButton(
    suggestion
) {

    var button =
        document.createElement("button");

    button.type = "button";

    button.textContent =
        suggestion;

    button.style.margin =
        "4px";

    button.style.padding =
        "8px 15px";

    button.style.border =
        "1px solid #5368e8";

    button.style.borderRadius =
        "20px";

    button.style.backgroundColor =
        "#f1f3ff";

    button.style.color =
        "#4056d6";

    button.style.cursor =
        "pointer";


    button.onclick = function() {

        replaceCurrentWord(
            suggestion
        );

        hideSuggestions();

        textInput.focus();
    };


    suggestions.appendChild(
        button
    );
}


// ============================================================
// REPLACE CURRENT WORD
// ============================================================

function replaceCurrentWord(
    replacement
) {

    var text =
        textInput.value;

    var words =
        text.split(/(\s+)/);


    for (
        var i = words.length - 1;
        i >= 0;
        i--
    ) {

        if (words[i].trim() !== "") {

            var original =
                words[i];

            var punctuation =
                original.match(
                    /[^a-zA-Z]+$/
                );

            var ending = "";

            if (punctuation) {

                ending =
                    punctuation[0];
            }

            words[i] =
                replacement +
                ending;

            break;
        }
    }


    textInput.value =
        words.join("");

    updateWordCount();
}


// ============================================================
// USER TYPES
// ============================================================

textInput.addEventListener(
    "input",
    function() {

        updateWordCount();

        requestSuggestions();
    }
);


// ============================================================
// CHECK SPELLING BUTTON
// ============================================================

checkButton.addEventListener(
    "click",
    function() {

        var text =
            textInput.value.trim();


        if (text === "") {

            result.textContent =
                "Please enter some text.";

            status.textContent =
                "Waiting";

            return;
        }


        hideSuggestions();


        loading.style.display =
            "block";

        status.textContent =
            "Checking...";


        var formData =
            new URLSearchParams();


        formData.append(
            "text",
            text
        );


        var contextPath =
            window.location.pathname.substring(
                0,
                window.location.pathname.indexOf(
                    "/",
                    1
                )
            );


        fetch(
            contextPath + "/check",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body:
                    formData.toString()
            }
        )

        .then(function(response) {

            if (!response.ok) {

                throw new Error(
                    "Server error"
                );
            }

            return response.text();
        })

        .then(function(correctedText) {

            result.textContent =
                correctedText;

            status.textContent =
                "Completed";
        })

        .catch(function(error) {

            console.log(
                "Spell check error:",
                error
            );

            result.textContent =
                "Unable to check spelling.";

            status.textContent =
                "Error";
        })

        .then(function() {

            loading.style.display =
                "none";
        });
    }
);


// ============================================================
// CLEAR BUTTON
// ============================================================

clearButton.addEventListener(
    "click",
    function() {

        textInput.value = "";

        result.textContent =
            "Your corrected text will appear here.";

        status.textContent =
            "Ready";

        wordCount.textContent =
            "0 words";

        hideSuggestions();

        textInput.focus();
    }
);


// ============================================================
// INITIAL SETUP
// ============================================================

updateWordCount();

hideSuggestions();