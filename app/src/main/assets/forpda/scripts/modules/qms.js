console.log("LOAD JS SOURCE qms.js");

var listElem;

function initQms() {
    listElem = document.querySelector(".mess_list");
    setTimeout(function () {
        scrollQms();
    }, 1);
}

function scrollQms() {
    getLastMess().scrollIntoView();
}
nativeEvents.addEventListener("DOMContentLoaded", initQms);

nativeEvents.addEventListener("load", scrollQms);

var lastMessRequestTS = new Date().getTime();

window.addEventListener("scroll", function (e) {
    var date = new Date();
    if (window.pageYOffset == 0 /*|| window.pageYOffset <= 48*/ && (date.getTime() - lastMessRequestTS >= 500)) {
        lastMessRequestTS = date.getTime();
        IChat.showMoreMess();
    }
});

function getLastMess() {
    var messages = listElem.querySelectorAll(".mess_container");
    return messages[messages.length - 1];
}

function showMoreMess(listSrc) {
    var lastHeight = listElem.offsetHeight;
    listElem.insertAdjacentHTML("afterbegin", listSrc);
    addedNewMessages();
    window.scrollBy(0, listElem.offsetHeight - lastHeight);
}

function showNewMess(listSrc, withScroll) {
    listElem.insertAdjacentHTML("beforeend", listSrc);
    addedNewMessages();
    if (withScroll) {
        getLastMess().scrollIntoView();
    }
}

function makeAllRead() {
    var unreaded = listElem.querySelectorAll(".mess_container.unread");
    for (var i = 0; i < unreaded.length; i++) {
        unreaded[i].classList.remove("unread");
    }
}
const savepicPattern = /https?:\/\/savepic\.ru\/(\d+)\.(.*)/g;

function transformQmsAttachments() {
    var links = document.querySelectorAll("a[href*='savepic.ru']");
    for (var i = 0; i < links.length; i++) {
        var link = links[i];
        var alt = link.textContent;
        var id = 0;
        var extension;
        var match
        while (match = savepicPattern.exec(link.href)) {
            id = match[1];
            extension = match[2];
        }
        link.innerHTML = "<img src=\"http://savepic.ru/" + id + "m." + extension + "\" alt=\"" + alt + "\">";
    }
}

function addedNewMessages() {
    transformQmsAttachments();
    transformSnapbacks();
    transformQuotes();

    improveCodeBlock();
    blocksOpenClose();
    removeImgesSrc();
    addIcons();
    jsEmoticons.parseAll();
}
