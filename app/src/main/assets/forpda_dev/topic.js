var ITheme = {
    log: function (arg) {
        console.log(arg);
    }
}

function getCoordinates(elem) {
    if (!elem) {
        return {
            top: 0,
            left: 0
        }
    }
    // (1)
    var box = elem.getBoundingClientRect();

    var body = document.body;
    var docEl = document.documentElement;

    // (2)
    var scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
    var scrollLeft = window.pageXOffset || docEl.scrollLeft || body.scrollLeft;

    // (3)
    var clientTop = docEl.clientTop || body.clientTop || 0;
    var clientLeft = docEl.clientLeft || body.clientLeft || 0;

    // (4)
    var top = box.top + scrollTop - clientTop;
    var left = box.left + scrollLeft - clientLeft;

    return {
        top: top,
        left: left
    };
}

function getScrollTop() {
    return (window.pageYOffset || document.documentElement.scrollTop) - (document.documentElement.clientTop || 0);
}


function scrollToElement(name) {
    if (typeof name != 'string') {
        name = PageInfo.elemToScroll;
    }
    var data = /([^-]*)-([\d]*)-(\d+)/g.exec(name);
    if (data) {
        //data[1] - name (spoil, quote, etc)
        //data[2] - post id
        //data[3] - number block of post, begin with 1
        data[1] = data[1].toLowerCase();
        if (data[1] === "spoiler") data[1] = "spoil";
        if (data[1] === "hide") data[1] = "hidden";
        anchorElem = document.querySelector('[name="entry' + data[2] + '"]');
        anchorElem = post.querySelectorAll(".post-block." + data[1])[Number(data[3]) - 1];
    } else {
        anchorElem = document.querySelector('[name="' + name + '"]');
    }
    if (anchorElem) {
        var post = anchorElem;
        while (post.classList && !post.classList.contains('post_body')) {
            if (post.classList.contains('spoil')) {
                post.classList.remove('close');
                post.classList.add('open');
            }
            post = post.parentNode;
        }

        window.addEventListener("load", function () {
            setTimeout(function () {
                anchorElem.scrollIntoView();
                lastTop = getCoordinates(anchorElem).top;
                lastScrollTop = getScrollTop();
                onProgressChanged();
            }, 1);
        });
    }

    //Активация элементов, убирается класс active с уже активированных
    if (elemToActivation)
        elemToActivation.classList.remove('active');

    elemToActivation = document.querySelector('.post_container[name="' + name + '"]');
    if (elemToActivation)
        elemToActivation.classList.add('active');
}
document.addEventListener('DOMContentLoaded', scrollToElement);







function suak() {

}
