function updateQuarry() {
    document.getElementById("redDemonCount").innerHTML = currQuarry.reds;
    document.getElementById("yellowDemonCount").innerHTML = currQuarry.yellows;
    document.getElementById("purpleDemonCount").innerHTML = currQuarry.purples;
}

function handleEndTurn() {
    // if ($( ".placeObeliskForm" ).is( ":visible" )) {
    //     $(".placeObeliskForm").toggle("slow");
    // }
    if ($(".reinforceObeliskForm").is( ":visible" )) {
        $(".reinforceObeliskForm").toggle("slow");
    }
    if ($(".rotateTileForm").is( ":visible" )) {
        $(".rotateTileForm").toggle("slow");
    }
    if ($movesLeft.is( ":visible" )) {
        $movesLeft.toggle("slow");
    }
    let message = {type : GAME_MESSAGE_TYPE.END_TURN, payload : {gameId : myGameId }};
    gameConn.send(JSON.stringify(message));
}

function handleMineDemons() {
    let message = {type : GAME_MESSAGE_TYPE.MINE_RESOURCES, payload : {clientId : myId, numAllowedMoves: currNumAllowedMoves}};
    gameConn.send(JSON.stringify(message));
}

function handlePlaceObelisk() {
    $(document).unbind('click').click(function() {
        document.addEventListener('click', placeObeliskListener);
    });
}

function placeObeliskListener(event) {
    let clickLoc = new ScreenLoc(event.pageX - BOARD_CORNER.x, event.pageY - BOARD_CORNER.y);
    if (clickLoc.x < 5 * TILE_SIZE && clickLoc.y < 5 * TILE_SIZE) { // on board
        let clickedRow = Math.floor(clickLoc.y / 100);
        let clickedCol = Math.floor(clickLoc.x / 100);
        let clickedTileSprite = tiles[clickedRow][clickedCol];
        let tileMidX = clickedTileSprite.x + (TILE_SIZE / 2);
        let tileMidY = clickedTileSprite.y + (TILE_SIZE / 2);

        let placedRow, placedCol;
        if (clickLoc.x < tileMidX) { // left half of tile
            placedCol = clickedCol;
            if (clickLoc.y < tileMidY) { // top left corner of tile
                placedRow = clickedRow;
            } else { // bottom left corner of tile
                placedRow = clickedRow + 1;
            }
        } else { // right half of tile
            if (clickLoc.y < tileMidY) { // top right corner of tile
                placedRow = clickedRow;
                placedCol = clickedCol + 1;
            } else { // bottom right corner of tile
                placedRow = clickedRow + 1;
                placedCol = clickedCol + 1;
            }
        }
        let message = {type : GAME_MESSAGE_TYPE.PLACE_OBELISK, payload : {numAllowedMoves: currNumAllowedMoves, row : placedRow, col : placedCol, clientId : myId}};
        gameConn.send(JSON.stringify(message));
        $(document).off();
    }
    document.removeEventListener('click', placeObeliskListener);
}


function placeObelisk(obelisk) {
    scene.loadImages(['/sprites/obelisk.png'], function () {
        let sprite = obelisksLayer.Sprite('/sprites/obelisk.png');
        sprite.setX((obelisk.col) * TILE_SIZE - 18);
        sprite.setY((obelisk.row) * TILE_SIZE - 50);
        sprite.update();
        obeliskSpritesToObelisk.set(sprite, obelisk);
        obeliskIdToSprite.set(obelisk.id, sprite);

        sprite.dom.setAttribute('data-toggle', "tooltip");
        sprite.dom.setAttribute('title', 'Strength: 1');
        set_tooltip_text();
    });

    // // assign each obelisk html element an id
    // for (let obeliskHTML of $('#sjs0-obelisksLayer').children()) {
    //     let att = document.createAttribute("id");       // Create a "class" attribute
    //     att.value = "obelisk" + ;                           // Set the value of the class attribute
    //     h1.setAttributeNode(att);
    // }
}

let canReinforce = false;

function reinforceListen(event) {
    console.log(canReinforce);
    if (canReinforce) {
        let clickLoc = new ScreenLoc(event.pageX - BOARD_CORNER.x, event.pageY - BOARD_CORNER.y);
        console.log("obeliskSprites " + Array.from(obeliskSpritesToObelisk.keys()));
        for (let obeliskSprite of Array.from(obeliskSpritesToObelisk.keys())) {
            if (obeliskSprite.isPointIn(clickLoc.x, clickLoc.y)) { // user clicked this obelisk
                let reinforceType = document.querySelector('input[name=reinforceObeliskType]:checked').value;
                let obelisk = obeliskSpritesToObelisk.get(obeliskSprite);
                reinforcedObelisk = obelisk;
                let message;

                console.log("reinforce type " + reinforceType);

                if (reinforceType === "obelisk") {
                    //document.removeEventListener('click');
                    canReinforce = false;
                    message = {
                        type: GAME_MESSAGE_TYPE.REINFORCE_WITH_OBELISK,
                        payload: {clientId: myId, row: obelisk.row, col: obelisk.col, numAllowedMoves: currNumAllowedMoves}
                    };
                    console.log(message);
                    gameConn.send(JSON.stringify(message));
                    $(document).off();
                    $(".reinforceObeliskForm").toggle("slow");
                } else if (reinforceType === "resources") {
                    message = {
                        type: GAME_MESSAGE_TYPE.GET_REINFORCE_COMBINATIONS,
                        payload: {
                            clientId: myId,
                            red: currQuarry.reds,
                            yellow: currQuarry.yellows,
                            purple: currQuarry.purples,
                        }
                    };
                    gameConn.send(JSON.stringify(message));
                    // $(".reinforceObeliskForm").toggle("slow");
                    $(document).off();
                    console.log("GET_REINFORCE_COMBINATIONS" + JSON.stringify(message));
                } else {
                    console.log("ERROR: Must choose reinforce type");
                }
            }
        }
    } else {
        document.removeEventListener('click', reinforceListen);
    }
}

