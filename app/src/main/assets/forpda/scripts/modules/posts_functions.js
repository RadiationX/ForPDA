console.log("LOAD JS SOURCE posts_functions.js");

function updateShowAvatarState(isShow) {
    console.log("updateShowAvatarState " + isShow);

    var body = document.body;
    var isHidden = body.classList.contains("hide_avatar");
    var isShowed = body.classList.contains("show_avatar");
    if (isShow && isHidden) {
        body.classList.remove("hide_avatar");
        body.classList.add("show_avatar");
        PageInfo.enableAvatars = true;
    } else if (!isShow && isShowed) {
        body.classList.remove("show_avatar");
        body.classList.add("hide_avatar");
        PageInfo.enableAvatars = false;
    }

    var blocks = document.querySelectorAll(".post-block.quote .block-title");
    for (var i = 0; i < blocks.length; i++) {
        var titleBlock = blocks[i];
        if (PageInfo.enableAvatars) {
            loadAvatar(titleBlock);
        }
    }
}

function updateTypeAvatarState(isCircle) {
    console.log("updateTypeAvatarState " + isCircle);
    var body = document.body;
    var isCircled = body.classList.contains("circle_avatar");
    var isSquared = body.classList.contains("square_avatar");
    if (isCircle && isSquared) {
        body.classList.remove("square_avatar");
        body.classList.add("circle_avatar");
    } else if (!isCircle && isCircled) {
        body.classList.remove("circle_avatar");
        body.classList.add("square_avatar");
    }
}

function deletePost(id) {
    id = Number(id);
    var post = document.querySelector("[data-post-id='" + id + "']");
    console.log(post);
    if (post) {
        post.parentNode.removeChild(post);
    }
}


function fixImagesSizeWithDensity() {
    const density = window.devicePixelRatio;
    console.log("Density: " + density);
    if (density == 1) {
        return;
    }
    var images = document.querySelectorAll("img.attach, img.linked-image");
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
        var width = Number(img.width);
        var height = Number(img.height);
        console.log("WH: " + width + " : " + height);

        width /= density;
        height /= density;
        if (width > 16 && height > 16) {
            //console.log(width + " : " + height);
            img.setAttribute("width", "" + width + "px");
            img.setAttribute("height", "" + height + "px");
            img.classList.add("size_fixed");
        }
    }
}

nativeEvents.addEventListener("DOMContentLoaded", fixImagesSizeWithDensity);
