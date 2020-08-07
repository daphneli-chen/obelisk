let landingConn = undefined; // the websocket connection
let myId = undefined; // the id of the current user
let myGameId = -1; // the id of the game the client is a part of
let games = new Map();  // a map of all the existing games in the lobby
                        // maps the gameId to the game


const $createGameButton = $('#createGameButton');

class Game {
    constructor(id, numAI, numHuman, numSpotsAvailable, gameName, difficulty) {
        this.id = id;
        this.numAI = numAI;
        this.numHuman = numHuman;
        this.numSpotsAvailable = numSpotsAvailable;
        this.gameName = gameName;
        this.difficulty = difficulty;
    }
}

$(document).ready(()=> {

    if (landingConn === undefined) {
        setup_landing_websocket();
    }

    $createGameButton.click(function () {
        let numHumanPlayers = parseInt($("input[name=numHumanPlayers]").val());
        let numAIPlayers = parseInt($("input[name=numAIPlayers]").val());
        let difficulty = $("input[name=difficulty]").val();
        let gameName = $('#gameName').val();


        if (isNaN(numHumanPlayers) || isNaN(numAIPlayers) || difficulty === "" || gameName === "") {
            alert("You must fill out all fields properly to create a game!");
        } else if (!(difficulty === "EVEN_EASIER" || difficulty === "VERY_EASY" || difficulty === "EASY" ||
            difficulty === "MOSTLY_EASY" || difficulty === "NORMAL" || difficulty === "ALMOST_HARD" ||
            difficulty === "HARD" || difficulty === "VERY_HARD" || difficulty === "EVEN_HARDER")) {
            alert("You must choose a difficulty from the menu");
        } else if (numHumanPlayers <= 0) {
            alert("You must have at least one human player in the game!");
        } else if (numAIPlayers < 0) {
            alert("You cannot have a negative number of AI players!");
        } else if (numHumanPlayers + numAIPlayers > 4) {
            alert("You cannot have more than 4 players in a game");
        } else {
            let message = {type : LANDING_MESSAGE_TYPE.CREATE, payload : { numHuman : numHumanPlayers, numAI : numAIPlayers , difficulty : difficulty, name : gameName}}; // TODO: get rid of hardcoded normal
            landingConn.send(JSON.stringify(message));
        }
    });
});

function setup_landing_websocket () {
    // made new websocket and assign it to 'conn'
    landingConn = new WebSocket("wss://obeliskgame.herokuapp.com/landingSocket");
    //landingConn = new WebSocket("ws://localhost:4567/landingSocket");

    landingConn.onerror = err => {
        console.log('Connection error:', err);
    };

    landingConn.onmessage = msg => {

        const data = JSON.parse(msg.data);
        switch (data.type) {
            case LANDING_MESSAGE_TYPE.CONNECT:
                myId = data.payload.id;
                let allGames = JSON.parse(data.payload.gameList);

                for (let i = 0; i < allGames.length; i++) { // add all existing games to client's game map and render
                    let currGame = allGames[i];
                    let game = new Game(currGame.id, currGame.numAI, currGame.numHuman, currGame.numSpotsAvailable, currGame.name, currGame.difficulty);
                    games.set(currGame.id, game);
                    addGameToList(game)
                }
                break;
            case LANDING_MESSAGE_TYPE.JOINED:
                console.log("joined");
                if (data.payload.status === "joined") { // successfully joined game
                    myGameId = data.payload.id;
                    let numSpotsAvailable = data.payload.numSpotsAvailable;

                    if (games.has(myGameId)) { // game to join exists--should always happen
                        let oldGame = games.get(myGameId);
                        games.set(myGameId, new Game(oldGame.id, oldGame.numAI, oldGame.numHuman, numSpotsAvailable, oldGame.name, oldGame.difficulty)); // update numSpotsAvailable for game in gameMap
                        callWaitingPage();

                    } else {
                        console.log('ERROR: attempt to join a nonexisting game');
                    }
                }
                break;
            case LANDING_MESSAGE_TYPE.NEW_GAME:
                let gameId = JSON.parse(data.payload.gameWrapper).id;
                let numHuman = JSON.parse(data.payload.gameWrapper).numHuman;
                let numAI = JSON.parse(data.payload.gameWrapper).numAI;
                let name = JSON.parse(data.payload.gameWrapper).name;
                let difficulty = JSON.parse(data.payload.gameWrapper).difficulty;
                let newGame = new Game(gameId, numAI, numHuman, numHuman, name, difficulty);
                games.set(gameId, newGame);
                addGameToList(newGame);
                break;
            case LANDING_MESSAGE_TYPE.GAME_STARTED:
                console.log("game started");
                if (myGameId === data.payload.id) { // myGameId matches the id of the game that's started
                    landingConn.close(1000, "Rerouting");
                    window.location.replace("obelisk/" + myGameId);
                }
                games.delete(data.payload.id);
                let id = "#" + data.payload.id;
                console.log(id);
                if ($(id).length > 0) {
                    console.log("you got the right thing");
                }
                $(id).remove();
                console.log('should be gone');
                break;
            default:
                console.log('Unknown message type!', data.type);
                break;
        }
    };
}

