package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;
import static com.gamesbykevin.jigsaw.board.Piece.CONNECTOR_RATIO;
import static com.gamesbykevin.jigsaw.board.Piece.TEXTURE_PADDING;
import static com.gamesbykevin.jigsaw.game.GameHelper.getEntityPlaceBorder;
import static com.gamesbykevin.jigsaw.game.GameHelper.getSquarePlaceBorder;
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

    //how much to trim the final image size
    private static float TRIM_RATIO = .9f;

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

        //now that we have maintained aspect ratio take a % off the width and height
        imageWidth  *= TRIM_RATIO;
        imageHeight *= TRIM_RATIO;

        //make sure dimensions are an even number
        if (imageWidth % 2 != 0)
            imageWidth++;
        if (imageHeight % 2 != 0)
            imageHeight++;

        //dimensions need to be a multiple of x
        while (imageWidth % 16 != 0) {
            imageWidth -= 2;
        }

        //dimensions need to be a multiple of x
        while (imageHeight % 16 != 0) {
            imageHeight -= 2;
        }

        //create our new image for us to cut with the new dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(Board.IMAGE_SOURCE, imageWidth, imageHeight, false);

        //typical size of piece, not including connectors
        int w = imageWidth / board.getCols();
        int h = imageHeight / board.getRows();

        //make sure the size of each image is even to make our math perfect
        if (w % 2 != 0)
            w--;
        if (h % 2 != 0)
            h--;

        //set the default size
        board.setDefaultWidth(w);
        board.setDefaultHeight(h);

        //get the size of a connector so we can correct our starting coordinates
        final int connectorW = (int)(w * CONNECTOR_RATIO);
        final int connectorH = (int)(h * CONNECTOR_RATIO);

        //where will the first piece be rendered
        final int startX = (WIDTH / 2) - (imageWidth / 2) - connectorW - (TEXTURE_PADDING / 2);
        final int startY = (HEIGHT / 2) - (imageHeight / 2) - connectorH - (TEXTURE_PADDING / 2);

        //setup where our place border will be rendered
        getEntityPlaceBorder().setX((WIDTH / 2) - (imageWidth / 2));
        getEntityPlaceBorder().setY((HEIGHT / 2) - (imageHeight / 2));
        getEntityPlaceBorder().setWidth(board.getCols() * w);
        getEntityPlaceBorder().setHeight(board.getRows() * h);
        getSquarePlaceBorder().setupVertices(getEntityPlaceBorder().getVertices());

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

                //assign the destination coordinates
                piece.setDestinationX((int)piece.getX());
                piece.setDestinationY((int)piece.getY());

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

        //create a list of possible coordinates to place the puzzle pieces
        List<PointF> coordinates = new ArrayList<>();

        final int tmpW = tmpImages[0][0].getWidth() / 3;
        final int tmpH = tmpImages[0][0].getHeight() / 3;

        //locate all our possible locations to place the puzzle pieces
        for (int x = -connectorW; x < WIDTH - connectorW - connectorW - tmpW; x += tmpW) {
            for (int y = -connectorH; y < HEIGHT - connectorH - connectorH - tmpH; y += tmpH) {

                //don't use the coordinates that cover the placement border
                if (x >= getEntityPlaceBorder().getX() && x <= getEntityPlaceBorder().getX() + getEntityPlaceBorder().getWidth() &&
                    y >= getEntityPlaceBorder().getY() && y <= getEntityPlaceBorder().getY() + getEntityPlaceBorder().getHeight())
                    continue;

                //add this location to our list
                coordinates.add(new PointF(x, y));
            }
        }

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //pick a random location from our coordinate list
                final int index = getRandomObject().nextInt(coordinates.size());

                //set where the pieces will start
                piece.setStartX((int)coordinates.get(index).x);
                piece.setStartY((int)coordinates.get(index).y);

                //remove location so we don't pick it again, as long as there are more options
                if (coordinates.size() > 1)
                    coordinates.remove(index);
            }
        }

        //remove list
        coordinates.clear();
        coordinates = null;

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

        //select the first piece
        board.setSelected(board.getPieces()[0][0]);

        //order the group
        orderGroup(board);

        //update the coordinates
        updateCoordinates(board);

        //de-select the piece
        board.setSelected(null);
    }

    protected static Piece getIndexPiece(Board board, int index) {

        //check each piece
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
                    Piece piece = board.getPieces()[row][col];

                    if (piece.getIndex() == index)
                        return piece;

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        //no piece found, return null
        return null;
    }

    protected static void orderGroup(Board board) {

        //have to have a selected piece
        if (board.getSelected() == null)
            return;

        //we want to order the group last so they are displayed on top
        int indexNotPlaced = (board.getCols() * board.getRows()) - 1;

        //check each piece
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
                    Piece piece = board.getPieces()[row][col];

                    //if the group matches we need to re-arrange
                    if (piece.getGroup() == board.getSelected().getGroup()) {

                        //update the other piece index
                        getIndexPiece(board, indexNotPlaced).setIndex(piece.getIndex());

                        //set the new index so this piece is rendered on top
                        piece.setIndex(indexNotPlaced);

                        //assign the next position
                        indexNotPlaced--;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected static void orderPlaced(Board board) {

        int indexPlaced = 0;
        int indexNotPlaced = (board.getCols() * board.getRows()) - 1;

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
                    Piece piece = board.getPieces()[row][col];

                    //if this piece isn't placed yet it is a good swap
                    if (!piece.isPlaced()) {

                        //set index at the end
                        piece.setIndex(indexNotPlaced);

                        //decrease index
                        indexNotPlaced--;

                    } else {

                        //set index at the beginning
                        piece.setIndex(indexPlaced);

                        //increase index
                        indexPlaced++;
                    }

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        //update the coordinates
        updateCoordinates(board);
    }

    /**
     * Setup the coordinates for open gl rendering
     */
    protected static void updateCoordinates(Board board) {

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
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

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
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
    protected static void updatePiece(Board board, Piece piece) {

        updatePieceVertices(board, piece);
        updatePieceUvs(board, piece);
    }

    protected static int getGroupCount(Board board, Piece piece) {

        //keep track of how many match
        int count = 0;

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
                    Piece tmp = board.getPieces()[row][col];

                    //if the group matches keep track of the count
                    if (tmp.getGroup() == piece.getGroup())
                        count++;

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        //return our result
        return count;
    }

    protected static void updateGroup(Board board, final int oldGroupId, final Piece piece) {

        boolean flag = false;

        //if there is no group id to compare to, ignore this step
        if (oldGroupId > -1) {

            for (int col = 0; col < board.getCols(); col++) {
                for (int row = 0; row < board.getRows(); row++) {

                    try {

                        //get the current piece
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
        final int connectorW = (int)(board.getDefaultWidth() * CONNECTOR_RATIO);
        final int connectorH = (int)(board.getDefaultHeight() * CONNECTOR_RATIO);

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                try {

                    //get the current piece
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

                    //update if the piece is placed
                    tmp.setPlaced(piece.isPlaced());

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
        board.setStarting(true);

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

                //flag placed false
                board.getPieces()[row][col].setPlaced(false);

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
        cut(board);

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
        final int connectorW = (int) (board.getSelected().getWidth() * CONNECTOR_RATIO);
        final int connectorH = (int) (board.getSelected().getHeight() * CONNECTOR_RATIO);

        //each piece will be the same size
        final int width = (int)(board.getSelected().getWidth() - connectorW - connectorW);
        final int height = (int)(board.getSelected().getHeight() - connectorH - connectorH);

        for (int col = 0; col < board.getCols(); col++) {

            if (flag)
                break;

            for (int row = 0; row < board.getRows(); row++) {

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
                if (!flag && east != null && piece.getGroup() != east.getGroup() && piece.getAngle() == 0 && east.getAngle() == 0) {

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
                if (!flag && west != null && piece.getGroup() != west.getGroup() && piece.getAngle() == 0 && west.getAngle() == 0) {

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
                if (!flag && south != null && piece.getGroup() != south.getGroup() && piece.getAngle() == 0 && south.getAngle() == 0) {

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
                if (!flag && north != null && piece.getGroup() != north.getGroup() && piece.getAngle() == 0 && north.getAngle() == 0) {

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

    protected static boolean isGameOver(final Board board) {

        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //if at least 1 piece is not placed, the game is not over
                if (!board.getPieces()[row][col].isPlaced())
                    return false;
            }
        }

        //all pieces are placed, game over
        return true;
    }
}