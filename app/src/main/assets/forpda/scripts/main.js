console.log("LOAD JS SOURCE main.js");
(function () {
    //Element.prototype.eventListenerList = {};

    Element.prototype._addEventListener = Element.prototype.addEventListener;
    Element.prototype.addEventListener = function (type, listener, useCapture) {
        this._addEventListener(type, listener, useCapture);

        if (!this.eventListenerList)
            this.eventListenerList = [];
        this.eventListenerList.push(listener);

        if (!this.eventListenerList[type])
            this.eventListenerList[type] = [];
        this.eventListenerList[type].push(listener);
    };

    Element.prototype._removeEventListener = Element.prototype.removeEventListener;
    Element.prototype.removeEventListener = function (type, listener, useCapture) {
        this._removeEventListener(type, listener, useCapture);
        this.eventListenerList[type] = "was removed";
    }

    return getEventListeners = function (target) {
        return !target.eventListenerList ? [] : target.eventListenerList;
    };
})();

function NativeEvents() {
    this.DOM = "DOMContentLoaded";
    this.PAGE = "load";

    const DOM = this.DOM;
    const PAGE = this.PAGE;
    const LOG_TAG = "JS event: ";

    var nativeDomComplete = [];
    var nativePageComplete = [];
    var instantDomComplete = [];
    var instantPageComplete = [];

    function onNativeDomComplete() {
        console.log(LOG_TAG + "onNativeDomComplete");
        functionCaller(nativeDomComplete);
    }

    function onNativePageComplete() {
        console.log(LOG_TAG + "onNativePageComplete");
        functionCaller(nativePageComplete);
    }

    function functionCaller(funcArray) {
        while (funcArray.length > 0) {
            var func = funcArray.shift();
            try {
                console.log(LOG_TAG + "Call function: '" + func.name + "'");
                func();
            } catch (e) {
                console.error(e);
            }
        }
    }

    document.addEventListener(DOM, function (e) {
        console.log(LOG_TAG + DOM);
        if (instantDomComplete.length > 0) {
            console.log(LOG_TAG + "Call instant functions");
            functionCaller(instantDomComplete);
        }
        if (typeof IBase != 'undefined') {
            IBase.domContentLoaded();
        } else {
            onNativeDomComplete();
        }
    });
    window.addEventListener(PAGE, function (e) {
        console.log(LOG_TAG + PAGE);
        if (instantPageComplete.length > 0) {
            console.log(LOG_TAG + "Call instant functions");
            functionCaller(instantPageComplete);
        }
        if (typeof IBase != 'undefined') {
            IBase.onPageLoaded();
        } else {
            onNativePageComplete();
        }
    });

    this.addEventListener = function (name, func, instantly) {
        instantly = Boolean(instantly | false);
        try {
            if (name == undefined | name == null | typeof name != "string") {
                throw new Error("Name invalid");
            }
            if (func == undefined | func == null | typeof func != "function") {
                throw new Error("Function invalid")
            }
            if (name === DOM) {
                if (instantly) {
                    instantDomComplete.push(func);
                } else {
                    nativeDomComplete.push(func);
                }
            }
            if (name === PAGE) {
                if (instantly) {
                    instantPageComplete.push(func);
                } else {
                    nativePageComplete.push(func);
                }
            }
        } catch (err) {
            console.error(err);
        }
    }

    this.onNativeDomComplete = function () {
        onNativeDomComplete();
    }

    this.onNativePageComplete = function () {
        onNativePageComplete();
    }
}
var nativeEvents = new NativeEvents();

nativeEvents.addEventListener(nativeEvents.DOM, function () {
    var elemsForClick = document.querySelectorAll(".post-block .block-title");
    for (var i = 0; i < elemsForClick.length; i++) {
        elemsForClick[i].classList.add("aec");
    }
    document.addEventListener("click", AndroidEffectClick);
});

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

function isBelong(target, x, y) {
    var coords = getCoordinates(target);
    var yNorm = y <= (coords.top + target.offsetHeight) && y >= coords.top;
    var xNorm = x <= (coords.left + target.offsetWidth) && x >= coords.left;
    return yNorm && xNorm;
}

function AndroidEffectClick(e) {
    var target = e.target;
    if (target.nodeName == "A") {
        playClickEffect();
        return;
    }
    var temp = target.parentNode;
    while (temp &&
        temp.classList &&
        !temp.classList.contains("aec")) {
        temp = temp.parentNode;
    }
    var targetContainClass =
        target.classList &&
        target.classList.contains("aec");
    var parentContainClass =
        temp != document &&
        temp.classList &&
        temp.classList.contains("aec");
    var targetBelong = isBelong(target, e.pageX, e.pageY);
    if ((targetContainClass || parentContainClass) && targetBelong) {
        playClickEffect();
    }
}

function playClickEffect() {
    console.log("PLAY CLICK");
    try {
        IBase.playClickEffect();
    } catch (ex) {
        console.info(ex.message);
    }
}