function joinGame(gameId){
    let message = {type: LANDING_MESSAGE_TYPE.JOIN, payload : {gameId : gameId, userId : myId}};
    landingConn.send(JSON.stringify(message));
}

function callWaitingPage() {
    $(".container").toggle("slow");
    $(".waitingScreen").toggle("slow");
    let waitingScene = sjs.Scene({w: $(document).width(), h: $(document).height()});
    let waiting = waitingScene.Layer('waiting', { useCanvas: false });

    waitingScene.loadImages(['sprites/yellow-monster.png'], function() {
        let sprite = waiting.Sprite('sprites/yellow-monster.png');

        let startX = $(document).width() / 3;
        let startY = $(document).height() / 3;

        console.log(startX);
        console.log(startY);

        sprite.setX(startX);
        sprite.setY(startY);
        sprite.update();

        let offset = 100;

        let maxX = $(document).width() * (2 / 3);
        let maxY = $(document).height() * (2 / 3);

        let right = true;
        let down = false;
        let left = false;
        let up = false;

        function paint() {
            if (right) {
                sprite.setX(sprite.x + offset);
            } else if (down) {
                sprite.setY(sprite.y + offset);
            } else if (left) {
                sprite.setX(sprite.x - offset);
            } else {
                sprite.setY(sprite.y - offset);
            }
            sprite.update();

            if (sprite.x > maxX && Math.abs(sprite.y -startY) < offset) {
                right = false;
                down = true;
            }
            if (sprite.x > maxX && sprite.y > maxY) {
                down = false;
                left = true;
            }
            if (sprite.y > maxY && sprite.x < startX) {
                left = false;
                up = true;
            }
            if (Math.abs(sprite.y -startY) < offset && sprite.x < startX) {
                up = false;
                right = true;
            }
        }

        let ticker = waitingScene.Ticker(paint, {tickDuration: 500});
        ticker.run();

    });

}

/* Adds the new game to the list of games on the landing page
    INPUT: newGame - Game representing the game to be added
    OUTPUT: none
 */
function addGameToList(newGame) {
    $('.panel-group').append(
        "<div id = '" + newGame.id + "' class = 'panel panel-default'>" +
        "<div class='panel-heading'>" +
        "<h4 class='panel-title'>" +
        "<a data-toggle='collapse' data-parent='#accordion' href='#collapse" + newGame.id + "'>" +
        newGame.gameName + "</a>" +
    "</h4>" +
    "</div>" +
    "<div id='collapse" + newGame.id + "' class='panel-collapse collapse'>" +
        "<div class='panel-body'>" +
            "<div>Available Spots: " + newGame.numSpotsAvailable + "</div>" +
            "<div>Human Players: " + newGame.numHuman + "</div>" +
            "<div>AI Players: " + newGame.numAI + "</div>" +
            "<div>Difficulty: " + newGame.difficulty + "</div>" +
        "<button class='btn btn-info btn-xs' id='join" + newGame.id + "' onClick='joinGame(" + newGame.id + ")' >Join Game</button>" +
    "</div>" +
    "</div>" +
    "</div>"
    );
}

