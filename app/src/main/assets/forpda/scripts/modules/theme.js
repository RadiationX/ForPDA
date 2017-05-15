/*var ITheme = {
    log:function(arg){
        console.log(arg);
    }
}*/
var anchorElem, elemToActivation;
var corrector;
document.addEventListener("DOMContentLoaded", function () {
    corrector = new ScrollCorrector();
});
document.addEventListener('DOMContentLoaded', scrollToElement);



//Вызывается при обновлении прогресса загрузке страницы и при загрузке её ресурсов
//По идеи должна верно скроллить к нужному элементу, даже если пользователь прокрутил страницу
//Как оно работает и работает ли вообще - объяснить не могу
function onProgressChanged() {
    if (corrector)
        corrector.startObserver();
}

function getScrollTop() {
    return (window.pageYOffset || document.documentElement.scrollTop) - (document.documentElement.clientTop || 0);
}
//name может быть EventObject или строкок
//name это аттрибут тега html, может быть просто якорем или entry+post_id
//Вызывается из джавы, если находится на той-же странице, и в ссылке есть entry или якорь, а также при загрузке страницы
//PageInfo.elemToScroll - переменная, заданная в шаблоне в теге script, содержит в себе якорь или entry
//doOnLoadScroll - объект в window, задаётся false только когда была сделана перезагрузка страницы или переход назад



function scrollToElement(name) {
    if (typeof name != 'string') {
        name = PageInfo.elemToScroll;
    }
    var anchorData = /([^-]*)-([\d]*)-(\d+)/g.exec(name);
    if (anchorData) {
        //anchorData[1] - name (spoil, quote, etc)
        //anchorData[2] - post id
        //anchorData[3] - number block of post, begin with 1
        anchorData[1] = anchorData[1].toLowerCase();
        if (anchorData[1] === "spoiler") anchorData[1] = "spoil";
        if (anchorData[1] === "hide") anchorData[1] = "hidden";
        anchorElem = document.querySelector('[name="entry' + anchorData[2] + '"]');
        anchorElem = anchorElem.querySelectorAll(".post-block." + anchorData[1])[Number(anchorData[3]) - 1];
    } else {
        anchorElem = document.querySelector('[name="' + name + '"]');
    }
    if (anchorElem) {
        //Открытие всех спойлеров
        var block = anchorElem;
        while (block.classList && !block.classList.contains('post_body')) {
            if (block.classList.contains('spoil')) {
                block.classList.remove('close');
                block.classList.add('open');
            }
            block = block.parentNode;
        }
        //Открытие шапки
        block = anchorElem;
        while (block.classList && !block.classList.contains('post_container')) {
            block = block.parentNode;
        }
        if (block.classList.contains("close")) {
            var button = block.querySelector(".hat_button");
            toggleButton(button, "hat_content");
        }
    } else {
        anchorElem = document.documentElement;
    }
    //Скрол к якорю/элементу
    setTimeout(function () {
        doScroll(anchorElem);
    }, 1);
    window.addEventListener("load", function () {
        setTimeout(function () {
            console.log("SCROLL");
            console.log(getCoordinates(anchorElem).top + " : " + getScrollTop());
            doScroll(anchorElem);

            //ITheme.log(document.documentElement.innerHTML);
        }, 1);
    });
    /*if (document.readyState == "complete") {
        doScroll(anchorElem);
    } else {
        
    }*/
}

function doScroll(tAnchorElem) {
    tAnchorElem.scrollIntoView();

    //Активация элементов, убирается класс active с уже активированных
    if (elemToActivation)
        elemToActivation.classList.remove('active');

    var postElem = tAnchorElem;
    while (!postElem.classList.contains("post_container")) {
        postElem = postElem.parentElement;
    }
    elemToActivation = postElem;
    if (elemToActivation)
        elemToActivation.classList.add('active');
}





function getCoordinates(elem) {
    if (!elem) {
        return {
            top: 0,
            left: 0
        }
    }
    // (1)
    var box = elem.getBoundingClientRect();

    var body = document.body;
    var docEl = document.documentElement;

    // (2)
    var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
    var scrollLeft = window.pageXOffset || docEl.scrollLeft || body.scrollLeft;

    // (3)
    var clientTop = docEl.clientTop || body.clientTop || 0;
    var clientLeft = docEl.clientLeft || body.clientLeft || 0;

    // (4)
    var top = box.top + scrollTop - clientTop;
    var left = box.left + scrollLeft - clientLeft;

    return {
        top: top,
        left: left
    };
}

function selectionToQuote() {
    var selObj = window.getSelection();
    var selectedText = selObj.toString();


    var p = selObj.anchorNode.parentNode;
    while (p.classList && !p.classList.contains('post_container')) {
        p = p.parentNode;
    }
    if (typeof p === "undefined" || typeof p.dataset === "undefined") {
        ITheme.toast("Для этого действия необходимо выбрать текст сообщения");
        return;
    }
    var postId = p.dataset.postId;
    if (selectedText != null && postId != null) {
        ITheme.quotePost(selectedText, "" + postId);
    } else {
        ITheme.toast("Ошибка создания цитаты: [" + selectedText + ", " + postId + "]");
        return;
    }
}

