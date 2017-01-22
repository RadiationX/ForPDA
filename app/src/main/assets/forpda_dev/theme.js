var anchorElem, elemToActivation;
var lastTop = 0,
    lastScrollTop = 0,
    lastDeltaTop = 0,
    lastDeltaScroll = 0;
//Костыль, чтобы нормально открывалась шапка, если скрыт post_header
var blockProgressChange = false;

//Вызывается при обновлении прогресса загрузке страницы и при загрузке её ресурсов
//По идеи должна верно скроллить к нужному элементу, даже если пользователь прокрутил страницу
//Как оно работает и работает ли вообще - объяснить не могу
function onProgressChanged() {
    if(blockProgressChange){
        blockProgressChange = false;
        return;
    }
    //ITheme.log("call onprogress");
    var newTop = getCoordinates(anchorElem).top;
    var newScrollTop = (window.pageYOffset || document.documentElement.scrollTop) - (document.documentElement.clientTop || 0);
    var tempDeltaTop = newTop - lastTop;
    var tempDeltaScroll = newScrollTop - lastScrollTop;
    var delta = tempDeltaTop;
    if (lastDeltaTop != 0)
        delta = delta + (lastDeltaTop - tempDeltaScroll + lastDeltaScroll);

    //ITheme.log("new: "+newTop+"  :  "+newScrollTop+"  :  "+tempDeltaTop+"  :  "+tempDeltaScroll+ "  :  "+delta);
    //console.log("prch: "+tempDeltaTop+"  :  "+tempDeltaScroll+"  :  "+lastDeltaTop+"  :  "+lastDeltaScroll+ "  :  "+delta);
    if (delta != 0) {
        window.scrollBy(0, delta);
    }

    lastTop = newTop;
    lastScrollTop = newScrollTop;
    lastDeltaTop = tempDeltaTop;
    lastDeltaScroll = tempDeltaScroll
}


//name может быть EventObject или строкок
//name это аттрибут тега html, может быть просто якорем или entry+post_id
//Вызывается из джавы, если находится на той-же странице, и в ссылке есть entry или якорь, а также при загрузке страницы
//PageInfo.elemToScroll - переменная, заданная в шаблоне в теге script, содержит в себе якорь или entry
//doOnLoadScroll - объект в window, задаётся false только когда была сделана перезагрузка страницы или переход назад
function scrollToElement(name) {
    //ITheme.log("call scroll to");
    if (typeof doOnLoadScroll !== "undefined")
        if (doOnLoadScroll == false)
            return;

    if (typeof name != 'string') name = PageInfo.elemToScroll;

    if (/([^-]*)-([\d]*)-(\d+)/g.test(name)) {
        var data = /([^-]*)-([\d]*)-(\d+)/g.exec(name);
        data[1] = data[1].toLowerCase();

        if (data[1] === "spoiler") data[1] = "spoil";
        if (data[1] === "hide") data[1] = "hidden";

        name = 'entry' + data[2];
        var post = document.querySelector('[name="' + name + '"]');
        anchorElem = post.querySelectorAll(".post-block." + data[1])[Number(data[3]) - 1];
    } else {
        anchorElem = document.querySelector('[name="' + name + '"]');
    }
    //console.log(anchorElem);
    var p = anchorElem;
    if (anchorElem) {
        while (p.classList && !p.classList.contains('post_body')) {
            if (p.classList.contains('spoil')) {
                p.classList.remove('close');
                p.classList.add('open');
            }
            p = p.parentNode;
        }
    }
    if(anchorElem)
        anchorElem.scrollIntoView();
    lastTop = getCoordinates(anchorElem).top;
    //window.scrollBy(0, lastTop);

    lastScrollTop = (window.pageYOffset || document.documentElement.scrollTop) - (document.documentElement.clientTop || 0);
    if (lastScrollTop == 0) {
        var temas = lastTop;
        window.addEventListener('load', function () {
            lastScrollTop = temas;
            lastTop = temas;
            onProgressChanged();
        });
    }

    //Активация элементов, убирается класс active с уже активированных
    if (elemToActivation)
        elemToActivation.classList.remove('active');

    elemToActivation = document.querySelector('.post_container[name="' + name + '"]');
    if(elemToActivation)
        elemToActivation.classList.add('active');
}

