// GAME CONTROL
let myId;
let myGameId;
let currTurnId;
let currNumAllowedMoves;
let currQuarry;
let currBoard; // list of list of tiles representing the current board
let currBoardInfo = undefined; // json representation of currBoard
let myNumObelisks;
let obelisks = new Map(); // maps obelisk id to obelisk
let nightControllerId; // id of player that controls night
let allDemons = new Map(); // map from demon id to demon
let capturableDemons = new Map(); // map from capturable demon id to demon
// let choosingCaptureObelisk = false;
let captureId;  // id of demon to be captured
let capturedDemonIds = new Set();
let combos = new Map();
let reinforcedObelisk;
let currClickedTile = undefined;
let possibleObeliskIds = new Set();
let numPlayersInGame = undefined;
// let clicked = false;
let isNight = false;
let $movesLeft;
//

// SCENE
let scene = sjs.Scene({w:500, h:500});
const TILE_SIZE = 100; // size of tile in pixels
const TILE_IMAGES = ['/sprites/crystal-small.png', '/sprites/forest-small.png', '/sprites/grass-small.png', '/sprites/mountain-small.png', '/sprites/portal-small.png'];
const DEMON_IMAGES = ['/sprites/purple-monster.png', '/sprites/red-monster.png', '/sprites/yellow-monster.png'];
let bg = scene.Layer('background', { useCanvas: false });
let obelisksLayer = scene.Layer('obelisksLayer', { useCanvas: false });
let demonsLayer = scene.Layer('demonsLayer', { useCanvas: false });
//

// SPRITES
let tiles; // list of list of sprites representing tiles on board
let obeliskSpritesToObelisk = new Map(); // set of sprites containing all obelisks on board
let obeliskIdToSprite = new Map(); // maps obelisk id to its sprite
let demonIdToSprite = new Map(); // maps demon id to its sprite
//

// WEBSOCKETS
let gameConn = undefined; // the websocket connection
//

