window.addEventListener("load", function () {
    var anchors = [];
    var links = document.querySelectorAll("a[name][title]");
    for (var i = 0; i < links.length; i++) {
        if (links[i].innerHTML === "ˇ") {
            anchors.push(links[i]);
        }
    }
    anchors.forEach(function (item, i, arr) {
        item.classList.add("anchor");
        item.innerHTML = "";
    });
    console.log(anchors);
})
