console.log("LOAD JS SOURCE main.js");
var nativeEvents = new NativeEvents();

function NativeEvents() {
    var nativeDomComplete = [];
    var nativePageComplete = [];

    function onNativeDomComplete() {
        console.log("JS event: onNativeDomComplete");
        functionCaller(nativeDomComplete);
    }

    function onNativePageComplete() {
        console.log("JS event: onNativePageComplete");
        functionCaller(nativePageComplete);
    }

    function functionCaller(funcArray) {
        while (funcArray.length > 0) {
            var func = funcArray.shift();
            try {
                console.log("Call function: " + func.name);
                func();
            } catch (e) {
                console.error(e);
            }
        }
    }

    document.addEventListener("DOMContentLoaded", function (e) {
        console.log("JS event: DOMContentLoaded");
        if (typeof IBase != 'undefined') {
            IBase.domContentLoaded();
        } else {
            onNativeDomComplete();
        }
    });
    window.addEventListener("load", function (e) {
        console.log("JS event: load");
        if (typeof IBase != 'undefined') {
            IBase.onPageLoaded();
        } else {
            onNativePageComplete();
        }
    });

    this.addEventListener = function (name, func) {
        try {
            if (name == undefined | name == null | typeof name != "string") {
                throw new Error("Name invalid");
            }
            if (func == undefined | func == null | typeof func != "function") {
                throw new Error("Function invalid")
            }
            if (name === "DOMContentLoaded") {
                nativeDomComplete.push(func);
            }
            if (name === "load") {
                nativePageComplete.push(func);
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

nativeEvents.addEventListener("DOMContentLoaded", function () {
    var elemsForClick = document.querySelectorAll(".post-block .block-title");
    for (var i = 0; i < elemsForClick.length; i++) {
        elemsForClick[i].classList.add("aec");
        /*console.log(elemsForClick[i]);
        if (!getEventListeners(elemsForClick[i]).includes(androidEffectClickListener)) {
            elemsForClick[i].addEventListener("click", androidEffectClickListener);
        }*/
    }
    document.addEventListener("click", androidEffectClickListener);
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

function suka(target, x, y) {
    var coords = getCoordinates(target);
    //console.log(target.nodeName + " : " + coords.top + " : " + coords.left + " : " + target.offsetHeight + " : " + target.offsetWidth + " : " + y + " : " + x);
    var yNorm = y <= (coords.top + target.offsetHeight) && y >= coords.top;
    var xNorm = x <= (coords.left + target.offsetWidth) && x >= coords.left;

    return yNorm && xNorm;
}
const androidEffectClickListener = function AndroidEffectClick(e) {
    var target = e.target;
    //console.log(e);
    if (target.nodeName == "A") {
        playClickEffect();
        return;
    }

    var temp = target.parentNode;
    while (temp && temp.classList && !temp.classList.contains("aec")) {
        temp = temp.parentNode;
    }
    //console.log(target.parentElement);
    //console.log(temp);
    var targetContainClass = target.classList && target.classList.contains("aec");
    var parentContainClass = temp != document && temp.classList && temp.classList.contains("aec");
    var targetBelong = suka(target, e.pageX, e.pageY);
    //var parentBelong = suka(temp, e.clientX, e.clientY);
    var parentBelong = false;
    //console.log(targetContainClass + " : " + parentContainClass + " : " + targetBelong + " : " + parentBelong);
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

    this.loadByNick = loadByNick;
    this.loadByUrl = loadByUrl;

}
