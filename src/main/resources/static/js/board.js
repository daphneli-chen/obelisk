function drawBoard(boardInfo) {
    scene.loadImages(TILE_IMAGES, function () {
        // loop through every tile of board
        let tileCols = [];
        let spriteCols = [];
        for (let r = 0; r < boardInfo.length; r++) {
            let tileRow = [];
            let spriteRow = [];
            for (let c = 0; c < boardInfo[r].length; c++) {
                let currTile = boardInfo[r][c];
                let newTile  = new Tile(currTile.row, currTile.col, currTile.pathDir, currTile.canRotate, currTile.type);
                // TODO: might have trouble grabbing enemies because it's a class we created
                let sprite;

                switch (newTile.type) {
                    case TILE_TYPE.PORTAL:
                        sprite = bg.Sprite("/sprites/portal-small.png");
                        break;
                    case TILE_TYPE.GRASS:
                        sprite = bg.Sprite("/sprites/grass-small.png");
                        break;
                    case TILE_TYPE.FOREST:
                        sprite = bg.Sprite("/sprites/forest-small.png");
                        break;
                    case TILE_TYPE.CRYSTAL:
                        sprite = bg.Sprite("/sprites/crystal-small.png");
                        break;
                    case TILE_TYPE.MOUNTAIN:
                        sprite = bg.Sprite("/sprites/mountain-small.png");
                        newTile  = new Tile(currTile.row, currTile.col, undefined, false, currTile.type);
                        break;
                }

                tileRow.push(newTile);

                sprite.rotate(getRadQuarterRot(newTile.orientation));
                sprite.update();

                sprite.setX(c * TILE_SIZE);
                sprite.setY(r * TILE_SIZE);
                sprite.update();

                spriteRow.push(sprite);

            }
            tileCols.push(tileRow);
            spriteCols.push(spriteRow);
        }
        currBoard = tileCols;
        tiles = spriteCols;
    });
}


function tileRotateListener(event) {
    let clickLoc = new ScreenLoc(event.pageX - BOARD_CORNER.x, event.pageY - BOARD_CORNER.y);
    console.log("clickLoc: " + clickLoc.x + "," + clickLoc.y);
    if (clickLoc.x < 5 * TILE_SIZE && clickLoc.y < 5 * TILE_SIZE) { // on board
        let clickedRow = Math.floor(clickLoc.y / 100);
        let clickedCol = Math.floor(clickLoc.x / 100);
        let clickedTile = currBoard[clickedRow][clickedCol];
        if (clickedTile.canRotate) {
            if (currClickedTile === undefined) {
                currClickedTile = clickedTile;
            }
            if (clickedTile.row === currClickedTile.row && clickedTile.col === currClickedTile.col) {
                currClickedTile = clickedTile;
                rotateTile(clickedTile.row, clickedTile.col, calcRotatedDirection(clickedTile.orientation, 1), 1);
            } else { // user clicked on different tile
                alert("You can only rotate one tile at a time.");
            }
        } else {
            document.removeEventListener('click', tileRotateListener);
            alert("That tile has already been rotated. Please try again.");
            $(".rotateTileForm").toggle("slow");
        }
    }
}

// direction input is the direction that the tile will be facing after rotating one turn
function rotateTile(row, col, direction, numTurns) {
    console.log("rotating tile");
    let clickedSprite = tiles[row][col];
    let clickedTile = currBoard[row][col];
    // clickedSprite.rotate(Math.PI / 2);
    clickedSprite.rotate((Math.PI / 2) * numTurns);
    clickedSprite.update();
    currBoard[row][col] = new Tile(clickedTile.row, clickedTile.col, direction.toString(), true, clickedTile.type); // update tile in board
}

// function getDirectionRad(dir) {
//     switch (dir) {
//         case TILE_ORIENTATION.NORTH:
//             return Math.PI / 2;
//         case TILE_ORIENTATION.SOUTH:
//             return 3 * Math.PI / 2;
//         case TILE_ORIENTATION.EAST:
//             return 0;
//         case TILE_ORIENTATION.WEST:
//             return Math.PI;
//     }
// }

function getRadQuarterRot(tileOrientation) {
    switch (tileOrientation) {
        case TILE_ORIENTATION.NORTH:
            return Math.PI;
        case TILE_ORIENTATION.SOUTH:
            return 0;
        case TILE_ORIENTATION.EAST:
            return 3 * Math.PI / 2;
        case TILE_ORIENTATION.WEST:
            return Math.PI / 2;
    }
}

let directionToIntMap = new Map();
directionToIntMap.set(TILE_ORIENTATION.NORTH, 0);
directionToIntMap.set(TILE_ORIENTATION.EAST, 1);
directionToIntMap.set(TILE_ORIENTATION.SOUTH, 2);
directionToIntMap.set(TILE_ORIENTATION.WEST, 3);

let intToDirectionMap = new Map();
intToDirectionMap.set(0, TILE_ORIENTATION.NORTH);
intToDirectionMap.set(1, TILE_ORIENTATION.EAST);
intToDirectionMap.set(2, TILE_ORIENTATION.SOUTH);
intToDirectionMap.set(3, TILE_ORIENTATION.WEST);

function getNumTurns(currDirection, endDirection) {
    let currDirectionInt = directionToIntMap.get(currDirection);
    let endDirectionInt = directionToIntMap.get(endDirection);
    return (endDirectionInt < currDirectionInt) ?
        (endDirectionInt + 4) - currDirectionInt :
        (endDirectionInt - currDirectionInt);
}

function calcRotatedDirection(currDirection, spins) {
    let currDirectionInt = directionToIntMap.get(currDirection);
    let newDirectionInt = parseInt(currDirectionInt) + spins;
    return intToDirectionMap.get(newDirectionInt % 4);
}

function createNavBar(playerIds, numAI) {
    numPlayersInGame = playerIds.length + numAI;
    for (let i = 0; i < playerIds.length; i++) {
        let entry = document.createElement("p");
        entry.className = "navbar-text";
        entry.id = playerIds[i];
        entry.innerHTML = "Player " + (i + 1);
        document.getElementById('navigation-bar').appendChild(entry);
    }
    for (let j = 0; j < numAI; j++) {
        let entry = document.createElement("p");
        entry.className = "navbar-text";
        entry.id = -j - 1;
        entry.innerHTML = "AI " + (j + 1);
        document.getElementById('navigation-bar').appendChild(entry);
    }
    let entry = document.createElement("p");
    entry.className = "navbar-text";
    entry.id = 0;
    entry.innerHTML = "Night";
    document.getElementById('navigation-bar').appendChild(entry);
    set_tooltip_text();
}


function adjustNavBar(turnId, color) {
    $('#navigation-bar').children('p').each(function () {
        if (parseInt(this.id) === turnId) {
            this.style.color = color;
        }
    });
}
