console.log("LOAD JS SOURCE posts_functions.js");
function updateShowAvatarState(isShow) {
    console.log("updateShowAvatarState "+isShow);
    var posts = document.querySelectorAll(".post_container");
    for (var i = 0; i < posts.length; i++) {
        var post = posts[i];
        var isHidden = post.classList.contains("hide_avatar");
        var isShowed = post.classList.contains("show_avatar");
        if (isShow && isHidden) {
            post.classList.remove("hide_avatar");
            post.classList.add("show_avatar");
        } else if (!isShow && isShowed) {
            post.classList.remove("show_avatar");
            post.classList.add("hide_avatar");
        }
    }
}

function updateTypeAvatarState(isCircle) {
    console.log("updateTypeAvatarState "+isCircle);
    var posts = document.querySelectorAll(".post_container");
    for (var i = 0; i < posts.length; i++) {
        var post = posts[i];
        var isCircled = post.classList.contains("circle_avatar");
        var isSquared = post.classList.contains("square_avatar");
        if (isCircle && isSquared) {
            post.classList.remove("square_avatar");
            post.classList.add("circle_avatar");
        } else if (!isCircle && isCircled) {
            post.classList.remove("circle_avatar");
            post.classList.add("square_avatar");
        }
    }
}

function deletePost(id){
    id = Number(id);
    var post = document.querySelector("[data-post-id='" + id + "']");
    console.log(post);
    if(post){
        post.parentNode.removeChild(post);
    }
}