let once = {
    once : true
};

function handleReinforceObelisk() {
    // document.getElementById("reinforceWithObeliskButton").disabled = true;
    if (myNumObelisks <= 0) {
        $('#reinforceWithObeliskButton').popover();
    }
    // if (!choosingCaptureObelisk) {
        $(document).off();
        canReinforce = true;
        document.addEventListener('click', reinforceListen);
    // }
}

function handleReinforceCombos(combos) {
    console.log("handleReinforceCombos");
    console.log(combos);
    let combosArr = Array.from(combos.keys());
    $('#resourcesComboForm').empty();
    for (let comboId of combosArr) {
        let currCombo = combos.get(comboId);
        populateResourcesCombos(currCombo.reds, currCombo.yellows, currCombo.purples, comboId);
    }
}

function populateResourcesCombos(numRed, numYellow, numPurple, id) {
    let i = document.createElement("input");
    i.className = "form-check-input";
    i.type = "radio";
    i.name = "combo";
    i.value = id;
    document.getElementById('resourcesComboForm').appendChild(i); // TODO: see if this works if changed to jquery

    let j = document.createElement("label");
    j.className = "form-check-label";
    j.for = "combo" + id;
    j.innerHTML = " Red: " + numRed + ", Yellow: " + numYellow + ", Purple: " + numPurple;
    let b = document.createElement('br');
    document.getElementById('resourcesComboForm').appendChild(j);
    document.getElementById('resourcesComboForm').appendChild(b);
}

function handleChooseResources() {
    console.log("handle choose resources");
    let chosen = document.querySelector('input[name=combo]:checked');
    if (chosen == null) {
        alert("Must choose a valid resource combo to reinforce obelisk");
    } else {
        let comboId = parseInt(chosen.value);
        // let comboId = parseInt($('input[name=combo]').value);
        console.log("comboId: " + comboId);
        let comboChosen = combos.get(comboId);
        console.log("combo chosen: " + comboChosen.reds + comboChosen.yellows + comboChosen.purples);
        let message = {type : GAME_MESSAGE_TYPE.REINFORCE_WITH_RESOURCES, payload : {clientId : myId, row : reinforcedObelisk.row, col: reinforcedObelisk.col, red: comboChosen.reds, yellow: comboChosen.yellows, purple: comboChosen.purples, numAllowedMoves: currNumAllowedMoves}};
        gameConn.send(JSON.stringify(message));
        console.log("REINFORCE_WITH_RESOURCES: " + JSON.stringify(message));
    }
}

function reinforceObelisk(obelisk) {
    obelisks.set(obelisk.id, obelisk);
    let obeliskSprite = obeliskIdToSprite.get(obelisk.id);
    obeliskSprite.setXScale(1.05 * obeliskSprite.xscale);
    obeliskSprite.setYScale(1.05 * obeliskSprite.yscale);
    obeliskSprite.update();
    obeliskSprite.dom.setAttribute('data-original-title', 'Strength: ' + obelisk.strength);
    set_tooltip_text();
}

function handleEndRotate() {
    let message = {
        type: GAME_MESSAGE_TYPE.ROTATE_TILE,
        payload: {
            clientId: myId,
            row: currClickedTile.row,
            col: currClickedTile.col,
            direction: calcRotatedDirection(currClickedTile.orientation, 1),
            numAllowedMoves: currNumAllowedMoves
        }
    };
    document.removeEventListener('click', tileRotateListener);
    gameConn.send(JSON.stringify(message));
    console.log("ROTATE_TILE: " + JSON.stringify(message));
}

function showObeliskStrengths(event) {
    // TODO:
}

function updateObeliskSpriteDiv(placedObelisk) {
    console.log("id: " + placedObelisk.id);
    $('#sjs0-obelisksLayer').lastChild.id = "help";
  $movesLeft.dom.setAttribute('id', 'test');
    // let obelisksLayer = document.getElementById('sjs0-obelisksLayer');
    // let lastObeliskSprite = obelisksLayer.lastChild;
    // lastObeliskSprite.id = "obelisk" + placedObelisk.id;
    // let lastObeliskDiv = $('#sjs0-obelisksLayer').children().last();
    // console.log($('#sjs0-obelisksLayer').children().last().attr('style'));
    // $('#sjs0-obelisksLayer div:last').attr('id', 'obelisk0');
    // console.log($('#obelisk0').attr('style'));
    // lastObeliskDiv.attr('id', "obelisk" + placedObelisk.id);
}

