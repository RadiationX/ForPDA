console.log("LOAD JS SOURCE news.js");

function transformImages() {
    var lightBox = document.querySelectorAll("p > a[data-lightbox] > img, p > img, p > iframe");
    for (var i = 0; i < lightBox.length; i++) {
        var p = lightBox[i];
        while (p.tagName != "P") {
            p = p.parentElement;
        }
        console.log(p);
        p.classList.add("full_width");
    }
}

function transformPoll() {
    var polls = document.querySelectorAll("form");
    for (var i = 0; i < polls.length; i++) {
        var poll = polls[i];
        var submitButton = poll.querySelector("button[type=submit]");
        submitButton.setAttribute("type", "button");
        submitButton.addEventListener("click", function (ev) {
            var form = ev.target;
            while (form != null && form != undefined && form.nodeName != "FORM") {
                form = form.parentElement;
            }
            var id = /poll_id=(\d+)/g.exec(form.action)[1]
            var answer = form["answer[]"].value
            var from = form["from"].value;
            if (answer.length == 0) {
                return;
            }
            console.log(id + " : " + answer);
            INews.sendPoll(id, answer, from);
        });
    }
}
//nativeEvents.addEventListener(nativeEvents.DOM, fixImagesSizeWithDensity, true);
nativeEvents.addEventListener(nativeEvents.DOM, transformImages, true);
nativeEvents.addEventListener(nativeEvents.DOM, transformPoll, true);