function AvatarLoader() {
    var loadedAvatars = {};
    var pendingLoad = {};
    this.loadByNick = loadByNick;
    this.loadByUrl = loadByUrl;

    function registerRequest(nick, callback) {
        var loaded = loadedAvatars[nick];
        if (loaded != undefined) {
            console.log("RETURN CACHED");
            callback(loaded);
            return;
        }
        var pending = pendingLoad[nick];
        var isPending = pending != undefined;
        if (!isPending) {
            pendingLoad[nick] = [];
            pending = pendingLoad[nick];
        }


        pending.push(callback);
        return isPending;
    }

    function onXhrResponse(nick, xhr) {
        if (xhr.status != 200) {
            console.error(xhr.status + ': ' + xhr.statusText);
            return;
        }
        //console.log("AvResp: " + xhr.status + " : " + xhr.responseText.length);
        var responseText = xhr.responseText;
        if (xhr.responseText.length == 0) {
            return;
        }

        loadedAvatars[nick] = responseText;
        var loaded = loadedAvatars[nick];

        for (var i = 0; i < pendingLoad[nick].length; i++) {
            var item = pendingLoad[nick][i];
            try {
                item(loaded);
            } catch (ex) {
                console.error(ex);
            }
        }

        pendingLoad[nick] = [];
    }

    function loadByNick(nick, callback) {
        var isPending = registerRequest(nick, callback);
        if (!isPending) {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'app_cache:avatars?nick=' + nick, true);
            xhr.send();
            xhr.onload = function () {
                onXhrResponse(nick, xhr);
            }
        }
    }

    function loadByUrl(nick, url, callback) {
        var isPending = registerRequest(nick, callback);
        if (!isPending) {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'app_cache:avatars?nick=' + nick, true);
            xhr.send();
            xhr.onload = function () {
                onXhrResponse(nick, xhr);
            }
        }
    }
}

function toggleButton(button, bodyClass, name) {
    var parent = button.parentNode;
    var body;
    if (bodyClass !== undefined)
        body = parent.querySelector("." + bodyClass);
    console.log("toggle " + parent.getAttribute("class") + " : " + body.getAttribute("class"));
    if (parent.classList.contains("close") | (body != undefined && parent.classList.contains("close"))) {
        parent.classList.remove("close");
        parent.classList.add("open");
        if (body !== undefined) {
            body.classList.remove("close");
            body.classList.add("open");
            //body.removeAttribute("hidden");
        }
        if (name === "poll") {
            IThemePresenter.setPollOpen("true");
        } else if (name === "hat") {
            IThemePresenter.setHatOpen("true");
        }
    } else {
        parent.classList.add("once-opened");
        parent.classList.remove("open");
        parent.classList.add("close");
        if (body !== undefined) {
            body.classList.remove("open");
            body.classList.add("close");
            //body.setAttribute("hidden", "");
        }
        if (name === "poll") {
            IThemePresenter.setPollOpen("false");
        } else if (name === "hat") {
            IThemePresenter.setHatOpen("false");
        }
    }
}

function fixImagesSizeWithDensity() {
    const density = window.devicePixelRatio;
    //const density = 5;
    console.log("Density: " + density);
    if (density == 1) {
        return;
    }
    var selector = "";
    var pageType = document.body.getAttribute("id") || "";
    switch (pageType) {
        case "announce":
            selector = "img";
            break;
        case "news":
            selector = "img";
            break;
        case "qms":
            selector = "img.emoticon";
            break;
        default:
            selector = "img.attach, img.linked-image, img.emoticon";
            break;
    }
    var images = document.querySelectorAll(selector);
    for (var i = 0; i < images.length; i++) {
        var item = images[i];
        fixSize(item);
        item.addEventListener("load", onLoadImage);
    }

    function onLoadImage(ev) {
        fixSize(ev.target);
    }

    function fixSize(img) {
        if (img.classList.contains("size_fixed")) {
            return;
        }
        var srcWidth = Number(img.width);
        var srcHeight = Number(img.height);
        
        if (srcWidth == 0 || srcWidth == 0) {
            return;
        }

        var width = srcWidth / density;
        var height = srcHeight / density;
        console.error("WH: " + width + " : " + height + "; "+img.src);
        if (width > 16 && height > 16) {
            //console.log(width + " : " + height);
            setEmSize(img, width, height);
        } else {
            setEmSize(img, srcWidth, srcHeight);
        }
        img.classList.add("size_fixed");
        
        //console.error("WH_ATTR: " + img.getAttribute("width") + " : " + img.getAttribute("width")+" :__: "+img.width+" : "+img.height);
    }
    
    function setEmSize(img, width, height){
        var wStr = "" + (width/16) + "em";
        var hStr = "" + (height/16) + "em";
        img.setAttribute("width", wStr);
        img.setAttribute("height", hStr);
        img.style.width = wStr;
        img.style.height = hStr;
    }
}

function escapeHtml(unsafe) {
    return unsafe
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
 }

 function changeStyleType(type) {
    console.log("changeStyleType: "+type+", typeof="+(typeof type))
    if(type !== "light" && type !== "dark"){
        console.log("Unknown style type: "+type)
        return
    }
    var styleLinks = document.querySelectorAll("link");
    for (var i = 0; i < styleLinks.length; i++) {
        var currentHref = styleLinks[i].href
        styleLinks[i].href = currentHref.replace(/\/(light|dark)\/(light|dark)/, "/" + type + "/" + type)
    }
 }
