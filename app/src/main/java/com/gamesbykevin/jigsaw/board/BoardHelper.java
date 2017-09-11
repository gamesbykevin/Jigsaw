package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;
import static com.gamesbykevin.jigsaw.board.Piece.TEXTURE_PADDING;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

/**
 * Created by Kevin on 9/4/2017.
 */
public class BoardHelper {

    //used to render the puzzle pieces
    private static Square square = null;

    //only calculate when we need to
    protected static boolean CALCULATE_UVS = true, CALCULATE_INDICES = true, CALCULATE_VERTICES = true;

    //our texture containing the puzzle pieces
    public static Bitmap PUZZLE_TEXTURE = null;

    //has the puzzle texture been generated
    public static boolean PUZZLE_TEXTURE_GENERATED = false;

    /**
     * Cleanup resources
     */
    public static void dispose() {
        square = null;
        PUZZLE_TEXTURE = null;
    }

    protected static Square getSquare() {

        //create new if null
        if (square == null)
            square = new Square();

        return square;
    }

    protected static void cut(final Board board) {

        //bitmap mask to cut the pieces
        Bitmap east = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_east);
        Bitmap west = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_west);
        Bitmap north = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_north);
        Bitmap south = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_south);

        if (PUZZLE_TEXTURE != null) {
            PUZZLE_TEXTURE.recycle();
            PUZZLE_TEXTURE = null;
        }

        //temporary store all our created bitmaps
        Bitmap[][] tmpImages = new Bitmap[board.getRows()][board.getCols()];

        //the desired size of the image
        int imageWidth;
        int imageHeight;

        final float imgSrcHeight = (float)Board.IMAGE_SOURCE.getHeight();
        final float imgSrcWidth = (float)Board.IMAGE_SOURCE.getWidth();

        //get the size ratio
        final float ratio =  imgSrcHeight / imgSrcWidth;

        if (imgSrcHeight > HEIGHT || imgSrcWidth > WIDTH) {

            if (ratio >= 1) {

                imageWidth = (int)(HEIGHT * (imgSrcWidth / imgSrcHeight));
                imageHeight = HEIGHT;

            } else {

                imageWidth = HEIGHT;
                imageHeight = (int)(HEIGHT * (imgSrcHeight / imgSrcWidth));

            }

        } else {

            imageWidth = Board.IMAGE_SOURCE.getWidth();
            imageHeight = Board.IMAGE_SOURCE.getHeight();
        }

        //make sure dimensions are an even number
        if (imageWidth % 2 != 0)
            imageWidth++;
        if (imageHeight % 2 != 0)
            imageHeight++;

        //dimensions need to be a multiple of 16
        while (imageWidth % 16 != 0) {
            imageWidth -= 2;
        }

        //dimensions need to be a multiple of 16
        while (imageHeight % 16 != 0) {
            imageHeight -= 2;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(Board.IMAGE_SOURCE, imageWidth, imageHeight, false);

        //typical size of piece, not including connectors
        final int w = imageWidth / board.getCols();
        final int h = imageHeight / board.getRows();

        //set the default size
        board.setDefaultWidth(w);
        board.setDefaultHeight(h);

        //where will the first piece be rendered
        final int startX = (WIDTH / 2) - (imageWidth / 2);
        final int startY = (HEIGHT / 2) - (imageHeight / 2);

        //now that all pieces are created, create the connectors
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //calculate the current location
                final int x = col * w;
                final int y = row * h;

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //cut our puzzle piece out of the re-sized map
                tmpImages[row][col] = piece.cutPuzzlePiece(resizedBitmap, x, y, w, h, north, south, west, east);

                //assign the coordinates
                piece.setX(startX + (col * w));
                piece.setY(startY + (row * h));

                //set the size of the piece
                piece.setWidth(tmpImages[row][col].getWidth());
                piece.setHeight(tmpImages[row][col].getHeight());

                //calculate the texture coordinates
                float tmpCol = (float)col * (1f / (float)board.getCols());
                float tmpRow = (float)row * (1f / (float)board.getRows());
                float tmpW = (1f / (float)board.getCols());
                float tmpH = (1f / (float)board.getRows());

                //make sure the texture coordinates are mapped
                piece.setTextureCoordinates(tmpCol, tmpRow, tmpW, tmpH);
            }
        }

        //create our single texture containing all puzzle pieces
        Bitmap texture = Bitmap.createBitmap(
            tmpImages[0][0].getWidth() * board.getCols(),
            tmpImages[0][0].getHeight()* board.getRows(),
            Bitmap.Config.ARGB_8888
        );

        //convert bitmap to mutable object that we will convert to texture
        PUZZLE_TEXTURE = texture.copy(Bitmap.Config.ARGB_8888, true);

        //create a canvas to render to
        Canvas canvas = new Canvas(PUZZLE_TEXTURE);

        int x = 0;
        int y = 0;

        for (int col = 0; col < tmpImages[0].length; col++) {
            for (int row = 0; row < tmpImages.length; row++) {

                //calculate so the bitmap is rendered in the center
                x = col * tmpImages[0][0].getWidth();
                y = row * tmpImages[0][0].getHeight();

                //draw the puzzle piece on the large bitmap
                canvas.drawBitmap(tmpImages[row][col], x, y, null);
            }
        }

        //flag that the texture has been generated
        PUZZLE_TEXTURE_GENERATED = true;

        //recycle bitmap image since it is no longer needed
        Board.IMAGE_SOURCE.recycle();
        Board.IMAGE_SOURCE = null;

        resizedBitmap.recycle();
        resizedBitmap = null;
    }

    /**
     * Setup the coordinates for open gl rendering
     */
    protected static void updateCoordinates(Board board) {

        for (int col = 0; col < board.getPieces()[0].length; col++) {
            for (int row = 0; row < board.getPieces().length; row++) {

                try {

                    //get the current shape
                    Piece piece = board.getPieces()[row][col];

                    if (piece == null)
                        continue;

                    //update piece coordinates
                    updatePiece(board, piece);

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        //make sure our indices are created
        board.getIndices();
    }

    protected static void updatePieceVertices(Board board, Piece piece) {

        //if rotating update vertices
        //if (piece.hasRotate())
        piece.updateVertices();

        //flag to recalculate
        CALCULATE_VERTICES = true;

        //assign vertices
        for (int i = 0; i < piece.getVertices().length; i++) {

            int index = (piece.getIndex() * 12) + i;

            if (index >= board.getVertices().length)
                return;

            board.getVertices()[index] = piece.getVertices()[i];
        }
    }

    protected static void updatePieceUvs(Board board, Piece piece) {

        //flag to recalculate
        CALCULATE_UVS = true;

        //which portion of the texture are we rendering
        for (int i = 0; i < piece.getTextureCoordinates().length; i++) {

            int index = (piece.getIndex() * 8) + i;

            if (index >= board.getUvs().length)
                return;

            board.getUvs()[index] = piece.getTextureCoordinates()[i];
        }
    }

    protected static void updatePieces(Board board, final int groupId) {

        for (int col = 0; col < board.getPieces()[0].length; col++) {
            for (int row = 0; row < board.getPieces().length; row++) {

                try {

                    //get the current shape
                    Piece tmp = board.getPieces()[row][col];

                    if (tmp == null || groupId != tmp.getGroup())
                        continue;

                    //update the piece only if they are part of the same group
                    updatePiece(board, tmp);

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }
    }

    /**
     * Update the UVS and  Vertices coordinates
     * @param board The board containing the render coordinates
     * @param piece Current desired puzzle piece we want to update
     */
    private static void updatePiece(Board board, Piece piece) {

        updatePieceVertices(board, piece);
        updatePieceUvs(board, piece);
    }

    protected static void updateGroup(Board board, final int oldGroupId, final Piece piece) {

        boolean flag = false;

        //if there is no group id to compare to, ignore this step
        if (oldGroupId > -1) {

            for (int col = 0; col < board.getPieces()[0].length; col++) {
                for (int row = 0; row < board.getPieces().length; row++) {

                    try {

                        //get the current shape
                        Piece tmp = board.getPieces()[row][col];

                        //if matching the old, update to new
                        if (tmp.getGroup() == oldGroupId) {
                            tmp.setGroup(piece.getGroup());
                            flag = true;
                        }

                    } catch (Exception e) {
                        UtilityHelper.handleException(e);
                    }
                }
            }

            //if no changes were made, don't continue
            if (!flag)
                return;
        }

        //get the size of the connectors
        final int connectorW = (int)(board.getDefaultWidth() * Piece.CONNECTOR_RATIO);
        final int connectorH = (int)(board.getDefaultHeight() * Piece.CONNECTOR_RATIO);

        for (int col = 0; col < board.getPieces()[0].length; col++) {
            for (int row = 0; row < board.getPieces().length; row++) {

                try {

                    //get the current shape
                    Piece tmp = board.getPieces()[row][col];

                    //we only want matching groups
                    if (tmp.getGroup() != piece.getGroup())
                        continue;

                    //don't check the same piece
                    if (tmp.getCol() == piece.getCol() && tmp.getRow() == piece.getRow())
                        continue;

                    //offset from the current piece
                    float offsetX = (float)(col - piece.getCol()) * (tmp.getWidth() - connectorW - connectorW - TEXTURE_PADDING);
                    float offsetY = (float)(row - piece.getRow()) * (tmp.getHeight() - connectorH - connectorH - TEXTURE_PADDING);

                    //update the position to be relative
                    tmp.setX(piece.getX() + offsetX);
                    tmp.setY(piece.getY() + offsetY);

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }
    }

    protected static void reset(final Board board) {

        //reset our flags
        board.setComplete(false);
        board.setSelection(false);

        //create new array if the size does not match
        if (board.getPieces().length != board.getRows() || board.getPieces()[0].length != board.getCols())
            board.setPieces(new Piece[board.getRows()][board.getCols()]);

        int index = 0;

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //create the piece and make sure location is correct
                if (board.getPieces()[row][col] == null) {
                    board.getPieces()[row][col] = new Piece(col, row);
                } else {
                    board.getPieces()[row][col].setCol(col);
                    board.getPieces()[row][col].setRow(row);
                }

                //each image will belong to their own group until they are combined
                board.getPieces()[row][col].setGroup(index);

                //keep track of index so we can map the open gl coordinates
                board.getPieces()[row][col].setIndex(index);

                //keep track of index
                index++;
            }
        }

        //now that all pieces are created, create the connectors
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //our neighbor piece
                Piece neighbor;

                //certain sides won't have any connectors depending on the puzzle position
                if (row == 0)
                    piece.setNorth(Piece.Connector.None);
                if (row == board.getRows() - 1)
                    piece.setSouth(Piece.Connector.None);
                if (col == 0)
                    piece.setWest(Piece.Connector.None);
                if (col == board.getCols() - 1)
                    piece.setEast(Piece.Connector.None);

                //if we aren't on the end set the connector with our neighbor
                if (col < board.getCols() - 1) {

                    //make random decision
                    boolean result = getRandomObject().nextBoolean();

                    //east neighbor
                    neighbor = board.getPieces()[row][col + 1];

                    //make sure we can connect to our neighbor
                    piece.setEast(result ? Piece.Connector.Male : Piece.Connector.Female);
                    neighbor.setWest(result ? Piece.Connector.Female : Piece.Connector.Male);
                }

                //if we aren't on the end set the connector with our neighbor
                if (row < board.getRows() - 1) {

                    //make random decision
                    boolean result = getRandomObject().nextBoolean();

                    //south neighbor
                    neighbor = board.getPieces()[row + 1][col];

                    //make sure we can connect to our neighbor
                    piece.setSouth(result ? Piece.Connector.Male : Piece.Connector.Female);
                    neighbor.setNorth(result ? Piece.Connector.Female : Piece.Connector.Male);
                }
            }
        }

        //cut the pieces
        BoardHelper.cut(board);

        //update open gl coordinates
        updateCoordinates(board);

        //we need to recalculate coordinates
        CALCULATE_UVS = true;
        CALCULATE_INDICES = true;
        CALCULATE_VERTICES = true;
    }

    protected static void placeSelected(final Board board) {

        //if nothing was selected we can't continue
        if (board.getSelected() == null)
            return;

        //were any changes made?
        boolean flag = false;

        //calculate the size of our end connectors
        final int connectorW = (int) (board.getSelected().getWidth() * Piece.CONNECTOR_RATIO);
        final int connectorH = (int) (board.getSelected().getHeight() * Piece.CONNECTOR_RATIO);

        //each piece will be the same size
        final int width = (int)(board.getSelected().getWidth() - connectorW - connectorW);
        final int height = (int)(board.getSelected().getHeight() - connectorH - connectorH);

        for (int col = 0; col < board.getPieces()[0].length; col++) {

            if (flag)
                break;

            for (int row = 0; row < board.getPieces().length; row++) {

                if (flag)
                    break;

                //only check pieces connected to the selected piece
                if (board.getPieces()[row][col].getGroup() != board.getSelected().getGroup())
                    continue;

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //check our neighbors
                Piece west = null, east = null, north = null, south = null;

                //check for our neighbors
                if (piece.getCol() < board.getCols() - 1)
                    east = board.getPieces()[(int) piece.getRow()][(int) piece.getCol() + 1];
                if (piece.getCol() > 0)
                    west = board.getPieces()[(int) piece.getRow()][(int) piece.getCol() - 1];
                if (piece.getRow() < board.getRows() - 1)
                    south = board.getPieces()[(int) piece.getRow() + 1][(int) piece.getCol()];
                if (piece.getRow() > 0)
                    north = board.getPieces()[(int) piece.getRow() - 1][(int) piece.getCol()];

                //if the piece exists and not already part of the same group, check if we can connect
                if (!flag && east != null && piece.getGroup() != east.getGroup()) {

                    //make sure the pieces are close enough
                    int minX = (int) (piece.getX() + width);
                    int maxX = (int) (piece.getX() + width + connectorW + connectorW);
                    int minY = (int) (piece.getY() - connectorH);
                    int maxY = (int) (piece.getY() + connectorH);

                    //now make sure the piece is on the correct side
                    if (east.getX() > minX && east.getX() < maxX) {
                        if (east.getY() > minY && east.getY() < maxY) {

                            //make group match as well as all pieces currently connected to the current piece
                            updateGroup(board, east.getGroup(), piece);

                            //flag change made
                            flag = true;
                        }
                    }
                }

                //if the piece exists and not already part of the same group, check if we can connect
                if (!flag && west != null && piece.getGroup() != west.getGroup()) {

                    //make sure the pieces are close enough
                    int minX = (int) (piece.getX() - width - connectorW - connectorW);
                    int maxX = (int) (piece.getX() - width);
                    int minY = (int) (piece.getY() - connectorH);
                    int maxY = (int) (piece.getY() + connectorH);

                    //now make sure the piece is on the correct side
                    if (west.getX() > minX && west.getX() < maxX) {
                        if (west.getY() > minY && west.getY() < maxY) {

                            //make group match as well as all pieces currently connected to the current piece
                            updateGroup(board, west.getGroup(), piece);

                            //flag change made
                            flag = true;
                        }
                    }
                }

                //if the piece exists and not already part of the same group, check if we can connect
                if (!flag && south != null && piece.getGroup() != south.getGroup()) {

                    //make sure the pieces are close enough
                    int minX = (int) (piece.getX() - connectorW);
                    int maxX = (int) (piece.getX() + connectorW);
                    int minY = (int) (piece.getY() + height);
                    int maxY = (int) (piece.getY() + height + connectorH + connectorH);

                    //now make sure the piece is on the correct side
                    if (south.getX() > minX && south.getX() < maxX) {
                        if (south.getY() > minY && south.getY() < maxY) {

                            //make group match as well as all pieces currently connected to the current piece
                            updateGroup(board, south.getGroup(), piece);

                            //flag change made
                            flag = true;
                        }
                    }
                }

                //if the piece exists and not already part of the same group, check if we can connect
                if (!flag && north != null && piece.getGroup() != north.getGroup()) {

                    //make sure the pieces are close enough
                    int minX = (int) (piece.getX() - connectorW);
                    int maxX = (int) (piece.getX() + connectorW);
                    int minY = (int) (piece.getY() - height - connectorH - connectorH);
                    int maxY = (int) (piece.getY() - height);

                    //now make sure the piece is on the correct side
                    if (north.getX() > minX && north.getX() < maxX) {
                        if (north.getY() > minY && north.getY() < maxY) {

                            //make group match as well as all pieces currently connected to the current piece
                            updateGroup(board, north.getGroup(), piece);

                            //flag change made
                            flag = true;
                        }
                    }
                }
            }
        }

        //only update if changes were made
        if (flag)
            updatePieces(board, board.getSelected().getGroup());
    }
}