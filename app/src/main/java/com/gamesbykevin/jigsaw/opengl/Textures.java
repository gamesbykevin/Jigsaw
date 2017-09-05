package com.gamesbykevin.jigsaw.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;

import static com.gamesbykevin.jigsaw.util.UtilityHelper.DEBUG;

/**
 * Created by Kevin on 7/23/2017.
 */

public class Textures {

    //array containing all the texture ids
    public static int[] IDS;

    //background image
    public static int TEXTURE_ID_BACKGROUND = 0;

    //texture id for each shape
    public static int TEXTURE_ID_SQUARE = 0;
    public static int TEXTURE_ID_HEXAGON = 0;
    public static int TEXTURE_ID_DIAMOND = 0;

    //the id of our source image to create the puzzle
    public static int TEXTURE_ID_IMAGE_SOURCE = 0;

    public static final int INDEX_TEXTURE_ID_IMAGE_SOURCE = 4;

    public static float TEXTURE_SQUARE_COLS = 5.0f;
    public static float TEXTURE_SQUARE_ROWS = 2.0f;

    public static float TEXTURE_DIAMOND_COLS = 5.0f;
    public static float TEXTURE_DIAMOND_ROWS = 2.0f;

    public static float TEXTURE_HEXAGON_COLS = 14.0f;
    public static float TEXTURE_HEXAGON_ROWS = 2.0f;

    //store reference to access resources
    private final Context activity;

    //keep track of the current index
    private int index = 0;

    public Textures(Context activity) {

        this.activity = activity;

        //create array containing all the texture ids
        IDS = new int[5];
    }

    /**
     * Load all the textures
     */
    public void loadTextures() {

        //reset index
        this.index = 0;

        //sprite sheet containing a lot of images
        //Bitmap square = BitmapFactory.decodeResource(activity.getResources(), R.drawable.square);
        //Bitmap ball = Bitmap.createBitmap(sheet, (Ball.DIMENSIONS * i), 0, Ball.DIMENSIONS, Ball.DIMENSIONS);

        //load the textures
        TEXTURE_ID_BACKGROUND = loadTexture(R.drawable.background);
        TEXTURE_ID_DIAMOND = loadTexture(R.drawable.diamonds);
        TEXTURE_ID_HEXAGON = loadTexture(R.drawable.hexagons);
        TEXTURE_ID_SQUARE = loadTexture(R.drawable.squares);
    }

    /**
     * Load a single texture
     * @param resId The resource id
     * @return texture id from generating a texture
     */
    public int loadTexture(int resId) {

        int textureId = 0;

        try {

            //make sure we aren't pre-scaling the image when loading the texture(s)
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            //get our bitmap
            final Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), resId, options);

            //load and get the texture id
            textureId = loadTexture(bitmap, index);

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //keep increasing the index
        index++;

        //return our value
        return textureId;
    }

    public static int loadTexture(Bitmap bitmap, final int index) {

        try {

            //our container to generate the textures
            GLES20.glGenTextures(1, IDS, index);

            //bind texture to texture id
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, IDS[index]);

            // Set filtering
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();

            if (IDS[index] == 0) {
                throw new Exception("Error loading texture: " + index);
            } else {

                //display texture id
                if (DEBUG)
                    UtilityHelper.logEvent("Texture loaded id: " + IDS[index]);
            }

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //return our value
        return IDS[index];
    }
}