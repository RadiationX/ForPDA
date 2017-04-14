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
document.addEventListener('DOMContentLoaded', improveCodeBlock);
document.addEventListener('DOMContentLoaded', blocksOpenClose);
document.addEventListener('DOMContentLoaded', removeImgesSrc);
//Вызывается при обновлении прогресса загрузке страницы и при загрузке её ресурсов
//По идеи должна верно скроллить к нужному элементу, даже если пользователь прокрутил страницу
//Как оно работает и работает ли вообще - объяснить не могу
function onProgressChanged() {
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
    var data = /([^-]*)-([\d]*)-(\d+)/g.exec(name);
    if (data) {
        //data[1] - name (spoil, quote, etc)
        //data[2] - post id
        //data[3] - number block of post, begin with 1
        data[1] = data[1].toLowerCase();
        if (data[1] === "spoiler") data[1] = "spoil";
        if (data[1] === "hide") data[1] = "hidden";
        anchorElem = document.querySelector('[name="entry' + data[2] + '"]');
        anchorElem = anchorElem.querySelectorAll(".post-block." + data[1])[Number(data[3]) - 1];
    } else {
        anchorElem = document.querySelector('[name="' + name + '"]');
    }
    //console.log(anchorElem);
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
        
        //Скрол к якорю/элементу
        setTimeout(function () {
            doScroll(anchorElem);
        }, 1);
        window.addEventListener("load", function () {
            setTimeout(function () {
                doScroll(anchorElem);
            }, 1);
        });
        /*if (document.readyState == "complete") {
            doScroll(anchorElem);
        } else {
            
        }*/
    }
}

function doScroll(tAnchorElem) {
    tAnchorElem.scrollIntoView();
    
    //Активация элементов, убирается класс active с уже активированных
    if (elemToActivation)
        elemToActivation.classList.remove('active');

    elemToActivation = document.querySelector('.post_container[name="' + name + '"]');
    if (elemToActivation)
        elemToActivation.classList.add('active');
}


function blocksOpenClose() {
    var blockTitleAll = document.querySelectorAll('.post-block.spoil>.block-title,.post-block.code>.block-title');

    if (!blockTitleAll[0]) return;

    for (var i = 0; i < blockTitleAll.length; i++) {
        var bt = blockTitleAll[i];
        var bb = bt.parentElement.querySelector('.block-body');
        if (bb.parentElement.classList.contains('code') && bb.scrollHeight <= bb.offsetHeight) {
            bb.parentElement.classList.remove('box');
        }
        bt.addEventListener('click', clickOnElement, false);
    }

    function clickOnElement(event) {
        var t = event.target;
        while (!t.classList.contains('post_body') || !t.classList.contains('msg-content') || t != document.body) {
            if (t.classList.contains('spoil')) {
                event.stopPropagation();
                toggler("close", "open", t);
                spoilCloseButton(t);
                return;
            } else if (t.classList.contains('code')) {
                event.stopPropagation();
                toggler("unbox", "box", t);
                return;
            }
            t = t.parentElement;
        }
    }

    function toggler(c, o, t) {
        if (t.classList.contains(c)) {
            t.classList.remove(c);
            t.classList.add(o);
            addImgesSrc(t);
        } else if (t.classList.contains(o)) {
            t.classList.remove(o);
            t.classList.add(c);
        }
    }
}

/**
 *		==================
 *		SPOIL CLOSE BUTTON
 *		==================
 */

function spoilCloseButton(t) {
    var el = t;
    while (!t.classList.contains('.post-body')) {
        if (t.classList.contains('spoil') && !t.querySelector('.spoil_close')) {
            if (t.querySelector('img[src]')) {
                var images = t.querySelectorAll('img[src]');
                images[images.length - 1].addEventListener("load", function () {
                    spoilCloseButton(el);
                });
            }
        }
        t = t.parentElement;
    }
}

/**
 *		===============================
 *		HIDE AND SHOW IMAGES IN SPOILER
 *		===============================
 */



function removeImgesSrc() {
    if (document.body.classList.contains("noimages")) return;
    var postBlockSpoils = document.body.querySelectorAll('.post-block.spoil.close > .block-body');
    for (var i = 0; i < postBlockSpoils.length; i++) {
        var images = postBlockSpoils[i].querySelectorAll('img');
        for (var j = 0; j < images.length; j++) {
            var img = images[j];
            if (!img.hasAttribute('src') || img.dataset.imageSrc) continue;
            img.dataset.imageSrc = img.src;
            img.removeAttribute('src');
        }
    }
}

