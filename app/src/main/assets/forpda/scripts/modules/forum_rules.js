console.log("LOAD JS SOURCE forum_rules.js");


function setupListeners() {
    var items = document.querySelectorAll(".rules_list .item");

    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (!item.classList.contains("header")) {
            item.addEventListener("click", listener)
        }
    }

    function listener(ev) {
        var item = ev.target;
        if (item.tagName == "A") {
            return;
        }
        while (!item.classList.contains("item")) {
            item = item.parentElement;
        }
        var text = (item.textContent).trim();
        if (typeof IRules != 'undefined') {
            IRules.copyRule(text);
        } else {
            console.log("Call copyRule");
        }
    }
}




nativeEvents.addEventListener(nativeEvents.DOM, setupListeners, true);
