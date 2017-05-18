const ATTACH_TYPES = ["file", "picture"];

function Attachment() {

    this.attachType = ATTACH_TYPES[0];
    this.name = undefined;
    this.weight = undefined;
    this.format = undefined;
    this.count = undefined;
    this.link = undefined;
    this.image = undefined;
}
var metaPattern = /([\s\S]*)\.([\s\S]*?) - [^:]*?: ([\s\S]*)/g;
var namePattern = /([\s\S]*)\.([\s\S]*)/g
var weightPattern = /\( ([\s\S]*?) \)/g;
var countPattern = /(\d+)/g;


function parseFile(item, weight, count) {
    var attach = new Attachment();
    attach.link = item.href;
    var match;
    while (match = namePattern.exec(item.textContent)) {
        attach.name = match[1];
        attach.format = match[2];
    }
    while (match = weightPattern.exec(weight.textContent)) {
        attach.weight = match[1];
    }
    while (match = countPattern.exec(count.textContent)) {
        attach.count = match[1];
    }
    return attach;
}

function parseTable(item) {
    console.log("PARSE TABLE");
    console.log(item);
    item = item.querySelector("a");
    var attach = new Attachment();
    attach.attachType = ATTACH_TYPES[1];
    attach.link = item.href;
    attach.image = item.querySelector("img").src;
    console.log(item.querySelector("img"));
    console.log(attach.image + " : " + (!attach.image && attach.image.length == 0) + " : " + (!!attach.image) + " : " + (attach.image.length == 0));
    if (typeof attach.image == "string" && attach.image.length == 0) {
        attach.image = item.querySelector("img").dataset.imageSrc;
    }
    var match;
    while (match = metaPattern.exec(item.getAttribute("title"))) {
        attach.name = match[1];
        attach.weight = match[3];
    }
    while (match = namePattern.exec(attach.image)) {
        attach.format = match[2];
    }
    return attach;
}

function parseImage(item) {
    var attach = new Attachment();
    attach.attachType = ATTACH_TYPES[1];
    attach.image = item.src;
    if (typeof attach.image == "string" && attach.image.length == 0) {
        attach.image = item.dataset.imageSrc;
    }
    attach.link = attach.image;
    var match;
    while (match = namePattern.exec(attach.image)) {
        attach.format = match[2];
    }
    return attach;
}

function transformAttachments(postContainer) {
    console.log(postContainer);
    var postAttachments = [];
    var files = postContainer.querySelectorAll(".edit");
    var findedFiles = [];
    var tableEl = [],
        imageEl = [],
        fileEl = [];
    for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file.textContent.includes("Прикрепленные")) {
            findedFiles.push(file.parentNode.parentNode);
        }
    }
    var toDel = [];
    for (var i = 0; i < findedFiles.length; i++) {
        var file = findedFiles[i];
        var temp = file;
        while (temp) {
            temp = temp.previousSibling;
            if(!temp) break;
            if (temp.nodeName === "BR") {
                toDel.push(temp);
            } else if (temp.nodeName != "#text") {
                break;
            }
        }
        temp = file;
        while (temp) {
            temp = temp.nextSibling;
            if(!temp) break;
            if (temp.nodeName === "BR") {
                toDel.push(temp);
            } else if (temp.nodeName != "#text") {
                break;
            }
        }
    }

    for (var i = 0; i < findedFiles.length; i++) {
        var file = findedFiles[i];
        var temp = file;
        var found = false;
        var lastBreak = false;;
        while (temp && !(temp.classList && temp.classList.contains("edit"))) {
            temp = temp.nextSibling;
            //console.log(temp);
            if (!temp) break;
            if (temp.classList && temp.classList.contains("ipb-attach")) {
                found = true;
                lastBreak = false;
                var elem = temp;
                temp = temp.nextSibling;
                var size = temp;
                temp = temp.nextSibling;
                var count = temp;
                toDel.push(elem);
                toDel.push(size);
                toDel.push(count);
                postAttachments.push(parseFile(elem, size, count));
            } else if (temp.nodeName == "TABLE" && temp.getAttribute("id").includes("ipb-attach")) {
                found = true;
                lastBreak = false;
                postAttachments.push(parseTable(temp));
            } else if (temp.classList && temp.classList.contains("linked-image")) {
                found = true;
                lastBreak = false;
                postAttachments.push(parseImage(temp));
            } else if (temp.nodeName == "BR") {
                if (lastBreak) {
                    break;
                }
                lastBreak = true;
            } else if (temp.nodeName != "BR" && temp.nodeName != "#text" && found) {
                break;
            }
            toDel.push(temp);
        }
        toDel.push(file);
    }
    //console.log(findedFiles[0].querySelectorAll("~ .ipb-attach"));

    files = postContainer.querySelectorAll(".post-block.spoil");
    findedFiles = [];
    for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file.querySelector(".block-title").textContent.includes("Прикрепленные")) {
            findedFiles.push(file);
        }
    }

    for (var i = 0; i < findedFiles.length; i++) {
        var file = findedFiles[i];
        var items = file.querySelectorAll("table");
        for (var j = 0; j < items.length; j++) {
            var item = items[j];
            postAttachments.push(parseTable(item));
        }

        items = file.querySelectorAll("img.linked-image");
        for (var j = 0; j < items.length; j++) {
            var item = items[j];
            postAttachments.push(parseImage(item));
        }
        toDel.push(file);
    }









    //return;
    for (var j = 0; j < toDel.length; j++) {
        //console.log(toDel[j]);
        try {
            toDel[j].parentNode.removeChild(toDel[j]);
        } catch (e) {
            console.error("Maybe povtero " + e);
        }
    }
    toDel = [];
    if (postAttachments.length > 0) {
        var attachmentsContainer = "<div class=\"attachments\">";
        attachmentsContainer += "<div class=\"title\">Прикрепленные файлы</div>";
        attachmentsContainer += "<div class=\"scroll_container\">";
        for (var i = 0; i < postAttachments.length; i++) {
            var attach = postAttachments[i];
            console.log(attach);
            var attachSrc = "<a href=\"" + attach.link + "\" class=\"attach " + attach.attachType + "\">";
            if (attach.attachType == ATTACH_TYPES[1]) {
                attachSrc += "<span class=\"image\" style=\"background-image: url(" + attach.image + ")\"></span>";
                attachSrc += "<span class=\"meta weight\">" + attach.format + (attach.weight ? ", " + attach.weight : "") + "</span>";
            } else {
                attachSrc += "<span class=\"meta format\">" + attach.format + "</span>";
                if (attach.count) {
                    attachSrc += "<span class=\"meta count\">" + attach.count + "</span>";
                }
                attachSrc += "<span class=\"meta name\">" + attach.name + "<span class=\"weight\">" + attach.weight + "</span></span>";

            }


            attachSrc += "</a>";
            attachmentsContainer += attachSrc;
        }

        attachmentsContainer += "</div></div>";
        postContainer.insertAdjacentHTML("beforeend", attachmentsContainer);
        console.log(attachmentsContainer);
    }
}

window.addEventListener("DOMContentLoaded", function (e) {
    console.log("START");
    var date = new Date().getTime();
    var postContainers = document.querySelectorAll(".post_body");
    for (var c = 0; c < postContainers.length; c++) {
        var postContainer = postContainers[c];
        transformAttachments(postContainer);
    }
    console.log("END");
    //alert(new Date().getTime()-date);
});
