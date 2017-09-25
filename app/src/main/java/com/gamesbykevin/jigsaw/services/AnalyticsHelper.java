package com.gamesbykevin.jigsaw.services;

import android.os.Bundle;

import com.gamesbykevin.jigsaw.activity.GameActivity;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.game.Game;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 9/24/2017.
 */

public class AnalyticsHelper {

    private static final String KEY_PUZZLE_STARTED = "puzzle_started";

    private static final String KEY_PUZZLE_COMPLETED = "puzzle_completed";

    private static final String KEY_PUZZLE_DURATION = "puzzle_duration";

    private static final String KEY_PUZZLE_SIZE = "puzzle_size";

    private static final String KEY_PUZZLE_ROTATE = "puzzle_rotate";

    public static void trackPuzzleStarted(final GameActivity activity, final Game game) {

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle started START");

        //create new bundle
        Bundle params = new Bundle();

        //calculate the size of the puzzle
        final int size = game.getBoard().getCols() * game.getBoard().getRows();

        //add the parameter
        params.putInt(KEY_PUZZLE_SIZE, size);

        //add the parameter
        params.putBoolean(KEY_PUZZLE_ROTATE, Board.ROTATE);

        //log the event
        activity.getFirebaseAnalytics().logEvent(KEY_PUZZLE_STARTED, params);

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle started END");
    }

    public static void trackPuzzleCompleted(final GameActivity activity, final Game game) {

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle completed START");

        //create new bundle
        Bundle params = new Bundle();

        //calculate the size of the puzzle
        final int size = game.getBoard().getCols() * game.getBoard().getRows();

        //add the puzzle size as a parameter
        params.putInt(KEY_PUZZLE_SIZE, size);

        //add the parameter
        params.putBoolean(KEY_PUZZLE_ROTATE, Board.ROTATE);

        //track the duration
        params.putLong(KEY_PUZZLE_DURATION, activity.getTimer().getLapsed());

        //log the event
        activity.getFirebaseAnalytics().logEvent(KEY_PUZZLE_COMPLETED, params);

        if (DEBUG)
            UtilityHelper.logEvent("Analytics puzzle completed END");
    }
}