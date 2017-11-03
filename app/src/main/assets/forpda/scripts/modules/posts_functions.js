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

nativeEvents.addEventListener(nativeEvents.DOM, fixImagesSizeWithDensity, true);