function addImgesSrc(target) {
    while (target != this) {
        if (target.classList.contains('spoil')) {
            var images = target.querySelectorAll('img');
            for (var i = 0; i < images.length; i++) {
                var img = images[i];
                if (img.hasAttribute('src') || !img.dataset.imageSrc) continue;
                img.src = img.dataset.imageSrc;
                img.removeAttribute('data-image-src');
            }
            return;
        }
        target = target.parentNode;
    }
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




function improveCodeBlock() {
    var codeBlockAll = document.querySelectorAll('.post-block.code');
    for (var i = 0; i < codeBlockAll.length; i++) {
        try {
            var codeBlock = codeBlockAll[i],
                codeTitle = codeBlock.querySelector('.block-title'),
                codeBody = codeBlock.querySelector('.block-body'),
                splitLines = codeBody.innerHTML.split("<br>"),
                count = '',
                lines = '';

            for (var j = 0; j < splitLines.length; j++) {
                lines += '<span class="line">' + splitLines[j] + '</span><br>';
                count += (j + 1) + '\n';
            }

            codeBlock.classList.add('wrap');
            codeTitle.insertAdjacentHTML("afterEnd", '<div class="block-controls"><div class="control wrap"></div><div class="control select_all"></div></div><div class="num-pre">' + count + '</div>');
            codeBlock.querySelector('.control.wrap').addEventListener('click', onClickToggleButton);
            codeBlock.querySelector('.control.select_all').addEventListener('click', SelectText);
            codeBody.innerHTML = lines;
        } catch (error) {
            alert(error);
        }
    }

    function onClickToggleButton(e) {
        e.stopPropagation();
        var button = e.target;
        var block;
        for (var i = 0; i < codeBlockAll.length; i++) {
            if (button == codeBlockAll[i].querySelector('.control.wrap')) {
                block = codeBlockAll[i];
                break;
            }
        }
        if (!block) return;
        if (block.classList.contains('wrap')) {
            block.classList.remove('wrap');
        } else {
            block.classList.add('wrap');
        }
    }

    function SelectText(e) {
        e.stopPropagation();
        var button = e.target;
        var block;
        for (var i = 0; i < codeBlockAll.length; i++) {
            if (button == codeBlockAll[i].querySelector('.control.select_all')) {
                block = codeBlockAll[i];
                break;
            }
        }
        var text = block.querySelector(".block-body");
        var range, selection
        if (document.body.createTextRange) {
            range = document.body.createTextRange();
            range.moveToElementText(text);
            range.select();
        } else if (window.getSelection) {
            selection = window.getSelection();
            range = document.createRange();
            range.selectNodeContents(text);
            selection.removeAllRanges();
            selection.addRange(range);
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
    console.log("Correct init");
    var postElements = document.querySelectorAll(".post_container");
    var lastAnchorPosition;
    var visibleElem;
    var frames = 60 * 2;
    var frame = 0;
    var observerId = 0;

    this.startObserver = function () {
        startObserver();
    }

    window.addEventListener("scroll", function () {
        console.log("onscroll");
        setVisible();
        updateLastPosition("scroll");
        frame = 0;
    });

    function updateLastPosition(from) {
        console.log("updateLastPosition " + from + ", last: " + lastAnchorPosition + ", new: " + getCoordinates(visibleElem).top);
        lastAnchorPosition = getCoordinates(visibleElem).top;
    }

    function tryScroll() {
        var delta = getCoordinates(visibleElem).top - lastAnchorPosition;
        if (delta == 0)
            return;
        window.scrollBy(0, delta);
        updateLastPosition("tryScroll");
        frame = 0;
    }

    function startObserver() {
        if (observerId == 1) {
            return;
        }
        setVisible();
        console.log("StartObserver");

        function observerLoop() {
            tryScroll();
            if (frame < frames) {
                requestAnimationFrame(observerLoop);
                frame++;
            } else {
                cancelAnimationFrame(observerLoop);
                observerId = 0;
                frame = 0;
            }
        }
        observerId = 1;
        observerLoop();
    }

    function setVisible() {
        var visibleElems = getVisiblePosts();
        /*if (visibleElem) {
            visibleElem.style.opacity = 1;
        }*/
        if (visibleElems.length > 0) {
            visibleElem = getNearest(visibleElems);
        }
        //visibleElem.style.opacity = 0.5;
    }

    function getVisiblePosts() {
        var scrollTop = getScrollTop();
        var windowHeight = document.documentElement.clientHeight;
        var result = [];
        for (var i = 0; i < postElements.length; i++) {
            var el = postElements[i];
            if (el.offsetHeight + el.offsetTop < scrollTop || el.offsetTop > scrollTop + windowHeight)
                continue;
            result.push(el);
        }
        return result;
    }

    function getNearest(visibleElems) {
        var scrollTop = getScrollTop();
        var windowHeight = document.documentElement.clientHeight;
        var nearest = visibleElems[0];
        var deltaHeight = windowHeight;
        var delta = 0;
        for (var i = 0; i < visibleElems.length; i++) {
            var el = visibleElems[i];
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
