const GAME_MESSAGE_TYPE = {
    CONNECT: 0,
    CONNECTED: 1,
    BOARD: 2,
    END_TURN: 3,
    TURN_OVER: 4,
    ROTATE_TILE: 5,// received when user rotates tile
    TILE_ROTATED: 6, // sent after tile rotated
    MINE_RESOURCES: 7, // received when user mines
    RESOURCES_MINED: 8, // sent after user mines
    PLACE_OBELISK: 9, // received when user places one
    OBELISK_PLACED: 10, // sent after uses places one
    REINFORCE_WITH_OBELISK: 11, // received when user upgrades with obelisk
    REINFORCE_WITH_RESOURCES: 12, // received when user upgrades with resources
    REINFORCED_WITH_OBELISK: 13, // sent after user upgrades with obelisk
    REINFORCED_WITH_RESOURCES: 14, // sent after user upgrades with resources
    NIGHT_START: 15,
    NIGHT_STEP: 16,
    POSSIBLE_OBELISKS: 17,
    GAME_OVER: 18,
    EXECUTE_STEP: 19,
    END_NIGHT: 20,
    CAPTURE: 21,
    NIGHT_OVER: 22,
    DEMON_CAPTURED: 23,
    EXECUTE_CAPTURE: 24,
    AI_MOVE_BEGIN: 25,
    GET_REINFORCE_COMBINATIONS: 26,
    REINFORCE_COMBINATIONS : 27
};

const LANDING_MESSAGE_TYPE = {
    CONNECT: 0,
    CREATE: 1,
    JOIN: 2,
    JOINED: 3,
    NEW_GAME: 4,
    GAME_STARTED: 5
};


