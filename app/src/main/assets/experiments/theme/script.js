/*var resources = window.performance.getEntriesByType("resource");
resources.forEach(function (resource) {
    console.log(resource.name);
});*/

window.addEventListener("load", function () {
    /*var styled = document.querySelectorAll("*[style]");
    console.log(styled);
    for (var i = 0; i < styled.length; i++) {
        var stld = styled[i];
        console.log(getComputedStyle(stld, null).backgroundImage);
    }*/

   /* getDataUri('http://s.4pda.to/JK4K47hyIGLskyz0baQ8n7HatBz1rEZPv8KSe64KVaH15T3hmm.gif', function (dataUri) {
        // Do whatever you'd like with the Data URI!
        console.log(dataUri);
    });

    getDataUri('http://s.4pda.to/JK4K4BZXln6gU7tKqz2b6rtdQ70cwIZGFOYh0BCZ2EnTXbKmm.jpg', function (dataUri) {
        // Do whatever you'd like with the Data URI!
        console.log(dataUri);
    });*/

    var xhr = new XMLHttpRequest();
    xhr.open('GET', 'http://s.4pda.to/JK4K4BZXln6gU7tKqz2b6rtdQ70cwIZGFOYh0BCZ2EnTXbKmm.jpg', true);
    xhr.send();
    if (xhr.status != 200) {
        console.error(xhr.status + ': ' + xhr.statusText); // пример вывода: 404: Not Found
    } else {
        console.log(xhr);
    }
})

function getDataUri(url, callback) {
    var image = new Image();

    image.crossOrigin = "anonymous";
    image.setAttribute('crossOrigin', 'anonymous');
    image.onload = function () {
        var canvas = document.createElement('canvas');
        canvas.width = this.naturalWidth; // or 'width' if you want a special/scaled size
        canvas.height = this.naturalHeight; // or 'height' if you want a special/scaled size


        canvas.getContext('2d').drawImage(this, 0, 0);
        console.log(canvas);
        document.body.insertBefore(canvas, document.body.childNodes[0]);
        //callback(canvas.toDataURL('image/png').replace(/^data:image\/(png|jpg);base64,/, ''));

        // ... or get as Data URI

        canvas.toDataURL('image/jpeg')
        callback(canvas.toDataURL('image/jpeg'));

    };

    image.src = url;
}

// Usage
