package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    protected static final float CONNECTOR_RATIO = .25f;

    //are we rotating
    private boolean rotate = false;

    //this index will help us update the open gl coordinates
    private int index;

    //the group will tell us which pieces are connected
    private int group;

    //add texture padding to prevent texture bleeding
    public static final int TEXTURE_PADDING = 2;

    //where do we place this piece to solve the board
    private int destinationX, destinationY;

    //has this piece been placed
    private boolean placed = false;

    public Piece(int col, int row) {

        //set the location
        super.setCol(col);
        super.setRow(row);
    }

    public void setPlaced(final boolean placed) {
        this.placed = placed;
    }

    public boolean isPlaced() {
        return this.placed;
    }

    public void setDestinationX(final int destinationX) {
        this.destinationX = destinationX;
    }

    public void setDestinationY(final int destinationY) {
        this.destinationY = destinationY;
    }

    public int getDestinationX() {
        return this.destinationX;
    }

    public int getDestinationY() {
        return this.destinationY;
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

    protected void setWest(final Connector west) {
        this.west = west;
    }

    protected void setEast(final Connector east) {
        this.east = east;
    }

    protected void setNorth(final Connector north) {
        this.north = north;
    }

    protected void setSouth(final Connector south) {
        this.south = south;
    }

    protected Connector getWest() {
        return this.west;
    }

    protected Connector getEast() {
        return this.east;
    }

    protected Connector getNorth() {
        return this.north;
    }

    protected Connector getSouth() {
        return this.south;
    }

    protected boolean isMale(Connector connector) {
        return (connector == Connector.Male);
    }

    protected boolean isFemale(Connector connector) {
        return (connector == Connector.Female);
    }

    protected boolean isNone(Connector connector) {
        return (connector == Connector.None);
    }

    protected Bitmap cutPuzzlePiece(Bitmap picture, final int startX, final int startY, final int w, final int h, Bitmap north, Bitmap south, Bitmap west, Bitmap east) {

        final int connectorW = (int)(w * CONNECTOR_RATIO);
        final int connectorH = (int)(h * CONNECTOR_RATIO);

        int fullW = w + (connectorW * 2) + TEXTURE_PADDING;
        int fullH = h + (connectorH * 2) + TEXTURE_PADDING;

        //create an empty mutable bitmap
        Bitmap tmp = Bitmap.createBitmap(fullW, fullH, Bitmap.Config.ARGB_8888);

        //source and destination coordinates
        Rect src = new Rect();
        Rect dest = new Rect();

        //create our canvas to draw on
        Canvas canvas = new Canvas(tmp);

        //how do we manipulate the canvas on drawBitmap
        PorterDuffXfermode modeIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        PorterDuffXfermode modeOut = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        //create paint object to cut the image(s)
        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        final int mx = (fullW / 2);
        final int my = (fullH / 2);

        final int hw = (w / 2);
        final int hh = (h / 2);

        //draw the middle part of the image in the middle
        src.set(startX, startY, startX + w, startY + h);
        dest.set(mx - hw, my - hh, mx + hw, my + hh);
        canvas.drawBitmap(picture, src, dest, null);

        if (isMale(getWest())) {

            //draw the side image
            dest.set(mx - hw - connectorW, my - hh, mx - hw, my + hh);
            src.set(startX - connectorW, startY, startX, startY + h);
            canvas.drawBitmap(picture, src, dest, null);

            //cut the puzzle connector
            paint.setXfermode(modeIn);
            src.set(0, 0, west.getWidth(), west.getHeight());
            canvas.drawBitmap(west, src, dest, paint);

        } else if (isFemale(getWest())) {

            //update destination
            dest.set(mx - hw, my - hh, mx - hw + connectorW, my + hh);

            //cut the puzzle connector
            paint.setXfermode(modeOut);
            src.set(0, 0, east.getWidth(), east.getHeight());
            canvas.drawBitmap(east, src, dest, paint);
        }

        if (isMale(getEast())) {

            //draw the side image
            dest.set(mx + hw, my - hh, mx + hw + connectorW, my + hh);
            src.set(startX + w, startY, startX + w + connectorW, startY + h);
            canvas.drawBitmap(picture, src, dest, null);

            //cut the puzzle connector
            paint.setXfermode(modeIn);
            src.set(0, 0, east.getWidth(), east.getHeight());
            canvas.drawBitmap(east, src, dest, paint);

        } else if (isFemale(getEast())) {

            //update destination
            dest.set(mx + hw - connectorW, my - hh, mx + hw, my + hh);

            //cut the puzzle connector
            paint.setXfermode(modeOut);
            src.set(0, 0, west.getWidth(), west.getHeight());
            canvas.drawBitmap(west, src, dest, paint);
        }

        if (isMale(getNorth())) {

            //draw the side image
            dest.set(mx - hw, my - hh - connectorH, mx + hw, my - hh);
            src.set(startX, startY - connectorH, startX + w, startY);
            canvas.drawBitmap(picture, src, dest, null);

            //cut the puzzle connector
            paint.setXfermode(modeIn);
            src.set(0, 0, north.getWidth(), north.getHeight());
            canvas.drawBitmap(north, src, dest, paint);

        } else if (isFemale(getNorth())) {

            //update destination
            dest.set(mx - hw, my - hh, mx + hw, my - hh + connectorH);

            //cut the puzzle connector
            paint.setXfermode(modeOut);
            src.set(0, 0, south.getWidth(), south.getHeight());
            canvas.drawBitmap(south, src, dest, paint);
        }

        if (isMale(getSouth())) {

            //draw the side image
            dest.set(mx - hw, my + hh, mx + hw, my + hh + connectorH);
            src.set(startX, startY + h, startX + w, startY + h + connectorH);
            canvas.drawBitmap(picture, src, dest, null);

            //cut the puzzle connector
            paint.setXfermode(modeIn);
            src.set(0, 0, south.getWidth(), south.getHeight());
            canvas.drawBitmap(south, src, dest, paint);

        } else if (isFemale(getSouth())) {

            //update destination
            dest.set(mx - hw, my + hh - connectorH, mx + hw, my + hh);

            //cut the puzzle connector
            paint.setXfermode(modeOut);
            src.set(0, 0, north.getWidth(), north.getHeight());
            canvas.drawBitmap(north, src, dest, paint);
        }

        //return our result
        return tmp;
    }

    public boolean contains(final float x, final float y) {

        //if any of these are true, it is outside (false)
        if (x < getX())
            return false;
        if (y < getY())
            return false;
        if (x > getX() + getWidth())
            return false;
        if (y > getY() + getHeight())
            return false;

        //the coordinate is inside (true)
        return true;
    }
}