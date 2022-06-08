package uk.ac.reading.sis05kol.mooc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public abstract class GameThread extends Thread	{

	private static final int MAX_FPS = 60;
	private double averageFPS;

	//Different mMode states
	public static final int STATE_LOSE = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;
	public static final int STATE_WIN = 5;
	public static final int STATE_NEXT_LEVEL = 6;
	public static final int STATE_LIFE_LOST = 7;

	//Control variable for the mode of the game (e.g. STATE_WIN)
	protected int mMode = 1;

	//Control of the actual running inside run()
	private boolean mRun = false;

	private boolean start_flg = false;
	private boolean action_flg = false;
		
	//The surface this thread (and only this thread) writes
	private SurfaceHolder mSurfaceHolder;
	
	//the message handler to the View/Activity thread
	private Handler mHandler;

	//Android Context - this stores almost all we need to know
	private Context mContext;
	
	//The view
	public GameView mGameView;

	//We might want to extend this call - therefore protected
	protected int mCanvasWidth = 1;
	protected int mCanvasHeight = 1;

	//Last time we updated the game physics
	protected long mLastTime = 0;
 
	protected Bitmap mBackgroundImage;
	
	protected long score = 0;

	protected long level = 0;
    //Used for time keeping
	private long now;
	private float elapsed;

    //Rotation vectors used to calculate orientation
    float[] mGravity;
    float[] mGeomagnetic;

    //Used to ensure appropriate threading
    static final Integer monitor = 1;
	

	public GameThread(GameView gameView) {		
		mGameView = gameView;
		
		mSurfaceHolder = gameView.getHolder();
		mHandler = gameView.getmHandler();
		mContext = gameView.getContext();
		
		mBackgroundImage = BitmapFactory.decodeResource
							(gameView.getContext().getResources(),
							R.drawable.background);
	}
	
	/*
	 * Called when app is destroyed, so not really that important here
	 * But if (later) the game involves more thread, we might need to stop a thread, and then we would need this
	 * Dare I say memory leak...
	 */
	public void cleanup() {		
		this.mContext = null;
		this.mGameView = null;
		this.mHandler = null;
		this.mSurfaceHolder = null;
	}
	
	//Pre-begin a game
	abstract public void setupBeginning();
	
	//Starting up the game
	public void doStart() {
		synchronized(monitor) {
			
			setupBeginning();
			
			mLastTime = System.currentTimeMillis() + 100;

			setState(STATE_RUNNING);
			
			setScore(0);
		}
	}
	
	//The thread start
	@Override
	public void run() {
		Canvas canvasRun;
		long startTime;
		long timeMillis = 1000 / MAX_FPS;
		long waitTime;
		int frameCount = 0;
		long totalTime = 0;
		long targetTime = 1000 / MAX_FPS;


		while (mRun) {
			canvasRun = null;
			startTime = System.nanoTime();

			try {
				canvasRun = mSurfaceHolder.lockCanvas(null);
				synchronized (monitor) {
					if (mMode == STATE_RUNNING) {
						this.updatePhysics();
					}
					doDraw(canvasRun);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (canvasRun != null && mSurfaceHolder != null) {
					try {
						mSurfaceHolder.unlockCanvasAndPost(canvasRun);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			timeMillis = System.nanoTime() - startTime/1000000;
			waitTime = targetTime - timeMillis;
			// This try catch is where we're capping framerate
			try	{
				if	(waitTime > 0)	{
					this.sleep(waitTime);
				}
			} catch	(Exception e)	{e.printStackTrace();}

			totalTime += System.nanoTime() - startTime;
			frameCount++;
			if (frameCount == MAX_FPS)	{
				averageFPS = 1000/((totalTime/frameCount)/1000000);
				frameCount = 0;
				totalTime = 0;
			}
		}
	}
	
	/*
	 * Surfaces and drawing
	 */
	public void setSurfaceSize(int width, int height) {
		synchronized (monitor) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			// don't forget to resize the background image
			mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
		}
	}


	protected void doDraw(Canvas canvas) {
		
		if(canvas == null) return;

		if(mBackgroundImage != null) canvas.drawBitmap(mBackgroundImage, 0, 0, null);
	}
	
	private void updatePhysics() {
		now = System.currentTimeMillis();
		elapsed = (now - mLastTime) / 1000.0f;

		updateGame(elapsed);

		mLastTime = now;
	}
	
	abstract protected void updateGame(float secondsElapsed);
	
	/*
	 * Control functions
	 */
	
	//Finger touches the screen
	public boolean onTouch(MotionEvent e) {
		if(e.getAction() != MotionEvent.ACTION_DOWN) return false;
		
		if(mMode == STATE_READY || mMode == STATE_LOSE || mMode == STATE_WIN) {
			doStart();
			return true;
		}
		
		if(mMode == STATE_PAUSE || mMode == STATE_NEXT_LEVEL) {
			unpause();
			return true;
		}

		if(mMode == STATE_LIFE_LOST)	{
			try {
				sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			unpause();
		}
		
		synchronized (monitor) {
				this.actionOnTouch(e.getRawX(), e.getRawY());
		}
		 
		return false;
	}


	
	protected void actionOnTouch(float x, float y) {
		//Override to do something

	}

	//The Orientation has changed
	@SuppressWarnings("deprecation")
	public void onSensorChanged(SensorEvent event) {
		synchronized (monitor) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    actionWhenPhoneMoved(orientation[2],orientation[1],orientation[0]);
                }
            }
		}
	}
	
	protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {
		//Override to do something
	}
	
	/*
	 * Game states
	 */
	public void pause() {
		synchronized (monitor) {
			if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
		}
	}
	
	public void unpause() {
		// Move the real time clock up to now
		synchronized (monitor) {
			mLastTime = System.currentTimeMillis();
		}
		setState(STATE_RUNNING);
	}	

	//Send messages to View/Activity thread
	public void setState(int mode) {
		synchronized (monitor) {
			setState(mode, null);
		}
	}

	public void setState(int mode, CharSequence message) {
		synchronized (monitor) {
			mMode = mode;

			if (mMode == STATE_RUNNING) {
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("viz", View.INVISIBLE);
				b.putBoolean("showAd", false);	
				msg.setData(b);
				mHandler.sendMessage(msg);
			} 
			else {				
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				
				Resources res = mContext.getResources();
				CharSequence str = "";

				if (mMode == STATE_READY)
					str = res.getText(R.string.mode_ready);
				else 
					if (mMode == STATE_PAUSE)
						str = res.getText(R.string.mode_pause);
					else 
						if (mMode == STATE_LOSE)
							str = res.getText(R.string.mode_lose);
						else 
							if (mMode == STATE_WIN)
								str = res.getText(R.string.mode_win);
							else
								if(mMode == STATE_NEXT_LEVEL)
									str = res.getText(R.string.mode_next_level);
								else
									if(mMode == STATE_LIFE_LOST)	{
										str = res.getText(R.string.mode_life_lost);
									}

				if (message != null) {
					str = message + "\n" + str;
				}

				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);

				msg.setData(b);
				mHandler.sendMessage(msg);
			}
		}
	}
	
	/*
	 * Getter and setter
	 */	
	public void setSurfaceHolder(SurfaceHolder h) {
		mSurfaceHolder = h;
	}
	
	public boolean isRunning() {
		return mRun;
	}
	
	public void setRunning(boolean running) {
		mRun = running;
	}
	
	public int getMode() {
		return mMode;
	}

	public void setMode(int mMode) {
		this.mMode = mMode;
	}





	/* ALL ABOUT SCORES */
	
	//Send a score to the View to view 
	//Would it be better to do this inside this thread writing it manually on the screen?
	public void setScore(long score) {
		this.score = score;
		
		synchronized (monitor) {
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("score", true);
			b.putString("text", "Score: " + getScoreString().toString());
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
	}

	public float getScore() {
		return score;
	}
	
	public void updateScore(long score) {
		if	(this.score + score < 0)	{
			setScore(0);
		} else {
			this.setScore(this.score + score);
		}
	}

	protected CharSequence getScoreString() {
		return Long.toString(Math.round(this.score));
	}

	public void setLevel(int level)	{
		this.level = level;

		synchronized (monitor) {
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("score", true);
			b.putString("text", "Level " + getScoreString().toString());
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
	}

	public float getLevel() {
		return level;
	}

	public void updateLevel(long level) {
		this.setScore(this.level = level);
	}

	protected CharSequence getLevelString() {
		return Long.toString(Math.round(this.level));
	}

}

// This file is part of the course "Begin Programming: Build your first mobile game" from futurelearn.com
// Copyright: University of Reading and Karsten Lundqvist
// It is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// It is is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// 
// You should have received a copy of the GNU General Public License
// along with it.  If not, see <http://www.gnu.org/licenses/>.