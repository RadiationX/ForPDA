window.addEventListener("DOMContentLoaded", transformImagesWithDensity);
function transformImagesWithDensity() {
    var images = document.querySelectorAll("img.attach, img.linked-image");
    var density = 3;
    //var density = window.devicePixelRatio;
    images.forEach(function (item, i, arr) {
        item.addEventListener("load", onLoadImage)
        
    });
    function onLoadImage(ev){
        var img = ev.target;
        var width = Number(img.width);
        var height = Number(img.height);
        
        width/=density;
        height/=density;
        console.log(width+" : "+height);
        img.setAttribute("width",  ""+width+"px");
        img.setAttribute("height",  ""+height+"px");
    }
}