function copySelectedText() {
    var selectedText = window.getSelection().toString();
    if (selectedText != null && selectedText) {
        ITheme.copySelectedText(selectedText);
    }
}

function selectAllPostText() {
    var selObj = window.getSelection();
    var p = selObj.anchorNode.parentNode;
    while (p.classList && !p.classList.contains('post_body')) {
        p = p.parentNode;
    }
    if (typeof p.classList === "undefined" || !p.classList.contains('post_body')) {
        ITheme.toast("Для этого действия необходимо выбрать текст сообщения");
        return;
    }
    var rng, sel;
    if (document.createRange) {
        rng = document.createRange();
        rng.selectNode(p);
        sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(rng);
    } else {
        rng = document.body.createTextRange();
        rng.moveToElementText(p);
        rng.select();
    }
}

function toggleButton(button, bodyClass) {
    var parent = button.parentNode;
    var body;
    if (bodyClass !== undefined)
        body = parent.querySelector("." + bodyClass);
    if (parent.classList.contains("close") | body.style.display == "none") {
        parent.classList.remove("close");
        parent.classList.add("open");
        if (body !== undefined) {
            body.removeAttribute("hidden");
        }
    } else {
        parent.classList.remove("open");
        parent.classList.add("close");
        if (body !== undefined) {
            body.setAttribute("hidden", "");
        }
    }
}

var pad = 100;


function onsuka(elem) {
    console.log("CLICK SUKA");
    var suka = document.querySelector(".posts_list")
    suka.style.paddingTop = pad + "px";
    pad += 100;
    corrector.startObserver();
}



function ScrollCorrector() {
    console.log("Scroll Corrector initialized");
    var postElements = document.querySelectorAll(".post_container");
    var visibleElements = [];
    var visibleElement = anchorElem;
    var lastPosition = 0;
    var frames = 60 * 1;
    var frame = 0;
    var observerId = 0;

    for (var i = 0; i < postElements.length; i++) {
        postElements[i].addEventListener("mousedown", downEvent);
        postElements[i].addEventListener("touchdown", downEvent);
    }

    function downEvent(e) {

        var elem = e.target;
        while (!elem.classList.contains("post_container")) {
            elem = elem.parentElement;
        }
        visibleElement = elem;
        updateLastPosition();
    }

    this.startObserver = function () {
        startObserver();
    }

    window.addEventListener("scroll", function () {
        setVisible();
        updateLastPosition();
        frame = 0;
    });

    function updateLastPosition() {
        lastPosition = getCoordinates(visibleElement).top;
        //console.log("Update LastPosition: " + lastPosition);
    }

    function tryScroll() {

        var delta = getCoordinates(visibleElement).top - lastPosition;
        if (delta == 0)
            return;
        /*for (var i = 0; i < visibleElements.length; i++) {
            var elem = visibleElements[i];
            console.log("Elem [" + i + "]: " + getCoordinates(elem).top);
        }*/
        console.log("Scroll by delta: " + delta + ", lastPosition: " + lastPosition + ", visElemTop: " + getCoordinates(visibleElement).top);
        window.scrollBy(0, delta);
        updateLastPosition();
        frame = 0;
    }

    function startObserver() {
        if (observerId == 1) {
            return;
        }
        setVisible();
        console.log("Start Scroll Observer");

        function observerLoop() {
            tryScroll();
            if (frame < frames) {
                requestAnimationFrame(observerLoop);
                frame++;
            } else {
                cancelAnimationFrame(observerLoop);
                observerId = 0;
                frame = 0;
                console.log("Stop Scroll Observer");
            }
        }
        observerId = 1;
        observerLoop();
    }

    /*function setVisible(newVisible){
        if (visibleElement) {
            visibleElement.style.opacity = 1;
        }
        visibleElement = newVisible;
        visibleElement.style.opacity = 0.5;
    }*/

    function setVisible() {
        return;
        visibleElements = getVisiblePosts();
        if (visibleElement) {
            visibleElement.style.opacity = 1;
        }
        /*if (visibleElements.length > 0) {
            visibleElement = getNearest(visibleElements);
        }*/
        visibleElement = getNearest(visibleElements);
        visibleElement.style.opacity = 0.5;
    }

    function getVisiblePosts() {
        var scrollTop = getScrollTop();
        var windowHeight = document.documentElement.clientHeight;
        if (!visibleElement)
            visibleElements = [];
        visibleElements.length = 0;
        for (var i = 0; i < postElements.length; i++) {
            var el = postElements[i];
            if (el.offsetHeight + el.offsetTop < scrollTop || el.offsetTop > scrollTop + windowHeight)
                continue;
            visibleElements.push(el);
        }
        return visibleElements;
    }

    function getNearest(visibleElements) {
        var scrollTop = getScrollTop();
        var windowHeight = document.documentElement.clientHeight;
        var nearest = visibleElements[0];
        var deltaHeight = windowHeight;
        var delta = 0;
        for (var i = 0; i < visibleElements.length; i++) {
            var el = visibleElements[i];
            var bottomY = Math.abs(el.offsetTop + el.offsetHeight - scrollTop - windowHeight);
            if (deltaHeight - bottomY < delta) {
                break;
            }
            delta = deltaHeight - bottomY;
            deltaHeight = bottomY;
            nearest = el;
        }
        return nearest;
    }
}





function suka() {}
