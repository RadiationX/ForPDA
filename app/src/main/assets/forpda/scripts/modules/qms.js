var listElem;

window.addEventListener("DOMContentLoaded", function (e) {
 console.log("DOMContentLoaded");
    listElem = document.querySelector(".mess_list");
    setTimeout(function () {
        getLastMess().scrollIntoView();
    }, 10);
});

window.addEventListener("load", function (e) {
    console.log("load");
    getLastMess().scrollIntoView();
});

var lastMessRequestTS = new Date().getTime();

window.addEventListener("scroll", function (e) {
    var date = new Date();
    if (window.pageYOffset == 0 /*|| window.pageYOffset <= 48*/ && (date.getTime() - lastMessRequestTS >= 500)) {
        lastMessRequestTS = date.getTime();
        //addTopPost();
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
    improveCodeBlock();
    blocksOpenClose();
    removeImgesSrc();
    addIcons();
}

function addTopPost() {
    var listSrc = "";
    for (var i = 0; i < 30; i++) {
        listSrc += '<div class="mess_container our"><div class="mess"><div class="content">cheburekkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk</div></div><div class="date"><span>04:20</span></div></div>';
    }
    listSrc += "<hr><hr>";
    showMoreMess(listSrc);
}

function addPost() {
    var listSrc = '<div class="mess_container our"><div class="mess"><div class="content">cheburekkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk</div></div><div class="date"><span>04:20</span></div></div>';
    showNewMess(listSrc, true);
}
