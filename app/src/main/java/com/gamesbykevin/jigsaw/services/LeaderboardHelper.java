package com.gamesbykevin.jigsaw.services;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.activity.GameActivity;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.game.Game;

/**
 * Created by Kevin on 8/29/2017.
 */
public class LeaderboardHelper {

    public static void updateLeaderboard(final GameActivity activity, final Game game) {

        //update the appropriate leader board with the appropriate duration
        activity.updateLeaderboard(getResId(game.getBoard()), activity.getTimer().getLapsed());
    }

    public static int getResId(final Board board) {

        //calculate the size of the board so we know which leader board to update
        final int size = board.getCols() * board.getRows();

        //what resource are we using?
        final int resId;

        if (Board.ROTATE) {

            //the size will determine which leader board to update
            switch (size) {

                case 9:
                    resId = R.string.leaderboard_9_pieces_rotate_on;
                    break;

                case 16:
                    resId = R.string.leaderboard_16_pieces_rotate_on;
                    break;

                case 25:
                    resId = R.string.leaderboard_25_pieces_rotate_on;
                    break;

                case 36:
                    resId = R.string.leaderboard_36_pieces_rotate_on;
                    break;

                case 49:
                    resId = R.string.leaderboard_49_pieces_rotate_on;
                    break;

                case 64:
                    resId = R.string.leaderboard_64_pieces_rotate_on;
                    break;

                case 81:
                    resId = R.string.leaderboard_81_pieces_rotate_on;
                    break;

                case 100:
                    resId = R.string.leaderboard_100_pieces_rotate_on;
                    break;

                case 121:
                    resId = R.string.leaderboard_121_pieces_rotate_on;
                    break;

                case 144:
                    resId = R.string.leaderboard_144_pieces_rotate_on;
                    break;

                case 169:
                    resId = R.string.leaderboard_169_pieces_rotate_on;
                    break;

                case 196:
                    resId = R.string.leaderboard_196_pieces_rotate_on;
                    break;

                case 225:
                    resId = R.string.leaderboard_225_pieces_rotate_on;
                    break;

                case 256:
                    resId = R.string.leaderboard_256_pieces_rotate_on;
                    break;

                default:
                    throw new RuntimeException("Size not defined: " + size);
            }

        } else {

            //the size will determine which leaderboard to update
            switch (size) {

                case 9:
                    resId = R.string.leaderboard_9_pieces_rotate_off;
                    break;

                case 16:
                    resId = R.string.leaderboard_16_pieces_rotate_off;
                    break;

                case 25:
                    resId = R.string.leaderboard_25_pieces_rotate_off;
                    break;

                case 36:
                    resId = R.string.leaderboard_36_pieces_rotate_off;
                    break;

                case 49:
                    resId = R.string.leaderboard_49_pieces_rotate_off;
                    break;

                case 64:
                    resId = R.string.leaderboard_64_pieces_rotate_off;
                    break;

                case 81:
                    resId = R.string.leaderboard_81_pieces_rotate_off;
                    break;

                case 100:
                    resId = R.string.leaderboard_100_pieces_rotate_off;
                    break;

                case 121:
                    resId = R.string.leaderboard_121_pieces_rotate_off;
                    break;

                case 144:
                    resId = R.string.leaderboard_144_pieces_rotate_off;
                    break;

                case 169:
                    resId = R.string.leaderboard_169_pieces_rotate_off;
                    break;

                case 196:
                    resId = R.string.leaderboard_196_pieces_rotate_off;
                    break;

                case 225:
                    resId = R.string.leaderboard_225_pieces_rotate_off;
                    break;

                case 256:
                    resId = R.string.leaderboard_256_pieces_rotate_off;
                    break;

                default:
                    throw new RuntimeException("Size not defined: " + size);
            }
        }

        //return our resource id for the correct leader board
        return resId;
    }
}