document.addEventListener('DOMContentLoaded', scrollToElement);


/**
 *		===================
 *		blocks close & open
 *		===================
 */

function blocksOpenClose() {
    var blockTitleAll = document.querySelectorAll('.post-block.spoil>.block-title,.post-block.code>.block-title'),
        bt;

    if (!blockTitleAll[0]) return;

    for (var i = 0; i < blockTitleAll.length; i++) {
        bt = blockTitleAll[i];
        var bb = bt.parentElement.querySelector('.block-body');
        if (bb.parentElement.classList.contains('code') && bb.scrollHeight <= bb.offsetHeight) bb.parentElement.classList.remove('box');
        bt.addEventListener('click', clickOnElement, false);
    }

    function clickOnElement(event) {
        var p = el().t.parentElement;

        function el() {
            var event = event || window.event;
            var target = event.target || event.srcElement;
            return {
                e: event,
                t: target
            };
        }
        if (p.classList.contains('spoil')) toggler("close", "open");
        if (p.classList.contains('code')) toggler("unbox", "box");

        function toggler(c, o) {
            if (p.classList.contains(c)) {
                p.classList.remove(c);
                p.classList.add(o);
            } else if (p.classList.contains(o)) {
                p.classList.remove(o);
                p.classList.add(c);
            }
        }
    }
}

document.addEventListener('DOMContentLoaded', blocksOpenClose);

function getCoordinates(elem) {
    if(!elem){
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
    blockProgressChange = true;
    var parent = button.parentNode;
    var body;
    if (bodyClass !== undefined)
        body = parent.querySelector("." + bodyClass);
    if (parent.classList.contains("close") | body.style.display == "none") {
        parent.classList.remove("close");
        parent.classList.add("open");
        if (body !== undefined){
            body.removeAttribute("hidden");
            //body.removeAttribute("style");
        }
    } else {
        parent.classList.remove("open");
        parent.classList.add("close");
        if (body !== undefined){
            body.setAttribute("hidden","");
            //body.style.overflow = "hidden";
            //body.style.height = "0";
            //body.style.visibility = "hidden";
        }
    }
}


document.addEventListener('DOMContentLoaded', improveCodeBlock);

function improveCodeBlock() {
	var codeBlockAll = document.querySelectorAll('.post-block.code');
	for (var i = 0; i < codeBlockAll.length; i++) {
        try{
            var codeBlock = codeBlockAll[i],
                codeTitle = codeBlock.querySelector('.block-title'),
                codeBody = codeBlock.querySelector('.block-body'),
                splitLines = codeBody.innerHTML.split("<br>"),
                count = '',
                lines = '';

            for(var j = 0; j<splitLines.length; j++){
                lines += '<span class="line">' + splitLines[j] + '</span><br>';
                count += (j + 1) + '\n';
            }

            codeBlock.classList.add('wrap');
            codeTitle.insertAdjacentHTML("afterEnd", '<div class="block-controls"><div class="control wrap"></div><div class="control select_all"></div></div><div class="num-pre">' + count + '</div>');
            codeBlock.querySelector('.control.wrap').addEventListener('click', onClickToggleButton);
            codeBlock.querySelector('.control.select_all').addEventListener('click', SelectText);
            codeBody.innerHTML = lines;
        }catch(error){
            alert(error);
        }
	}

	function onClickToggleButton(e) {
		e.stopPropagation();
        var button = e.target;
        var block;
		for (var i = 0; i < codeBlockAll.length; i++) {
            if(button==codeBlockAll[i].querySelector('.control.wrap')){
                block = codeBlockAll[i];
                break;
            }
		}
        if(!block)return;
        if (block.classList.contains('wrap')) {
            block.classList.remove('wrap');
		} else{
            block.classList.add('wrap');
        }
	}
    
    function SelectText(e) {
        e.stopPropagation();
        var button = e.target;
        var block;
		for (var i = 0; i < codeBlockAll.length; i++) {
            if(button==codeBlockAll[i].querySelector('.control.select_all')){
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
