console.log("LOAD JS SOURCE blocks.js");
var loader;
try {
    loader = new AvatarLoader();
} catch (ex) {
    console.error(ex);
}

function transformSnapbacks() {
    var snapBacks = document.querySelectorAll("a[href*=findpost][title='Перейти к сообщению'],a[href*=showuser]");
    for (var i = 0; i < snapBacks.length; i++) {
        var snapBack = snapBacks[i];
        //console.log("SNAPBACK " + snapBack.href);
        //console.log(snapBack);
        if (snapBack.classList.contains("snapback")) {
            break;
        }
        if (snapBack.href.indexOf("showuser") != -1) {
            var temp = snapBack;
            while (temp.firstElementChild != null) {
                temp = temp.firstElementChild;
            }
            temp.insertAdjacentHTML("afterbegin", "<span class=\"icon\"></span>");
            snapBack.classList.add("user");
        } else {
            var temp;

            //Удаление изображения
            temp = snapBack.getElementsByTagName("IMG");
            if (temp.length > 0) {
                temp = temp[0];
                temp.parentNode.removeChild(temp);
            }

            //Обычно идёт следующий эелемент
            temp = snapBack.nextElementSibling;
            if (temp != null && temp != undefined) {
                var nick = temp.textContent;
                //console.log(nick);
                //Обычно ник вконце с запятой
                //Удаляем из текста и вставляем в ссылку
                temp.parentNode.removeChild(temp);
                snapBack.appendChild(temp);

                //Поиск самого "глубокого" элемента для иконки
                while (temp.firstElementChild != null && temp.tagName.toLocaleLowerCase === "img") {
                    temp = temp.firstElementChild;
                }
                temp.insertAdjacentHTML("afterbegin", "<span class=\"icon\"></span>");



                snapBack.classList.add("post");
            }
        }
        snapBack.classList.add("snapback");
    }
}

function transformQuotes() {
    var quotes = document.querySelectorAll(".post-block.quote");
    var myRegexp = /([\s\S]*?) @ ((?:\d+.\d+.\d+|[a-zA-zа-я-А-Я]+)(?:, \d+:\d+)?)?/g;
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
            var date = match[2];
            var validNick = true;
            if (nick.length == 0) {
                validNick = false;
                nick = "undefined";
            }
            if (date == null || date == undefined) {
                date = "undefined";
            }

            newTextSrc += "<span class=\"name\">" + nick + "</span>";
            newTextSrc += "<span class=\"date\">" + date + "</span>";
            newTextSrc + "</span>";
            titleBlock.innerHTML = titleBlock.innerHTML.replace(match[0], "");
            titleBlock.insertAdjacentHTML("afterbegin", newTextSrc);
            var match2 = /([a-zA-Zа-яА-Я])/.exec(nick);
            var letter;
            if (match2) {
                letter = match2[1];
            } else {
                letter = nick.charAt(0);
            }
            titleBlock.insertAdjacentHTML("afterbegin", "<div class=\"avatar\"><span class=\"image\"></span>" + letter + "</div>");

            if (validNick) {
                try {
                    if (PageInfo.enableAvatars) {
                        loadAvatar(titleBlock);
                    }
                } catch (ex) {
                    console.error(ex);
                }
            }
        }
        quote.classList.add("transformed");
    }
}

function loadAvatar(block) {
    var imageEl = block.querySelector(".avatar .image");
    if (imageEl.style.backgroundImage.indexOf("base64") != -1) {
        return;
    }
    var nick = block.querySelector(".name").innerHTML;
    loader.loadByNick(nick, function (loaded) {
        imageEl.style.backgroundImage = "url(\"" + loaded + "\")";
    })
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

        if (codeBlock.classList.contains('spoil')) {
            var btn = bb.querySelector('.spoil_close');
            if (!btn) {
                var btnsContainer = document.createElement("div");
                btnsContainer.classList.add("btns_container");
                btn = document.createElement('div');
                bb.appendChild(btnsContainer);
                btnsContainer.appendChild(btn);
                btn.innerHTML = 'Закрыть спойлер';
                btn.className = "spoil_close";
                btnsContainer.style.display = "none";
            }

            btn.addEventListener('click', clickBtn);

            function clickBtn(event) {
                clickOnElement(event);
                var t = event.target;
                while (!t.classList.contains('post_body') || !t.classList.contains('msg-content') || t != document.body) {
                    if (t.classList.contains('spoil')) {
                        t.scrollIntoView();
                        return;
                    }
                    t = t.parentElement;
                }

            }

        }
    }

    function clickOnElement(event) {

        var t = event.target;
        console.log("clickOnElement data-brackets-id " + t.getAttribute("data-brackets-id"));
        while (t.classList.contains("post-block") || !t.classList.contains('post_body') || !t.classList.contains('msg-content') || t != document.body) {
            console.log("clickOnElement " + t.getAttribute("data-brackets-id"));
            if (t.classList.contains('spoil')) {
                //event.stopPropagation();
                console.log("call spoilCloseButton data-brackets-id " + t.parentElement.getAttribute("data-brackets-id"));
                toggler("close", "open", t);
                spoilCloseButton(t);

                return;
            } else if (t.classList.contains('code')) {
                //event.stopPropagation();
                toggler("unbox", "box", t);
                return;
            }
            t = t.parentElement;
        }
    }

    /*function toggler(c, o, t) {
        if (t.classList.contains(c)) {
            t.classList.remove(c);
            t.classList.add(o);
            addImgesSrc(t);
        } else if (t.classList.contains(o)) {
            t.classList.remove(o);
            t.classList.add(c);
        }
    }*/
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
/**
 *		==================
 *		SPOIL CLOSE BUTTON
 *		==================
 */

