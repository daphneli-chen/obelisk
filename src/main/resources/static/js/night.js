function spawnDemons(spawnedDemons) {
    let demonOffsetX = 10;
    let demonOffsetY = 10;

    scene.loadImages(DEMON_IMAGES, function () {
        // let allDemonsArr = Array.from(allDemons.values());
        // for (let i = 0; i < allDemonsArr.length; i++) {
        for (let i = 0; i < spawnedDemons.length; i++) {
            // let demon = allDemonsArr[i];
            let demon = spawnedDemons[i];
            let sprite;
            switch (demon.color){
                case "PURPLE":
                    sprite = demonsLayer.Sprite('/sprites/purple-monster.png');
                    sprite.dom.setAttribute('title', 'Strength: 1');
                    break;
                case "RED":
                    sprite = demonsLayer.Sprite('/sprites/red-monster.png');
                    sprite.dom.setAttribute('title', 'Strength: 5');
                    break;
                case "YELLOW":
                    sprite = demonsLayer.Sprite('/sprites/yellow-monster.png');
                    sprite.dom.setAttribute('title', 'Strength: 3');
                    break;
            }

            sprite.setX(demon.col * TILE_SIZE + demonOffsetX);
            sprite.setY(demon.row  * TILE_SIZE + demonOffsetY);
            sprite.dom.setAttribute('data-toggle', "tooltip");
            sprite.dom.setAttribute('class', 'demonSprite');
            sprite.update();

            set_tooltip_text();

            demonIdToSprite.set(demon.id, sprite);

            if (i === 2) { // starting second row
                demonOffsetX = 10;
                demonOffsetY = 50;
            } else { // first row
                demonOffsetX += 30;
            }
        }
    });
}

function captureDemonListener(event) {
    // document.removeEventListener('click', captureDemonListener);
    let clickLoc = new ScreenLoc(event.pageX - BOARD_CORNER.x, event.pageY - BOARD_CORNER.y);
    let clicked = false;
    if (clickLoc.x < 5 * TILE_SIZE && clickLoc.y < 5 * TILE_SIZE) { // on board
        document.removeEventListener('click', captureDemonListener);
        let clicked = false;
        for (let demonId of Array.from(demonIdToSprite.keys())) {
            let demonSprite = demonIdToSprite.get(demonId);
            if (demonSprite.isPointIn(clickLoc.x, clickLoc.y)) {
                if (capturableDemons.has(demonId)) {
                    handleCaptureDemon(demonId);
                    clicked = true;
                    break;
                } else {
                    clicked = true;
                    alert("That demon is not capturable");
                    break;
                }
            }
        }
        if (!clicked) {
            alert("You must click a demon to capture it");
        }
        // document.removeEventListener('click', captureDemonListener);
    }
}



function handleCaptureDemon(demonId){
    if (myId === nightControllerId) {
        // console.log(choosingCaptureObelisk);
        captureId = demonId;
        let message = {type : GAME_MESSAGE_TYPE.CAPTURE, payload : {gameId : myGameId, demonId : demonId }};
        gameConn.send(JSON.stringify(message));
        // document.removeEventListener('click', captureDemonListener);
        console.log("CAPTURE " + JSON.stringify(message));
    }
}

function handleMoveDemon() {
    let message = {type : GAME_MESSAGE_TYPE.EXECUTE_STEP, payload : {gameId : myGameId }};
    gameConn.send(JSON.stringify(message));
}

function moveDemon(movedDemon) {
    console.log("moveDemon");
    let oldDemon = allDemons.get(movedDemon.id);
    let sprite = demonIdToSprite.get(movedDemon.id);
    // console.log(moveddemon.col);
    // console.log(moveddemon.row);
    // let changeX = movedDemon.col - oldDemon.col;
    // let changeY = movedDemon.row - oldDemon.row;
    // sprite.setX(sprite.x + (changeX * TILE_SIZE));
    // sprite.setY(sprite.y + (changeY * TILE_SIZE));
    // sprite.update();

    let changeX = (movedDemon.col - oldDemon.col) * TILE_SIZE;
    let changeY = (movedDemon.row - oldDemon.row) * TILE_SIZE;

    let targetX = sprite.x + changeX;
    let targetY = sprite.y + changeY;

    function move() {
        if (!(sprite.x === targetX && sprite.y === targetY)) {
            sprite.setX(sprite.x + (changeX / 10));
            sprite.setY(sprite.y + (changeY / 10));
            sprite.update();
        }
    }

    let ticker = scene.Ticker(move, {tickDuration: 20});
    ticker.run();

    allDemons.set(movedDemon.id, movedDemon);
}

function handleEndNight() {
    let message = {type : GAME_MESSAGE_TYPE.END_NIGHT, payload : {gameId : myGameId }};
    gameConn.send(JSON.stringify(message));
}

function chooseCaptureListener(event) {
    let clickLoc = new ScreenLoc(event.pageX - BOARD_CORNER.x, event.pageY - BOARD_CORNER.y);
    for (let obeliskSprite of Array.from(obeliskSpritesToObelisk.keys())) {
        let currObelisk = obeliskSpritesToObelisk.get(obeliskSprite);
        let clicked = false;
        if (obeliskSprite.isPointIn(clickLoc.x, clickLoc.y)) {
            if (possibleObeliskIds.has(currObelisk.id)) { // user clicked this obelisk and it's a valid obelisk to capture
                document.removeEventListener('click', chooseCaptureListener);
                // choosingCaptureObelisk = false;
                let message = {
                    type: GAME_MESSAGE_TYPE.EXECUTE_CAPTURE,
                    payload: {gameId: myGameId, row: currObelisk.row, col: currObelisk.col, demonId: captureId}
                };
                gameConn.send(JSON.stringify(message));
                clicked = true;
                break;
            } else {
                clicked = true;
                alert("You must choose a valid obelisk");
                break;
            }
        }
    }

    document.removeEventListener('click', chooseCaptureListener);

    if (!clicked && isNight) {
        alert("You must click a obelisk to capture the demon");
    }

    // document.removeEventListener('click', chooseCaptureListener);
    // let message = "Must choose a valid obelisk";
        // alert(message);
        // choosingCaptureObelisk = false;
    // TODO: make valid obelisks into a list rather than just showing a message
}

function executeCapture(obeliskId, demonId) {
    let demonSprite = demonIdToSprite.get(demonId);
    let obeliskSprite = obeliskIdToSprite.get(obeliskId);
    demonSprite.setX(obeliskSprite.x);
    demonSprite.setY(obeliskSprite.y);
    demonSprite.update();
}

function collectDemons() {
    for (let id of capturedDemonIds) {
        let capturedDemonSprite = demonIdToSprite.get(id);
        console.log("capturedDemonSprite: " + capturedDemonSprite);
        capturedDemonSprite.remove();
    }
}
