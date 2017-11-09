console.log("LOAD JS SOURCE qms.js");

function initQms() {
    setTimeout(function () {
        scrollQms();
    }, 1);
}

function scrollQms() {
    getLastMess().scrollIntoView();
}


var lastMessRequestTS = new Date().getTime();

window.addEventListener("scroll", function (e) {
    var date = new Date();
    if (window.pageYOffset && (date.getTime() - lastMessRequestTS >= 500)) {
        lastMessRequestTS = date.getTime();
        IChat.showMoreMess();
    }
});

function getLastMess() {
    var listElem = document.querySelector(".mess_list");
    var messages = listElem.querySelectorAll(".mess_container");
    return messages[messages.length - 1];
}

function showMoreMess(listSrc) {
    var listElem = document.querySelector(".mess_list");
    var lastHeight = listElem.offsetHeight;
    listElem.insertAdjacentHTML("afterbegin", listSrc);
    addedNewMessages();
    window.scrollBy(0, listElem.offsetHeight - lastHeight);
}

function showNewMess(listSrc, withScroll) {
//console.log("SHOW NEW MESS "+listSrc);
    //listSrc = JSON.parse(listSrc);
    var listElem = document.querySelector(".mess_list");
    listElem.insertAdjacentHTML("beforeend", listSrc);
    addedNewMessages();
    if (withScroll) {
        setTimeout(function () {
            try{
                getLastMess().focus();
                getLastMess().querySelector(".accessibility_anchor").focus();
            }catch(ex){
                console.error(ex);
            }
            getLastMess().scrollIntoView();
        }, 100);
    }
}

function makeAllRead() {
var listElem = document.querySelector(".mess_list");
    var unreaded = listElem.querySelectorAll(".mess_container.unread");
    for (var i = 0; i < unreaded.length; i++) {
        unreaded[i].classList.remove("unread");
    }
}
const savepicPattern = /https?:\/\/savepic\.net\/(\d+)\.(.*)/g;

function transformQmsAttachments() {
    var links = document.querySelectorAll("a[href*='savepic.net']");
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
        link.innerHTML = "<img src=\"http://savepic.net/" + id + "m." + extension + "\" alt=\"" + alt + "\">";
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

nativeEvents.addEventListener(nativeEvents.DOM, initQms);
nativeEvents.addEventListener(nativeEvents.PAGE, scrollQms);
