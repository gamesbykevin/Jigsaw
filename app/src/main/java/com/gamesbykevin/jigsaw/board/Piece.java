package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.gamesbykevin.jigsaw.base.Entity;

/**
 * Created by Kevin on 9/4/2017.
 */
public class Piece extends Entity {

    /**
     * The connector options
     */
    public enum Connector {
        Male, Female, None
    }

    //connectors for each side
    private Connector west, east, north, south;

    /**
     * The size of the connector will be a fraction of the piece size
     */
    public static final float CONNECTOR_RATIO = .2f;

    //are we rotating
    private boolean rotate = false;

    //this index will help us update the open gl coordinates
    private int index;

    //the group will tell us which pieces are connected
    private int group;

    public Piece(int col, int row) {

        //set the location
        super.setCol(col);
        super.setRow(row);
    }

    public void setGroup(final int group) {
        this.group = group;
    }

    public int getGroup() {
        return this.group;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * Update the vertices based on the combined current angle and pipe angle
     */
    public void updateVertices() {

        //add the angle to the current pipe to update the vertices
        super.getTransformedVertices(getAngle());
    }

    public void setRotate(final boolean rotate) {
        this.rotate = rotate;
    }

    public boolean hasRotate() {
        return this.rotate;
    }

    public void setWest(final Connector west) {
        this.west = west;
    }

    public void setEast(final Connector east) {
        this.east = east;
    }

    public void setNorth(final Connector north) {
        this.north = north;
    }

    public void setSouth(final Connector south) {
        this.south = south;
    }

    public Connector getWest() {
        return this.west;
    }

    public Connector getEast() {
        return this.east;
    }

    public Connector getNorth() {
        return this.north;
    }

    public Connector getSouth() {
        return this.south;
    }

    /**
     * Cut the bitmap to create a puzzle piece
     * @param bitmap The bitmap pertaining to the desired puzzle piece
     */
    public void cut(Bitmap bitmap, Bitmap west, Bitmap north, Bitmap east, Bitmap south) {

        //canvas object to make changes to bitmap
        Canvas canvas = new Canvas(bitmap);

        //create our paint object which will add/subtract the pixel data accordingly
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        //make the cut smoother
        paint.setAntiAlias(true);

        //source and destination coordinates
        Rect src = new Rect(), dest = new Rect();

        //how do we manipulate the canvas on drawBitmap
        PorterDuffXfermode modeIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        PorterDuffXfermode modeOut = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        //cut the piece
        dest.set(0, 0, bitmap.getWidth(), (bitmap.getHeight() / 3));
        paint.setXfermode((getNorth() == Connector.Male) ? modeIn : modeOut);
        cutNorth(canvas, (getNorth() == Connector.Male) ? north : south, src, dest, paint);

        //cut the piece
        dest.set(0, bitmap.getHeight() - (bitmap.getHeight() / 3), bitmap.getWidth(), bitmap.getHeight());
        paint.setXfermode((getSouth() == Connector.Male) ? modeIn : modeOut);
        cutSouth(canvas, (getSouth() == Connector.Male) ? south : north, src, dest, paint);

        //cut the piece
        dest.set(0, 0, (bitmap.getWidth() / 3), bitmap.getHeight());
        paint.setXfermode((getWest() == Connector.Male) ? modeIn : modeOut);
        cutWest(canvas, (getWest() == Connector.Male) ? west : east, src, dest, paint);

        //cut the piece
        dest.set(bitmap.getWidth() - (bitmap.getWidth() / 3), 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setXfermode((getEast() == Connector.Male) ? modeIn : modeOut);
        cutEast(canvas, (getEast() == Connector.Male) ? east : west, src, dest, paint);
    }

    private void cutNorth(Canvas canvas, Bitmap cutImage, Rect src, Rect dest, Paint paint) {

        //don't do anything
        if (getNorth() == Connector.None)
            return;

        //make the final cut
        cut(canvas, cutImage, src, dest, paint);
    }

    private void cutSouth(Canvas canvas, Bitmap cutImage, Rect src, Rect dest, Paint paint) {

        //don't do anything
        if (getSouth() == Connector.None)
            return;

        //make the final cut
        cut(canvas, cutImage, src, dest, paint);
    }

    private void cutWest(Canvas canvas, Bitmap cutImage, Rect src, Rect dest, Paint paint) {

        //don't do anything
        if (getWest() == Connector.None)
            return;

        //make the final cut
        cut(canvas, cutImage, src, dest, paint);
    }

    private void cutEast(Canvas canvas, Bitmap cutImage, Rect src, Rect dest, Paint paint) {

        //don't do anything
        if (getEast() == Connector.None)
            return;

        //make the final cut
        cut(canvas, cutImage, src, dest, paint);
    }

    private void cut(Canvas canvas, Bitmap cutImage, Rect src, Rect dest, Paint paint) {

        //source of the cut image will always be the same
        src.set(0, 0, cutImage.getWidth(), cutImage.getHeight());

        //draw bitmap on top of our puzzle piece to make the cut
        canvas.drawBitmap(cutImage, src, dest, paint);
    }
}