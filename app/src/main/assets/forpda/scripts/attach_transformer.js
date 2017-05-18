function AttachmentTransformer() {
    function Attachment() {
        this.attachType = TYPE_FILE;
        this.name = undefined;
        this.weight = undefined;
        this.extension = undefined;
        this.count = undefined;
        this.link = undefined;
        this.image = undefined;
    }
    const TYPE_FILE = "file";
    const TYPE_PICTURE = "picture";
    //1-name, 2-extension, 3-weight
    const metaPattern = /([\s\S]*)\.([\s\S]*?) - [^:]*?: ([\s\S]*)/g;
    //1-name, 2-extension
    const namePattern = /([\s\S]*)\.([\s\S]*)/g
    //1-weight
    const weightPattern = /\( ([\s\S]*?) \)/g;
    //1-count
    const countPattern = /(\d+)/g;


    function parseFile(item, weight, count) {
        var attach = new Attachment();
        attach.link = item.href;
        var match;
        while (match = namePattern.exec(item.textContent)) {
            attach.name = match[1];
            attach.extension = match[2];
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
        item = item.querySelector("a");
        var attach = new Attachment();
        attach.attachType = TYPE_PICTURE;
        attach.link = item.href;
        attach.image = item.querySelector("img").src;
        if (typeof attach.image == "string" && attach.image.length == 0) {
            attach.image = item.querySelector("img").dataset.imageSrc;
        }
        var match;
        while (match = metaPattern.exec(item.getAttribute("title"))) {
            attach.name = match[1];
            attach.weight = match[3];
        }
        while (match = namePattern.exec(attach.image)) {
            attach.extension = match[2];
        }
        return attach;
    }

    function parseImage(item) {
        var attach = new Attachment();
        attach.attachType = TYPE_PICTURE;
        attach.image = item.src;
        if (typeof attach.image == "string" && attach.image.length == 0) {
            attach.image = item.dataset.imageSrc;
        }
        attach.link = attach.image;
        var match;
        while (match = namePattern.exec(attach.image)) {
            attach.extension = match[2];
        }
        return attach;
    }

    function createView(postAttachments, postContainer) {
        if (postAttachments.length > 0) {
            var attachmentsContainer = "<div class=\"attachments\">";
            attachmentsContainer += "<div class=\"title\">Прикрепленные файлы</div>";
            attachmentsContainer += "<div class=\"scroll_container\">";
            for (var i = 0; i < postAttachments.length; i++) {
                var attach = postAttachments[i];
                var attachSrc = "<a href=\"" + attach.link + "\" class=\"attach " + attach.attachType + "\">";
                if (attach.attachType == TYPE_PICTURE) {
                    attachSrc += "<span class=\"image\" style=\"background-image: url(" + attach.image + ")\"></span>";
                    attachSrc += "<span class=\"meta weight\">" + attach.extension + (attach.weight ? ", " + attach.weight : "") + "</span>";
                } else {
                    attachSrc += "<span class=\"meta extension\">" + attach.extension + "</span>";
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
        }
    }

    function findPreviousBr(targetNodes, nodesForRemove) {
        for (var i = 0; i < targetNodes.length; i++) {
            var node = targetNodes[i];
            var tmp = node;
            while (tmp) {
                tmp = tmp.previousSibling;
                if (!tmp) break;
                if (tmp.nodeName === "BR") {
                    nodesForRemove.push(tmp);
                } else if (tmp.nodeName != "#text") {
                    break;
                }
            }
        }
    }

    function fromPostBlock(nodesForRemove, postAttachments, postContainer) {
        var nodes = postContainer.querySelectorAll(".post-block.spoil");
        var targetNodes = [];
        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            if (node.querySelector(".block-title").textContent.includes("Прикрепленные")) {
                targetNodes.push(node);
            }
        }

        findPreviousBr(targetNodes, nodesForRemove);

        for (var i = 0; i < targetNodes.length; i++) {
            var node = targetNodes[i];
            var items = node.querySelectorAll("table");
            for (var j = 0; j < items.length; j++) {
                var item = items[j];
                postAttachments.push(parseTable(item));
            }

            items = node.querySelectorAll("img.linked-image");
            for (var j = 0; j < items.length; j++) {
                var item = items[j];
                postAttachments.push(parseImage(item));
            }
            nodesForRemove.push(node);
        }
    }

    function fromPlainText(nodesForRemove, postAttachments, postContainer) {
        var nodes = postContainer.querySelectorAll(".edit");
        var targetNodes = [];


        for (var i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            if (node.textContent.includes("Прикрепленные")) {
                targetNodes.push(node.parentNode.parentNode);
            }
        }

        findPreviousBr(targetNodes, nodesForRemove);
        for (var i = 0; i < targetNodes.length; i++) {
            var node = targetNodes[i];
            var temp = node;
            var found = false;
            var isLastBr = false;;
            while (temp && !(temp.classList && temp.classList.contains("edit"))) {
                temp = temp.nextSibling;
                if (!temp) break;
                if (temp.classList && temp.classList.contains("ipb-attach")) {
                    found = true;
                    isLastBr = false;
                    var elem = temp;
                    temp = temp.nextSibling;
                    var size = temp;
                    temp = temp.nextSibling;
                    var count = temp;
                    nodesForRemove.push(elem);
                    nodesForRemove.push(size);
                    nodesForRemove.push(count);
                    postAttachments.push(parseFile(elem, size, count));
                } else if (temp.nodeName == "TABLE" && temp.getAttribute("id").includes("ipb-attach")) {
                    found = true;
                    isLastBr = false;
                    postAttachments.push(parseTable(temp));
                } else if (temp.classList && temp.classList.contains("linked-image")) {
                    found = true;
                    isLastBr = false;
                    postAttachments.push(parseImage(temp));
                } else if (temp.nodeName == "BR") {
                    if (isLastBr) {
                        break;
                    }
                    isLastBr = true;
                } else if (temp.nodeName != "BR" && temp.nodeName != "#text" && found) {
                    break;
                }
                nodesForRemove.push(temp);
            }
            nodesForRemove.push(node);
        }
    }

    function removeNodes(nodesForRemove) {
        for (var j = 0; j < nodesForRemove.length; j++) {
            try {
                nodesForRemove[j].parentNode.removeChild(nodesForRemove[j]);
            } catch (e) {
                console.error("Handle error delete: " + e);
            }
        }
    }

    this.transform = function (postContainer) {
        var postAttachments = [];
        var nodesForRemove = [];
        fromPlainText(nodesForRemove, postAttachments, postContainer);
        fromPostBlock(nodesForRemove, postAttachments, postContainer);
        removeNodes(nodesForRemove);
        createView(postAttachments, postContainer);
    }
}


window.addEventListener("DOMContentLoaded", function (e) {
    console.log("START");
    var date = new Date().getTime();
    var postContainers = document.querySelectorAll(".post_body");
    var transformer = new AttachmentTransformer();
    for (var i = 0; i < postContainers.length; i++) {
        var postContainer = postContainers[i];
        transformer.transform(postContainers[i])
    }
    console.log("END");
    console.log(new Date().getTime() - date);
});
