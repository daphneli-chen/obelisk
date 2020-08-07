const TILE_TYPE = {
    PORTAL: 'PORTAL',
    GRASS: 'GRASS',
    FOREST: 'FOREST',
    CRYSTAL: 'CRYSTAL',
    MOUNTAIN: 'MOUNTAIN'
};

const TILE_ORIENTATION = {
    NORTH: 'NORTH',
    SOUTH: 'SOUTH',
    EAST: 'EAST',
    WEST: 'WEST'
};

class Tile {
    constructor (row, col, orientation, canRotate, type) {
        this.row = row;
        this.col = col;
        this.orientation = orientation;
        this.canRotate = canRotate;
        this.type = type;
    }
}

class Quarry {
    constructor(reds, yellows, purples) {
        this.reds = reds;
        this.yellows = yellows;
        this.purples = purples;
    }
}

class Obelisk {
    constructor(strength, row, col, id) {
        this.strength = strength;
        this.row = row;
        this.col = col;
        this.id = id;
    }
}

class Demon {
    constructor(row, col, id, color) {
        this.row = row;
        this.col = col;
        this.id = id;
        this.color = color;
    }
}

