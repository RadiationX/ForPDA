console.log("LOAD JS SOURCE blocks.js");

function transformSnapbacks() {
    var snapBacks = document.querySelectorAll("a[href*=findpost][title='Перейти к сообщению'],a[href*=showuser]");
    for (var i = 0; i < snapBacks.length; i++) {
        var snapBack = snapBacks[i];
        if (snapBack.classList.contains("snapback")) {
            break;
        }
        if (!(snapBack.href.indexOf("showuser") != -1)) {
            var temp = snapBack.nextElementSibling;
            if (temp != null && temp != undefined) {
                snapBack.innerHTML = temp.textContent;
                temp = snapBack;
                while (temp != null) {
                    //console.log(temp);
                    temp = temp.nextSibling;
                    if (!temp) break;
                    if (temp.nodeName === "#text") {
                        if (temp.nodeValue === " ") {
                            temp.nodeValue = "";
                        }
                        continue;
                    }
                    if (temp.tagName == "B") {
                        break;
                    }
                }
                if (temp) {
                    temp.parentNode.removeChild(temp);
                }
            }
            temp = snapBack.getElementsByTagName("IMG");
            if (temp.length > 0) {
                temp = temp[0];
                temp.parentNode.removeChild(temp);
            }
        }
        snapBack.classList.add("snapback");
    }
}

function transformQuotes() {
    var quotes = document.querySelectorAll(".post-block.quote");
    var myRegexp = /([\s\S]*?) @ ((?:\d+.\d+.\d+|[a-zA-zа-я-А-Я]+)(?:, \d+:\d+)?)/g;
    for (var i = 0; i < quotes.length; i++) {
        var quote = quotes[i];
        if (quote.classList.contains("transformed")) {
            break;
        }
        var titleBlock = quote.querySelector(".block-title");

        var titleText = titleBlock.textContent;
        //console.log(titleText);
        var match;
        while (match = myRegexp.exec(titleText)) {
            var newTextSrc = "<span class=\"title\">";
            var nick = match[1];

            newTextSrc += "<span class=\"name\">" + nick + "</span>";
            newTextSrc += "<span class=\"date\">" + match[2] + "</span>";
            newTextSrc + "</span>";
            titleBlock.innerHTML = titleBlock.innerHTML.replace(match[0], "");
            titleBlock.insertAdjacentHTML("afterbegin", newTextSrc);
            var match2 = /([a-zA-Zа-яА-Я])/.exec(nick);
            if (match2) {
                nick = match2[1];
            } else {
                nick = nick.charAt(0);
            }
            titleBlock.insertAdjacentHTML("afterbegin", "<div class=\"avatar\">" + nick + "</div>")
        }
        quote.classList.add("transformed");
    }
}

function improveCodeBlock() {
    var codeBlockAll = document.querySelectorAll('.post-block.code');
    for (var i = 0; i < codeBlockAll.length; i++) {
        try {
            var codeBlock = codeBlockAll[i];
            var codeTitle = codeBlock.querySelector('.block-title')
            if (!codeBlock.classList.contains("improve")) {
                var codeBody = codeBlock.querySelector('.block-body'),
                    splitLines = codeBody.innerHTML.split(/<br[^>]*?>/g),
                    count = '',
                    lines = '';
                for (var j = 0; j < splitLines.length; j++) {
                    lines += '<div>' + splitLines[j] + '</div>';
                    count += (j + 1) + '\n';
                }
                codeBlock.classList.add('wrap');
                codeTitle.insertAdjacentHTML("beforeEnd", '<div class="block-controls"><i class="wrap"></i><i class="select_all"></i></div>');
                codeBody.innerHTML = "<div class=\"lines\">" + lines + "</div>";
                codeBlock.classList.add("improve");
            }
            codeTitle.querySelector('.wrap').addEventListener('click', onClickToggleButton);
            codeTitle.querySelector('.select_all').addEventListener('click', SelectText);
        } catch (error) {
            console.log(error);
        }
    }

    function onClickToggleButton(e) {
        e.stopPropagation();
        var button = e.target;
        var block;
        for (var i = 0; i < codeBlockAll.length; i++) {
            if (button == codeBlockAll[i].querySelector('.wrap')) {
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
            if (button == codeBlockAll[i].querySelector('.select_all')) {
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

function blocksOpenClose() {
    var blockAll = document.querySelectorAll('.post-block.spoil,.post-block.code');

    if (!blockAll[0]) return;

    for (var i = 0; i < blockAll.length; i++) {
        var codeBlock = blockAll[i];
        /*if (codeBlock.classList.contains("trigger")) {
            continue;
        }*/
        var bt = codeBlock.querySelector(".block-title");
        var bb = codeBlock.querySelector('.block-body');
        //console.log(bb);
        if (bb.parentElement.classList.contains('code') && bb.scrollHeight <= bb.offsetHeight) {
            //bb.parentElement.classList.remove('box');
        }
        bt.addEventListener('click', clickOnElement, false);
        /*if (!codeBlock.classList.contains("trigger")) {
            codeBlock.classList.add("trigger");
        }*/
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
    while (t && !t.classList.contains('.post-body')) {
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
    var postBlockSpoils = document.body.querySelectorAll('.post-block.spoil.close');
    for (var i = 0; i < postBlockSpoils.length; i++) {
        var codeBlock = postBlockSpoils[i];
        /*if (codeBlock.classList.contains("images")) {
            continue;
        }*/
        var images = codeBlock.querySelector(".block-body").querySelectorAll("img");
        for (var j = 0; j < images.length; j++) {
            var img = images[j];
            if (!img.hasAttribute('src') || img.dataset.imageSrc) continue;
            img.dataset.imageSrc = img.src;
            img.removeAttribute('src');
        }
        /*if (!codeBlock.classList.contains("images")) {
            codeBlock.classList.add("images");
        }*/
    }
}

function addImgesSrc(target) {
    while (target != null) {
        if (target.classList && target.classList.contains('spoil')) {
            var images = target.querySelectorAll('img');
            for (var i = 0; i < images.length; i++) {
                var img = images[i];
                if (img.hasAttribute('src') || !img.dataset.imageSrc) continue;
                img.src = img.dataset.imageSrc;
                img.removeAttribute('data-image-src');
                corrector.startObserver();
            }
            return;
        }
        target = target.parentNode;
    }
}

function addIcons(e) {
    var blockAll = document.querySelectorAll(".post-block");
    var newIcon;
    for (var i = 0; i < blockAll.length; i++) {
        var codeBlock = blockAll[i];
        if (!codeBlock.classList.contains("icons")) {
            var blockTitle = codeBlock.querySelector(".block-title");
            if (blockTitle.innerText.length == 0) {
                blockTitle.classList.add("empty");
            }
            newIcon = document.createElement('i');
            newIcon.classList.add("icon");
            blockTitle.appendChild(newIcon);
            codeBlock.classList.add("icons");
        }
    }
}

nativeEvents.addEventListener("DOMContentLoaded", transformSnapbacks);
nativeEvents.addEventListener("DOMContentLoaded", transformQuotes);

nativeEvents.addEventListener("DOMContentLoaded", improveCodeBlock);
nativeEvents.addEventListener("DOMContentLoaded", blocksOpenClose);
nativeEvents.addEventListener("DOMContentLoaded", removeImgesSrc);
nativeEvents.addEventListener("DOMContentLoaded", addIcons);
