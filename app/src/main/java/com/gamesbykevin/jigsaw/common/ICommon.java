package com.gamesbykevin.jigsaw.common;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.jigsaw.activity.GameActivity;

import javax.microedition.khronos.opengles.GL10;

public interface ICommon extends Disposable
{
	/**
	 * Update the entity
	 */
	public void update(GameActivity activity);

	/**
	 * Logic to reset
	 */
	public void reset();
}