function AvatarLoader() {
    var loadedAvatars = {};
    var pendingLoad = {};

    function registerRequest(nick, callback) {
        var loaded = loadedAvatars[nick];
        if (loaded != undefined) {
            console.log("RETURN CACHED");
            callback(loaded);
            return;
        }
        var pending = pendingLoad[nick];
        var isPending = pending != undefined;
        if (!isPending) {
            pendingLoad[nick] = [];
            pending = pendingLoad[nick];
        }


        pending.push(callback);
        return isPending;
    }

    function onXhrResponse(xhr) {
        if (xhr.status != 200) {
            console.error(xhr.status + ': ' + xhr.statusText);
            return;
        }
        console.log("AvResp: " + xhr.status + " : " + xhr.responseText.length);
        var responseText = xhr.responseText;
        if (xhr.responseText.length == 0) {
            return;
        }

        loadedAvatars[nick] = responseText;
        var loaded = loadedAvatars[nick];

        pendingLoad[nick].forEach(function (item, i, arr) {
            try {
                item(loaded);
            } catch (ex) {
                console.error(ex);
            }
        });
        pendingLoad[nick] = [];
    }

    function loadByNick(nick, callback) {
        var isPending = registerRequest(nick, callback);
        if (!isPending) {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'app_cache:avatars?nick=' + nick, true);
            xhr.send();
            xhr.onload = function () {
                onXhrResponse(xhr);
            }
        }
    }

    function loadByUrl(nick, url, callback) {
        var isPending = registerRequest(nick, callback);
        if (!isPending) {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'app_cache:avatars?nick=' + nick, true);
            xhr.send();
            xhr.onload = function () {
                onXhrResponse(xhr);
            }
        }
    }

    this.loadByNick = loadByNick;
    this.loadByUrl = loadByUrl;

}
