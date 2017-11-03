console.log("LOAD JS SOURCE search.js");

function measurePostHeight() {
    var hats = document.querySelectorAll(".hat_content");
    for (var i = 0; i < hats.length; i++) {
        var hat = hats[i];
        if(hat.clientHeight >= 360){
            hat.classList.add("over_height");
        }else{
            hat.parentElement.querySelector(".hat_button").style.display = "none";
        }
    }

}

nativeEvents.addEventListener(nativeEvents.DOM, measurePostHeight, true);
