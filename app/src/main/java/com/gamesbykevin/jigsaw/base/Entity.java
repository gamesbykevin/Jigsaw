package com.gamesbykevin.jigsaw.base;

/**
 * Created by Kevin on 9/2/2017.
 */
public class Entity extends com.gamesbykevin.androidframeworkv2.base.Entity {

    private float[] uvs;

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

    public void setTextureCoordinates(float col, float row, float width, float height) {

        if (getTextureCoordinates() == null)
            this.uvs = new float[8];

        //assign values
        uvs[0] = col; uvs[1] = row;
        uvs[2] = col; uvs[3] = row + height;
        uvs[4] = col + width; uvs[5] = row + height;
        uvs[6] = col + width; uvs[7] = row;
    }

    public float[] getTextureCoordinates() {
        return this.uvs;
    }
}