function spoilCloseButton(t) {
    console.log("spoilCloseButton data-brackets-id " + t.getAttribute("data-brackets-id"));
    var el = t;
    if (t.classList.contains('spoil')) {

        t = t.querySelector(".block-body");

        console.log("body " + t);

        if (t.querySelector('img[src]')) {
            var images = t.querySelectorAll('img[src]');
            images[images.length - 1].addEventListener("load", function () {
                spoilCloseButton(el);
            });
        }


        for (var i = t.childNodes.length; i >= 0; i--) {
            var node = t.childNodes.item(i);
            console.log("NODE " + t.classList);
            if ((!!node) && (!!node.classList) && node.classList.contains("btns_container")) {
                var btn = node;
                console.log("SUKA " + (t.clientHeight > document.documentElement.clientHeight) + " : " + btn.style.display);
                console.log("data-brackets-id " + btn.parentElement.getAttribute("data-brackets-id"));
                if (t.clientHeight > document.documentElement.clientHeight) {
                    btn.style.display = "block";
                    console.log("SET BLYA BLOCK");
                    return;
                } else {
                    btn.style.display = "none";
                    console.log("SET BLYA NONE");
                    return;
                }
            }
        }

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
            //console.log("removeImgesSrc " + img.src + " : " + img.dataset.src);
            if (!img.hasAttribute('src') || img.dataset.imageSrc) continue;
            var srcUrl = img.dataset.src;
            if (!srcUrl) {
                srcUrl = img.src;
            }
            img.dataset.imageSrc = srcUrl;
            img.removeAttribute('src');
            img.removeAttribute('data-src');
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
                console.log("addImgesSrc " + img.src + " : " + img.dataset.imageSrc);
                if (img.hasAttribute('src') || !img.dataset.imageSrc) continue;
                img.src = img.dataset.imageSrc;
                img.removeAttribute('data-image-src');
                if (typeof corrector !== 'undefined')
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

function improveSpoilBlock() {
    var posts = document.querySelectorAll('.post_container');
    for (var j = 0; j < posts.length; j++) {
        var post = posts[j];
        var spoilBlockAll = post.querySelectorAll('.post-block.spoil');
        for (var i = 0; i < spoilBlockAll.length; i++) {
            try {
                var codeBlock = spoilBlockAll[i];
                var codeTitle = codeBlock.querySelector('.block-title')
                if (!codeBlock.classList.contains("improve")) {
                    codeTitle.insertAdjacentHTML("beforeEnd", '<div class="block-controls"><i class="link" data-spoil-number="' + (i + 1) + '" data-post-id="' + post.getAttribute("data-post-id") + '"></i></div>');
                    codeBlock.classList.add("improve");
                }
                codeTitle.querySelector('.link').addEventListener('click', function (e) {
                    e.stopPropagation();
                    var postId = e.target.getAttribute("data-post-id");
                    var spoilerNumber = e.target.getAttribute("data-spoil-number");
                    console.log(postId + " : " + spoilerNumber);
                    ITheme.copySpoilerLink(postId, spoilerNumber);
                });
            } catch (error) {
                console.log(error);
            }
        }
    }


}

nativeEvents.addEventListener("DOMContentLoaded", transformSnapbacks);
nativeEvents.addEventListener("DOMContentLoaded", transformQuotes);

nativeEvents.addEventListener("DOMContentLoaded", improveSpoilBlock);
nativeEvents.addEventListener("DOMContentLoaded", improveCodeBlock);
nativeEvents.addEventListener("DOMContentLoaded", blocksOpenClose);
nativeEvents.addEventListener("DOMContentLoaded", removeImgesSrc);
nativeEvents.addEventListener("DOMContentLoaded", addIcons);