class ScreenLoc {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

const BOARD_CORNER = new ScreenLoc(100,100); // location of top left corner of board, in px

$(document).ready(()=> {
    $movesLeft = $("#movesLeftText");
    $movesLeft.append('<p>Remaining number of moves for current player: ' +currNumAllowedMoves + '</p>');
    $movesLeft.append('<p> Remaining number of obelisks: ' +myNumObelisks + '</p>');
    if (gameConn === undefined) { // first time visiting page
        setup_play_websocket();
    }

    if (currBoardInfo !== undefined) {
        drawBoard(currBoardInfo);
    }

    $('#endTurnButton').click(function() {
        if (myId === currTurnId) {
            handleEndTurn();
            if ($( "#endTurnButton" ).is( ":visible" )) {
                $("#endTurnButton").toggle("slow");
            }
            if ($( "#button_div" ).is( ":visible" )) {
                $("#button_div").toggle("slow");
            }
        }
    });

    $('#mineDemonsButton').click(function() {
        if (myId === currTurnId && checkNumAllowedMoves(getMoveCost())) {
            handleMineDemons();
        }
    });

    $('#placeObeliskButton').click(function() {
        if (myId === currTurnId && checkNumAllowedMoves(getMoveCost())) {
            if ($(".reinforceObeliskForm").is( ":visible" )) {
                $(".reinforceObeliskForm").toggle("slow");
            }
            if ($(".rotateTileForm").is( ":visible" )) {
                $(".rotateTileForm").toggle("slow");
            }
            // $(".placeObeliskForm").toggle("slow");
            handlePlaceObelisk();
        }
    });

    $('#reinforceObeliskButton').click(function() {
        if (myId === currTurnId && checkNumAllowedMoves(getMoveCost())) {
            // if ($( ".placeObeliskForm" ).is( ":visible" )) {
            //     $(".placeObeliskForm").toggle("slow");
            // }
            if ($(".rotateTileForm").is( ":visible" )) {
                $(".rotateTileForm").toggle("slow");
            }
            $(".reinforceObeliskForm").toggle("slow");
            handleReinforceObelisk();
        }
    });

    $('#moveDemonsButton').click(function() {
        if (myId === nightControllerId) { handleMoveDemon(); }
    });

    $('#captureDemonButton').click(function() {
        if (myId === nightControllerId) {
            document.addEventListener('click', captureDemonListener);
        }
    });

    $('#endNightButton').click(function() {
        if (myId === nightControllerId) { handleEndNight(); }
    });

    $('#startRotate').click(function() {
        document.addEventListener('click', tileRotateListener);
    });

    $('#endRotate').click(function() {
        if (myId === currTurnId) handleEndRotate()
    });

    $('#rotateTileButton').click(function() {
        if (myId === currTurnId && checkNumAllowedMoves(1)) {
            // if ($( ".placeObeliskForm" ).is( ":visible" )) {
            //     $(".placeObeliskForm").toggle("slow");
            // }
            if ($(".reinforceObeliskForm").is( ":visible" )) {
                $(".reinforceObeliskForm").toggle("slow");
            }
            $(".rotateTileForm").toggle("slow");
        }
    });

    $('#resourcesComboSubmit').click(function() {
        console.log("help");
        if (myId === currTurnId) {
            $('.resourcesCombosForm').toggle("slow");
            handleChooseResources();
        }
    });

    // $(document)
    //     .click(function(event) {
    //         if (myId === currTurnId && checkNumAllowedMoves()) { // only do something if it's my turn
    //             handleRotate(event);
    //         }
    //     })
        // .mousemove(function(event) {
        //     showObeliskStrengths(event);
        // });
});


function setup_play_websocket () {
    // made new websocket and assign it to 'conn'
    gameConn = new WebSocket("wss://obeliskgame.herokuapp.com/gameSocket");
    // gameConn = new WebSocket("ws://localhost:4567/gameSocket");

    gameConn.onerror = err => {
        console.log('Connection error:', err);
    };

    gameConn.onmessage = msg => {
        const data = JSON.parse(msg.data);
        switch (data.type) {
            case GAME_MESSAGE_TYPE.CONNECT:
                if (data.payload.message === 'CONNECTED') {
                    let url = window.location.href;
                    let message = {type : GAME_MESSAGE_TYPE.CONNECTED, payload : {clientURL : url}};
                    gameConn.send(JSON.stringify(message));
                }
                break;
            case GAME_MESSAGE_TYPE.BOARD:
                myId = data.payload.clientId;
                myGameId = data.payload.gameId;
                currTurnId = data.payload.firstTurn;
                currNumAllowedMoves = data.payload.numAllowedMoves;
                myNumObelisks = data.payload.obelisks;
                let playerIds = JSON.parse(data.payload.playerIds);
                let humHuman = data.payload.numHuman;
                createNavBar(playerIds, humHuman);
                adjustNavBar(currTurnId, "white");
                let quarryInfo = JSON.parse(data.payload.quarry);
                currQuarry = new Quarry(quarryInfo.reds, quarryInfo.yellows, quarryInfo.purples);
                updateQuarry();

                let boardInfo = JSON.parse(data.payload.board);
                currBoardInfo = boardInfo;
                drawBoard(boardInfo);
                if (currTurnId === myId) {
                    alert("It is your turn to begin the game!");
                } else {
                    if ($( "#endTurnButton" ).is( ":visible" )) {
                        $("#endTurnButton").toggle("slow");
                    }
                    if ($( "#button_div" ).is( ":visible" )) {
                        $("#button_div").toggle("slow");
                    }
                    $movesLeft.toggle("slow");
                }
                updateMovesLeft();
                break;
            case GAME_MESSAGE_TYPE.TURN_OVER:
                console.log("TURN OVER");
                adjustNavBar(currTurnId, "");
                currTurnId = data.payload.nextTurnId;
                adjustNavBar(currTurnId, "white");
                currNumAllowedMoves = data.payload.numAllowedMoves;
                if (currTurnId === myId) {
                    alert("It is now your turn!");
                    if ($( "#endTurnButton" ).is( ":hidden" )) {
                        $("#endTurnButton").toggle("slow");
                    }
                    if ($( "#button_div" ).is( ":hidden" )) {
                        $("#button_div").toggle("slow");
                    }
                    if ($movesLeft.is( ":hidden" )) {
                        $movesLeft.toggle("slow");
                    }
                }
                updateMovesLeft();
                break;
            case GAME_MESSAGE_TYPE.TILE_ROTATED:
                console.log("TILE_ROTATED");
                console.log(data.payload);
                if (data.payload.status === true) {
                    currNumAllowedMoves = data.payload.numAllowedMoves;
                    let rotatedTile = currBoard[data.payload.row][data.payload.col];
                    currBoard[rotatedTile.row][rotatedTile.col] = new Tile(rotatedTile.row, rotatedTile.col, rotatedTile.orientation, false, rotatedTile.type); // update tile in board
                    currClickedTile = undefined;
                    if (currTurnId !== myId) { // need to rotate the tile on the board for the other players
                        console.log("rotated tile direction: " + rotatedTile.orientation);
                        rotateTile(data.payload.row, data.payload.col, data.payload.direction, getNumTurns(rotatedTile.orientation, data.payload.direction));
                    }
                    updateMovesLeft();
                } else if (currTurnId === myId) {
                    alert("You could not rotate that tile. Please move again.");
                }
                if (myId === currTurnId) {
                    $(".rotateTileForm").toggle("slow");
                }
                break;
            case GAME_MESSAGE_TYPE.RESOURCES_MINED:
                if (data.payload.status === true) {
                    currNumAllowedMoves = data.payload.numAllowedMoves;
                    currQuarry.purples++;
                    updateQuarry();
                    updateMovesLeft();
                }
                break;
            case GAME_MESSAGE_TYPE.OBELISK_PLACED:
                console.log("OBELISK_PLACED");
                if (data.payload.status === true) {
                    if (data.payload.clientId === myId) {
                        myNumObelisks = data.payload.obelisksRemaining;
                    }
                    currNumAllowedMoves = data.payload.numAllowedMoves;
                    let obeliskInfo = JSON.parse(data.payload.obelisk);
                    console.log(obeliskInfo);
                    let placedObelisk = new Obelisk(obeliskInfo.strength, obeliskInfo.row, obeliskInfo.col, obeliskInfo.id);
                    console.log("placedObeliskId " + placedObelisk.id);
                    obelisks.set(placedObelisk.id, placedObelisk);
                    // $(".placeObeliskForm").toggle("slow");
                    placeObelisk(placedObelisk);
                    updateMovesLeft();
                    // updateObeliskSpriteDiv(placedObelisk);
                } else {
                    // TODO: this alert is being sent even though this is a valid location
                    if (currTurnId === myId) {
                        alert("You could not place an obelisk in this location. Please move again.");
                    }
                }
                break;
            case GAME_MESSAGE_TYPE.REINFORCED_WITH_OBELISK:
                if (data.payload.status === true) {
                    currNumAllowedMoves = data.payload.numAllowedMoves;
                    if (data.payload.clientId === myId) {
                        myNumObelisks = data.payload.obelisksRemaining;
                    }
                    let obeliskInfo = JSON.parse(data.payload.obelisk);
                    let newObelisk = new Obelisk(obeliskInfo.strength, obeliskInfo.row, obeliskInfo.col, obeliskInfo.id);
                    reinforceObelisk(newObelisk);
                    updateMovesLeft();
                } else if (currTurnId === myId) {
                    alert("This obleisk is already reinforced to the max. Please move again.");
                }
                break;
            case GAME_MESSAGE_TYPE.REINFORCE_COMBINATIONS:
                console.log("REINFORCE_COMBINATIONS");
                $(".resourcesCombosForm").toggle("slow");
                console.log(data.payload);
                let comboInfo = JSON.parse(data.payload.combinations);
                let count = 0;
                combos.clear();
                for (let combo of comboInfo) {
                    combos.set(count++, new Quarry(combo.reds, combo.yellows, combo.purples));
                }
                handleReinforceCombos(combos);
                break;
            case GAME_MESSAGE_TYPE.REINFORCED_WITH_RESOURCES:
                console.log("REINFORCED_WITH_RESOURCES");
                if (data.payload.status === true) {
                    currNumAllowedMoves = data.payload.numAllowedMoves;
                    let obeliskInfo = JSON.parse(data.payload.obelisk);
                    let newObelisk = new Obelisk(obeliskInfo.strength, obeliskInfo.row, obeliskInfo.col, obeliskInfo.id);
                    let quarryInfo = JSON.parse(data.payload.quarry);
                    currQuarry = new Quarry(quarryInfo.reds, quarryInfo.yellows, quarryInfo.purples);
                    updateQuarry();
                    reinforceObelisk(newObelisk);
                    updateMovesLeft();
                    if (myId === currTurnId) {
                        $('.reinforceObeliskForm').toggle("slow");
                    }
                } else if (currTurnId === myId) {
                    alert("You could not reinforce this obelisk with this set of resources. Please move again.");
                }
                break;
            case GAME_MESSAGE_TYPE.NIGHT_START:
                console.log("NIGHT_START");
                $(document).off();
                isNight = true;
                nightControllerId = data.payload.clientId;
                //choosingCaptureObelisk = false;
                adjustNavBar(currTurnId, "");
                currTurnId = 0;
                adjustNavBar(currTurnId, "white");
                capturedDemonIds.clear();

                let spawnedDemonsInfo = JSON.parse(data.payload.spawnedDemons);
                let capturableDemonsInfo = JSON.parse(data.payload.capturableDemons);
                let spawnedDemons = [];

                for (let spawnedDemon of spawnedDemonsInfo) {
                    let newDemon = new Demon(spawnedDemon.currRow, spawnedDemon.currCol, spawnedDemon.id, spawnedDemon.color);
                    spawnedDemons.push(newDemon);
                    allDemons.set(newDemon.id, newDemon);
                }

                capturableDemons.clear();
                for (let capturableDemon of capturableDemonsInfo) {
                    let newDemon = new Demon(capturableDemon.currRow, capturableDemon.currCol, capturableDemon.id, capturableDemon.color);
                    capturableDemons.set(newDemon.id, newDemon);
                }
                document.body.style.background = '#020022';
                $('.quarryHeader').css('color', 'aliceBlue');
                if (myId === nightControllerId) {
                    // $(".capturableDemons").toggle("slow");
                    $(".nightButtons").toggle("slow");
                }
                //if ($( "#movesLeftText" ).is( ":visible" )) {
                    //$("#movesLeftText").toggle("slow");
                //}
                spawnDemons(spawnedDemons);
                // populateCapturableDemonsTable();

                if (myId === nightControllerId) {
                    alert("You control the night! Spawn and capture enemies!");
                }
                break;
            case GAME_MESSAGE_TYPE.NIGHT_STEP:
                console.log("NIGHT_STEP");
                console.log(data.payload);
                let movedDemonInfo = JSON.parse(data.payload.demon);
                if (movedDemonInfo === null) { // demons done moving
                    if (myId === nightControllerId) {
                        $('.endNight').toggle("slow");
                        $('.nightButtons').toggle("slow");
                    }
                } else {
                    let movedDemon = new Demon(movedDemonInfo.currRow, movedDemonInfo.currCol, movedDemonInfo.id, movedDemonInfo.color);
                    let capturableDemonsInfo2 = JSON.parse(data.payload.capturableDemons);
                    console.log(JSON.parse(data.payload.capturableDemons));
                    capturableDemons.clear();
                    for (let capturableDemon of capturableDemonsInfo2) {
                        let newDemon = new Demon(capturableDemon.currRow, capturableDemon.currCol, capturableDemon.id, capturableDemon.color);
                        capturableDemons.set(newDemon.id, newDemon);
                    }
                    // populateCapturableDemonsTable();
                    moveDemon(movedDemon);
                    if (data.payload.status === true) { // demons done moving
                        if (myId === nightControllerId) {
                            $('.endNight').toggle("slow");
                            $('.nightButtons').toggle("slow");
                        }
                    }
                }
                break;
            case GAME_MESSAGE_TYPE.POSSIBLE_OBELISKS:
                console.log("POSSIBLE_OBELISKS");
                // choosingCaptureObelisk = true;
                let possibleObelisksInfo = JSON.parse(data.payload.obelisks);
                possibleObeliskIds.clear();
                for (let obelisk of possibleObelisksInfo) {
                    possibleObeliskIds.add(obelisk.id);
                    // TODO: show the possible obelisks to the user
                    // TODO: maybe prompt user to click obelisk and only show error if not a possible obelisk
                    // TODO: send EXECUTE_MESSAGE
                }
                document.addEventListener('click', chooseCaptureListener);
                // chooseCapture(possibleObeliskIds);
                break;
            case GAME_MESSAGE_TYPE.DEMON_CAPTURED:
                console.log("DEMON_CAPTURED");
                let obeliskUsedInfo = JSON.parse(data.payload.obelisk);
                let demonCapturedInfo = JSON.parse(data.payload.demon);
                let demonCapturedId = demonCapturedInfo.id;
                capturedDemonIds.add(demonCapturedId);
                executeCapture(obeliskUsedInfo.id, demonCapturedId);

                let capturableDemonsData = JSON.parse(data.payload.capturableDemons);
                capturableDemons.clear();
                for (let capturableDemon of capturableDemonsData) {
                    let newDemon = new Demon(capturableDemon.currRow, capturableDemon.currCol, capturableDemon.id, capturableDemon.color);
                    capturableDemons.set(newDemon.id, newDemon);
                }
                // populateCapturableDemonsTable();

                break;
            case GAME_MESSAGE_TYPE.NIGHT_OVER:
                console.log("NIGHT_OVER");
                isNight = false;
                let quarry = JSON.parse(data.payload.quarry);
                currQuarry = new Quarry(quarry.reds, quarry.yellows, quarry.purples);
                collectDemons();
                updateQuarry();
                document.body.style.background = '#fffef2';
                $('.quarryHeader').css('color', 'black');
                if ($( ".endNight" ).is( ":visible" )) {
                    $(".endNight").toggle("slow");
                }
                if ($( ".capturableDemons" ).is( ":visible" )) {
                    $(".capturableDemons").toggle("slow");
                }
                $(document).off();
                //if ($( "#movesLeftText" ).is( ":hidden" )) {
                    //$("#movesLeftText").toggle("slow");
                //}
                break;
            case GAME_MESSAGE_TYPE.GAME_OVER:
                let message = data.payload.message;
                alert(message);
                gameConn.close(1000, "Rerouting");
                window.location.replace("/obelisk");
                break;
            case GAME_MESSAGE_TYPE.AI_MOVE_BEGIN:
                let aiID = JSON.parse(data.payload.aiID);
                adjustNavBar(currTurnId, "");
                currTurnId = aiID;
                adjustNavBar(currTurnId, "white");
                alert("An AI Player is now making their move. Please wait");
                break;
            default:
                console.log('Unknown message type!', data.type);
                break;
        }
    };
};

function checkNumAllowedMoves(cost) {
    if (currNumAllowedMoves <= 0 || currNumAllowedMoves - cost < 0) {
        let message = "You don't have enough tokens left to complete this move.";
        alert(message);
        return false;
    } else {
        return true;
    }
}

/*
Cost of moves that aren't tile rotation, based on number of players in game
 */
function getMoveCost() {
  if (numPlayersInGame === 1) {
    return 3;
  } else if (numPlayersInGame === 2) {
    return 2;
  } else if (numPlayersInGame === 3) {
    return 3;
  } else {
    return 2;
  }
}

function updateMovesLeft() {
    $movesLeft.empty();
    $movesLeft.append('<h4> You have ' +currNumAllowedMoves + ' remaining move tokens</h4>');
    $movesLeft.append('<h4> You have ' +myNumObelisks + ' remaining obelisks that you can place</h4>');
}

function set_tooltip_text() {
  $('#mineDemonsButton').attr("title", "Add one purple stone to the quarry.\nCosts " + getMoveCost() + " move tokens.");
  $('#placeObeliskButton').attr("title", "Click on an empty space between tiles to place a new obelisk. Costs " + getMoveCost() + " move tokens.");
  $('#reinforceObeliskButton').attr("title", "Select an upgrade method and click on a placed obelisk to add 1 strength to it. Costs " + getMoveCost() + " move tokens.");
  $('#rotateTileButton').attr("title", "Click on a tile until desired orientation is reached, then press \"End Rotate\". Each tile can only be rotated once per game. Costs 1 move token.");
  $(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();
  });
}
