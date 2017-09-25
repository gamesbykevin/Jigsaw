package com.gamesbykevin.jigsaw.services;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.game.Game;

/**
 * Created by Kevin on 8/29/2017.
 */
public class AchievementHelper {

    public static void completedGame(final BaseGameActivity activity, final Game game) {

        //unlock achievement, we completed our first puzzle
        activity.unlockAchievement(R.string.achievement_completed_a_puzzle);

        //keep incrementing the progress of these achievements
        activity.incrementAchievement(R.string.achievement_complete_50_puzzles, 1);
        activity.incrementAchievement(R.string.achievement_complete_100_puzzles, 1);
        activity.incrementAchievement(R.string.achievement_complete_500_puzzles, 1);
        activity.incrementAchievement(R.string.achievement_complete_1000_puzzles, 1);

        //calculate the size of the board
        final int size = game.getBoard().getCols() * game.getBoard().getRows();

        //if the size is not large enough, we can't unlock these
        if (size >= 100) {

            if (Board.ROTATE) {

                //if the size is big enough, unlock the achievement
                if (size >= 100)
                    activity.unlockAchievement(R.string.achievement_100_piece_rotate_on);
                if (size >= 200)
                    activity.unlockAchievement(R.string.achievement_200_piece_rotate_on);

            } else {

                //if the size is big enough, unlock the achievement
                if (size >= 100)
                    activity.unlockAchievement(R.string.achievement_100_piece);
                if (size >= 200)
                    activity.unlockAchievement(R.string.achievement_200_piece);

            }
        }
    }
}