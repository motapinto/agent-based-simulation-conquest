package data;

import java.io.Serializable;

public enum MessageType implements Serializable {
    /** Server to Player / Zone Messages */
    START,
    GAME_OVER,

    /** Player to Zone Messages */
    MOVING,

    /** Player to Server Messages */
    KILLED,

    /** Zone to Serve / Player Messages */
    CAPTURED,
    NEUTRAL,

    /** Zone to Player Messages */
    ZONE_POINTS,
    POSITION,
    PLAYERS_IN_ZONE,

    /** Player to Player Messages */
    INFORM_HEALTH,
    ATTACK,
    HEAL,
    BACKUP
}
