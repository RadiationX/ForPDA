window.addEventListener("DOMContentLoaded", function (e) {
    var snapBacks = document.querySelectorAll("a[href*=findpost][title='Перейти к сообщению'],a[href*=showuser]");
    for (var i = 0; i < snapBacks.length; i++) {
        var snapBack = snapBacks[i];
        if (!snapBack.href.includes("showuser")) {
            var temp = snapBack.nextElementSibling;
            snapBack.innerHTML = temp.textContent;
            temp = snapBack;
            while (temp != null) {
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
        snapBack.classList.add("snapback");
    }
});

window.addEventListener("DOMContentLoaded", function (e) {
    var quotes = document.querySelectorAll(".post-block.quote");
    var myRegexp = /([\s\S]*?) @ ((?:\d+.\d+.\d+|[a-zA-zа-я-А-Я]+)(?:, \d+:\d+)?)/g;
    for (var i = 0; i < quotes.length; i++) {
        var quote = quotes[i];
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
            if(match2){
                nick = match2[1];
            }else{
                nick = nick.charAt(0);
            }
            titleBlock.querySelector(".icon").innerHTML = nick;
        }
    }
});
