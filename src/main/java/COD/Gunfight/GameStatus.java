package COD.Gunfight;

/**
 * Wrap status of a Gunfight session's score to enum
 */
public class GameStatus {
    private final int win, loss, streak;

    enum GAME_STATUS {
        BEGINNING,
        TIE,

        WON_OPENING_GAME,
        WON_TAKEN_LEAD,
        FIVE_OR_MORE_WIN_AHEAD,
        STANDARD_WIN,
        WON_STILL_BEHIND,

        LOST_OPENING_GAME,
        LOST_LOSING_LEAD,
        FIVE_OR_MORE_LOSS_BEHIND,
        STANDARD_LOSS,
        LOST_STILL_AHEAD
    }

    public GameStatus(int win, int loss, int streak) {
        this.win = win;
        this.loss = loss;
        this.streak = streak;
    }

    public GAME_STATUS getGameStatus() {
        if(win == 0 && loss == 0) {
            return GAME_STATUS.BEGINNING;
        }
        if(win == loss) {
            return GAME_STATUS.TIE;
        }

        if(streak >= 1) {
            if(win - loss == 1) {
                if(loss == 0) {
                    return GAME_STATUS.WON_OPENING_GAME;
                }
                else {
                    return GAME_STATUS.WON_TAKEN_LEAD;
                }
            }
            else if(win - loss >= 5) {
                return GAME_STATUS.FIVE_OR_MORE_WIN_AHEAD;
            }
            else if(win - loss > 1) {
                return GAME_STATUS.STANDARD_WIN;
            }
            else {
                return GAME_STATUS.WON_STILL_BEHIND;
            }
        }
        else {
            if(loss - win == 1) {
                if(win == 0) {
                    return GAME_STATUS.LOST_OPENING_GAME;
                }
                else {
                    return GAME_STATUS.LOST_LOSING_LEAD;
                }
            }
            else if(loss - win >= 5) {
                return GAME_STATUS.FIVE_OR_MORE_LOSS_BEHIND;
            }
            else if(loss - win > 1) {
                return GAME_STATUS.STANDARD_LOSS;
            }
            else {
                return GAME_STATUS.LOST_STILL_AHEAD;
            }
        }
    }
}