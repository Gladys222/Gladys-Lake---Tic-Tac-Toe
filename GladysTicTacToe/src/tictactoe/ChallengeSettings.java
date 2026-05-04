package tictactoe;

/**
 * ChallengeSettings — a simple data holder for which challenge
 * modifiers the player has turned on before a challenge game starts.
 *
 *  C1 – Time Pressure  : each player has only 3 seconds to place their piece
 *  C2 – Piece Stealer  : instead of placing, you may move ANY opponent piece blank
 *  C3 – Vanishing Act  : 1–2 random pieces disappear mid-game (never hands a win)
 *  C4 – Big Board      : play on 4×4 (need 4-in-a-row) or 5×5 (need 4-in-a-row)
 */
public class ChallengeSettings
{
    public boolean timePressure  = false;   // C1
    public boolean pieceStealer = false;   // C2
    public boolean vanishingAct = false;   // C3
    public boolean bigBoard      = false;   // C4
    public int     boardSize     = 3;       // 3, 4, or 5  (only relevant if bigBoard)
    public int     winLength     = 3;       // cells in a row needed to win

    /** True if any modifier is on. */
    public boolean anyActive()
    {
        return timePressure || pieceStealer || vanishingAct || bigBoard;
    }

    /** Human-readable summary shown in the status bar */
    public String summary()
    {
        StringBuilder sb = new StringBuilder("Challenges: ");
        if (timePressure)  sb.append("[⏱ 3s] ");
        if (pieceStealer)  sb.append("[🎯 Steal] ");
        if (vanishingAct)  sb.append("[💨 Vanish] ");
        if (bigBoard)      sb.append("[📐 ").append(boardSize).append("×").append(boardSize).append("] ");
        if (!anyActive())  sb.append("none");
        return sb.toString().trim();
    }
}