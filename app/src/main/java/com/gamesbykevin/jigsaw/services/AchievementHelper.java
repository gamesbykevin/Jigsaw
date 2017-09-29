package com.gamesbykevin.jigsaw.services;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.game.Game;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

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

        //if the size is large enough, unlock these achievements
        if (size >= 100) {

            activity.unlockAchievement(R.string.achievement_100_piece);

            if (Board.ROTATE)
                activity.unlockAchievement(R.string.achievement_100_piece_rotate_on);
        }

        //if the size is large enough, unlock these achievements
        if (size >= 200) {

            activity.unlockAchievement(R.string.achievement_200_piece);

            if (Board.ROTATE)
                activity.unlockAchievement(R.string.achievement_200_piece_rotate_on);
        }

        //get the list of completed levels
        ArrayList<Integer> indexes = (ArrayList<Integer>)activity.getObjectValue(R.string.completed_puzzle_index_key, new TypeToken<ArrayList<Integer>>(){}.getType());

        //if we completed all puzzles, unlock achievement
        if (indexes.size() >= 75)
            activity.unlockAchievement(R.string.achievement_complete_all_puzzles);
    }
}