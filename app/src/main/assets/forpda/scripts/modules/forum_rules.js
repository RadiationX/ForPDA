console.log("LOAD JS SOURCE forum_rules.js");

function setupListeners() {
    var items = document.querySelectorAll(".rules_list .item");
    items.forEach(function (item, i, arr) {
        if (!item.classList.contains("header")) {
            item.addEventListener("click", listener)
        }
    });

    function listener(ev) {
        var item = ev.target;
        while(!item.classList.contains("item")){
            item = item.parentElement;
        }
        var text = (item.textContent).trim();
        IRules.copyRule(text);
    }
}


nativeEvents.addEventListener("DOMContentLoaded", setupListeners);
