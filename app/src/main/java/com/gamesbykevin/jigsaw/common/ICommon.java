package com.gamesbykevin.jigsaw.common;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.jigsaw.activity.GameActivity;

import javax.microedition.khronos.opengles.GL10;

public interface ICommon extends Disposable
{
	/**
	 * Update the entity
	 */
	public void update();

	/**
	 * Logic to reset
	 */
	public void reset();

	/**
	 * Logic to render
	 * @param m Our open gl float array matrices
	 */
	public void render(float[] m);
}