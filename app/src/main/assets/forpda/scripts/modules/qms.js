console.log("LOAD JS SOURCE qms.js");

var listElem;
function initQms(){
    listElem = document.querySelector(".mess_list");
    setTimeout(function () {
        scrollQms();
    }, 1);
}
function scrollQms(){
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
    var suka = listElem.insertAdjacentHTML("afterbegin", listSrc);
    console.error(suka);
    window.scrollBy(0, listElem.offsetHeight - lastHeight);
    addedNewMessages();
}

function showNewMess(listSrc, withScroll) {
    listElem.insertAdjacentHTML("beforeend", listSrc);
    if (withScroll) {
        getLastMess().scrollIntoView();
    }
    addedNewMessages();
}

function addedNewMessages(){
    transformSnapbacks();
    transformQuotes();
    
    improveCodeBlock();
    blocksOpenClose();
    removeImgesSrc();
    addIcons();
    jsEmoticons.parseAll();
}
