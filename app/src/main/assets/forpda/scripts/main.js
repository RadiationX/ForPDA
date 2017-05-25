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
                if (func != undefined || func != null) {
                    console.log("Call function: " + func.name);
                }
                func();
            } catch (e) {
                console.error(e);
            }
        }
    }

    document.addEventListener("DOMContentLoaded", function (e) {
        console.log("JS event: DOMContentLoaded");
        try {
            ITheme.domContentLoaded();
        } catch (ignore) {
            onNativeDomComplete();
        }
    });
    window.addEventListener("load", function (e) {
        console.log("JS event: load");
        try {
            ITheme.onPageLoaded();
        } catch (ignore) {
            onNativePageComplete();
        }
    });

    this.addEventListener = function (name, func) {
        if (name === "DOMContentLoaded") {
            nativeDomComplete.push(func);
        }
        if (name === "load") {
            nativePageComplete.push(func);
        }
    }

    this.onNativeDomComplete = function () {
        onNativeDomComplete();
    }

    this.onNativePageComplete = function () {
        onNativePageComplete();
    }
}
