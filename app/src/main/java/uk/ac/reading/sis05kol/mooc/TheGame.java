package uk.ac.reading.sis05kol.mooc;

//Other parts of the android libraries that we use
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class TheGame extends GameThread{

    // Stores the image of a ball
    private Ball mBall;
    private Ball mBallGlow;
    // Stores the image of the smiley ball (score ball)
    private Ball mSmileyBall;
    // Stores the image of the sad ball (no-score ball)
    private Ball mSadBall;
    // Stores the image of the Paddle used to hit the ball
    private Paddle mPaddle;
    private Paddle mPaddleGlow;

    private Enemy mEnemy;
    private Enemy[] mEnemies;

    //The surface this thread (and only this thread) writes upon
    private SurfaceHolder mSurfaceHolder;

    //the message handler to the View/Activity thread
    private Handler mHandler;

    // Space between bricks
    private float gapBetweenBricks = 15;
    private ArrayList<Brick> mBricks = new ArrayList<>();
    private ArrayList<Brick> mActiveBricks = new ArrayList<>();

    private Bitmap basicBrick, powerBrick, lifeBrick;

    // Initial starting level
    private int initLevel = 1;
    // Always start the game at the init level value.
    private int nLevel = initLevel;
    // Unchanged lives value which will be referenced when lives are reset.
    private int initLives = 3;
    // Lives player has in the game.
    private int lives = initLives;
    // Counts bricks hit, used to deduct points if life is lost.
    private int mBrickCount = 0;
    // A factor that determines where the ball will deflect to when hitting the paddle.
    private float friction = (float)0.6;

    private float x = 0;

    //  This is run before anything else, so we can prepare things here
    public TheGame(GameView gameView) {

        //  House keeping
        super(gameView);

        //Prepare the image so we can draw it on the screen (using a canvas)
        mBall = new Ball (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.ball));

        mBallGlow = new Ball (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.ball_glow));

        //Prepare the image of the paddle so we can draw it on the screen (using a canvas)
        mPaddle = new Paddle (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.paddle));

        mPaddleGlow = new  Paddle (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.paddle_glow));

        mEnemy = new Enemy(BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.enemy));

        // Idea is to create a row of bricks - Unknown screen size.
        // Set up a number of bricks and decide how many of them to use in setupBeginning()

        // Basic brick  -   Updates score by 1 as do most other bricks.
        Bitmap basicBrick = brickImage(gameView, Brick.BrickType.BASIC);
            this.basicBrick = basicBrick;
        // Power Brick  -   Awards player 10 points
        Bitmap powerBrick = brickImage(gameView, Brick.BrickType.POWERPOINT);
            this.powerBrick = powerBrick;
        // Life Brick   -   Gives additional lives to player
        Bitmap lifeBrick = brickImage(gameView, Brick.BrickType.LIFE);
            this.lifeBrick = lifeBrick;

        setLevel(nLevel);
        updateLevel(nLevel);


        //Prepare the image of the SmileyBall so we can draw it on the screen (using a canvas)
        mSmileyBall =  new Ball (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.smiley_ball));

        //Prepare the image of the SadBall(s) so we can draw it on the screen (using a canvas)
        mSadBall =  new Ball (BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.sad_ball));
    }


    //This is run before a new game (also after an old game)
    @Override
    public void setupBeginning() {

        setupBall();    // Ball positioned in middle of screen, speed dictates the ball moves straight down toward paddle.
        setupPaddle();  // Paddle at the bottom of the screen in the middle
        setupEnemy();
        initBricks();   // If there are bricks and the first brick element is not null then
    }

    @Override
    protected void doDraw(Canvas canvas) {
        //If there isn't a canvas to do nothing
        //It is ok not understanding what is happening here
        if(canvas == null) return;

        //House keeping
        super.doDraw(canvas);
        // Draw ball & paddle
        mBall.draw(canvas);
        mBallGlow.draw(canvas);
        mPaddle.draw(canvas);
        mPaddleGlow.draw(canvas);
        mEnemy.draw(canvas);

        // Checks if bricks that are active have been eliminated
        // if ActiveBrick element i has not been eliminated, draw brick, else, do not draw
        if  (mActiveBricks != null) {
            for(int i = 0; i < mActiveBricks.size(); i++) {
                if  (mActiveBricks.get(i) != null)  {
                    mActiveBricks.get(i).draw(canvas);
                }
            }
        }



        //Draw SmileyBall
        // mSmileyBall.draw(canvas);

        //Loop through all SadBall
        //for(int i = 0; i < 1; i++) {
        //    //Draw SadBall in position i
        //    mSadBall.draw(canvas);
        //}
    }


    //This is run whenever the phone is touched by the user
    @Override
    protected void actionOnTouch(float x, float y) {

        // Moves the ball to the x position of the touch instantly
        //mPaddle.stopAt(x);

        // Moves the paddle towards the x coordinate being touched
        mPaddle.moveTowards(x);
        mPaddleGlow.moveTowards(x);
        this.x = x;
    }

    //This is run whenever the phone moves around its axises
    @Override
    protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {
        //Change the paddle speed
        /*mPaddleSpeedX = mPaddleSpeedX + 70f * xDirection;

        //If paddle is outside the screen and moving further away
        //Move it into the screen and set its speed to 0
        if((mPaddleX - mPaddle.getWidth()/2) <= 0 && mPaddleSpeedX <= 0) {
            mPaddleSpeedX = 0;
            mPaddleX = 0 + (mPaddle.getWidth()/2);
        }
        if((mPaddleX + mPaddle.getWidth()/2) >= mCanvasWidth && mPaddleSpeedX >= 0) {
            mPaddleSpeedX = 0;
            mPaddleX = mCanvasWidth - (mPaddle.getWidth()/2);
        }*/

    }

    private void moveSprite(Sprite s, float secondsElapsed)    {

        // Moves the sprite during the game runtime
        s.move(secondsElapsed);

        // Checks to see if sprite is colliding with top of screen, if so, move away from when collision occurs.
        if (s.isMovingUp() && s.getTop() <= 0 || s.isMovingDown() && s.getBottom() >= mCanvasHeight)
            s.moveY();

        // sprite moves to left/right side depending which side of screen it hits. E.g. hits right side, move left.
        if ((s.isMovingLeft() && s.getLeft() <= 0) || (s.isMovingRight() && s.getRight() >= mCanvasWidth))
            s.moveX();

    }

    private void moveBall(Sprite s, float secondsElapsed)    {

        // Moves the ball during the game runtime
        s.move(secondsElapsed);

        // Checks to see if ball is colliding with top of screen, if so, move away from when collision occurs.
        if (s.isMovingUp() && s.getTop() <= 0)  {
            s.moveY();
        } // If ball is moving down and the ball reaches the canvasheight (bottom of screen)
        else if (s.isMovingDown() && s.getBottom() >= mCanvasHeight)
        {
            // You lose a life if the ball hits the bottom
            loseALife();
        }
        // ball moves to left/right side depending which side of screen it hits. E.g. hits right side, move left.
        if ((s.isMovingLeft() && s.getLeft() <= 0) || (s.isMovingRight() && s.getRight() >= mCanvasWidth))  {
            s.moveX();
        }

    }

    //This is run just before the game "scenario" is printed on the screen
    @Override
    protected void updateGame(float secondsElapsed) {
        if (mBallGlow.getX() != mBall.getX() || mBallGlow.getY() != mBall.getY()) {
            mBallGlow.setSpeed(mBall.getSpeedX(), mBall.getSpeedY());
            mBallGlow.setPosition(mBall.getX(), mBall.getY());
        }
        mBallGlow.move(secondsElapsed);


        checkBallCollisions();
        checkBrickCollisions();

        // Keeps ball moving during runtime and deals with ball hitting any side of screen
        moveBall(mBall, secondsElapsed);
        moveSprite(mEnemy, secondsElapsed);

        // Move paddle toward touch on screen over period of time
        mPaddle.move(secondsElapsed);
        mPaddleGlow.move(secondsElapsed);

        /*if (mPaddle.isPaddleAtDestination(mPaddle.getX(), x)) {
            mPaddle.stopPaddle(mPaddle.getX());
            mPaddleGlow.stopPaddle(mPaddle.getX());
        }*/

        if (mPaddle.isMovingLeft() && mPaddle.getX() <= 0) {
            mPaddle.stopAt(0);
            mPaddleGlow.stopAt(mPaddle.getX());
        } else if (mPaddle.isMovingRight() && mPaddle.getX() >= mCanvasWidth) {
            mPaddle.stopAt(mCanvasWidth);
            mPaddleGlow.stopAt(mPaddle.getX());
        }

        //Check if the ball hits either the left side or the right side of the screen
        //But only do something if the ball is moving towards that side of the screen
        //If it does that => change the direction of the ball in the X direction


        /*//Loop through all SadBalls
        for(int i = 0; i < mSadBallX.length; i++) {
            //Perform collisions (if necessary) between SadBall in position i and the red ball
            updateBallCollision(mSadBallX[i], mSadBallY[i]);
        }

        //If the ball goes out of the top of the screen and moves towards the top of the screen =>
        //change the direction of the ball in the Y direction
        if(mBallY <= mBall.getWidth() / 2 && mBallSpeedY < 0) {
            mBallSpeedY = -mBallSpeedY;
        }

        //If the ball goes out of the bottom of the screen => lose the game
        if(mBallY >= mCanvasHeight) {
            setState(GameThread.STATE_LOSE);
        }*/

    }

    /**
     * Initialises bricks with x and y coordinates
     */
    private void initBricks() {

        if ( mBricks != null && mBricks.get(0) != null) {

            float brickWidth = mBricks.get(0).getWidth() + gapBetweenBricks;
            // we want to use enough bricks to fill a row
            int bricksPerRow = (int) (mCanvasWidth / brickWidth);

            // but we can only use up to the number of bricks that we already have available
            if (bricksPerRow > mBricks.size()) {
                bricksPerRow = mBricks.size();
            }

            // initialise the array to hold the bricks we want to use
            int brickArraySize = mBricks.size();
            // set up position of first brick
            float margin = mCanvasWidth - (brickWidth * bricksPerRow);
            float nextBrickX = (margin / 2 + brickWidth / 2);
            float nextBrickY = mCanvasHeight / 3;

            // reference the required number of available bricks in the active brick array
            // Set bricks positions
            for (int i = 0; i < brickArraySize; i++) {
                mActiveBricks.add(i, mBricks.get(i));

                // If x coordinate of the next brick is over the value of mCanvasWidth then
                // move to a new row above current row
                if (nextBrickX > mCanvasWidth) {
                    nextBrickY += -(mBricks.get(i).getHeight() + gapBetweenBricks);
                    nextBrickX = margin / 2 + brickWidth / 2;
                }

                // Uses new x and y coordinates as new brick position. Rinse n repeat.
                mActiveBricks.get(i).setPosition(nextBrickX, nextBrickY);
                nextBrickX += brickWidth;
            }
        }
    }

    /**
     * set the position and initial speed of the ball at start of new game/level
     */
    private void setupBall()    {
        // Set ball to middle of the canvas/screen.
        mBall.setPosition(mCanvasWidth / 2, mCanvasHeight / 2);
        // Ball move down at an angle to randomise movement and collisions.
        mBall.setSpeed(0, mCanvasHeight/3);
        // mBallGlow is a semantic feature, a glow
        mBallGlow.setPosition(mBall.getX(), mBall.getY());
        mBallGlow.setSpeed(mBall.getSpeedX(), mBall.getSpeedY());
    }

    /**
     * set initial position and speed of paddle at start of new game/level
     */
    private void setupPaddle()   {

        // X: middle of X axis      Y: Full paddle just above the CanvasHeight limit.
        mPaddle.setPosition(mCanvasWidth / 2, mCanvasHeight - ( mPaddle.getHeight() + mPaddleGlow.getHeight()) / 2);
        // Speed of paddle is 0 when game starts and for newer levels so it doesn't try to react to previous level touches.
        mPaddle.setSpeed(0, 0);
        // mPaddle Glow is a semantic feature, it follows the paddle's movements.
        mPaddleGlow.setPosition(mPaddle.getX(), mPaddle.getY());
        // mPaddleGlow is a semantic feature, copies the paddles speed.
        mPaddleGlow.setSpeed(mPaddle.getSpeedX(), mPaddle.getSpeedY());
    }

    private void setupEnemy()   {
        Random rnd = new Random();
        int randXPosition = rnd.nextInt(mCanvasWidth);
        mEnemy.setPosition(randXPosition, mCanvasHeight / 6);
        mEnemy.setSpeed(mCanvasWidth/3,mCanvasHeight / 3);
    }

    /**
     * Checks if ball collides with paddle and other objects
     */
    private void checkBallCollisions()  {
        if (mBall.isMovingDown() && mBall.isOverlapping(mPaddle)) {
            // Bounce the ball off of the paddle, not the paddle from the ball.
            //mBall.reboundOff(mPaddle);
            mBall.setSpeed(mBall.getSpeedX() + mPaddle.getSpeedX() * friction, -mBall.getSpeedY());
            mBallGlow.setSpeed(mBall.getSpeedX(), mBall.getSpeedY());
        }

        /*if (mBall.reboundOff(mEnemy) && mEnemy.reboundOff(mBall)) {
            updateScore(-1);
        }*/
        if (mBall.isOverlapping(mEnemy) && mEnemy.isOverlapping(mBall))    {
            mEnemy.reboundOff(mBall);
            mBall.reboundOff(mEnemy);
            updateScore(-1);
        }
    }

    /**
     * Check if bricks are active or not
     * Takes action if a brick is not active.
     */
    private void checkBrickCollisions()  {
        // This is here as a parameter to ensure that the rebound function is called
        // only once even when the ball collides with two bricks.
        int j = 0, k = 0;
        // if the array of bricks isn't equal to nothing then
        if (mActiveBricks != null) {
            // For every brick in the mActiveBricks array
            for (int i = 0; i < mActiveBricks.size(); i++) {
                // if mActiveBrick 'i' is not equal to nothing AND the ball is overlapping this brick then
                if (mActiveBricks.get(i) != null && mBall.isOverlapping(mActiveBricks.get(i))) {
                    // Rebound the ball off of the bricks
                    if (j == 0)
                        mBall.reboundOff(mActiveBricks.get(i));
                        j++;
                    // When brick is hit, the brick disappears from array holding that brick.
                    mActiveBricks.remove(i);
                    // Adds to score if brick is hit.
                    updateScore(1);
                    this.mBrickCount++;
                    // Breaks out loop when a brick is hit. Avoids problems if more than one is hit.
                }
                // If enemy collides with a brick
                else if (mActiveBricks.get(i) != null && mEnemy.isOverlapping(mActiveBricks.get(i)))
                {
                    // Bounce enemy off brick once
                    if (k == 0)
                        mEnemy.reboundOff(mActiveBricks.get(i));
                        k++;
                }
            }

            // check if there are remaining bricks.
            boolean remainingBricks = false;
            for (int i = 0; i < mActiveBricks.size(); i++) {
                if (mActiveBricks.get(i) != null) {
                    remainingBricks = true;
                }
            }

            // if all bricks are gone from mActiveBricks Array...
            // TODO: Game state to win, change level, repopulate array.
            if (!remainingBricks) {
                this.nLevel++;
                setState(STATE_NEXT_LEVEL);
                setLevel(nLevel);
                setupBeginning();
            }
        }
    }

    /**
     * Used to determine which bitmap image is used for which brickType.
     * @param gameView  used to set image
     * @param brickType which brick type we want an image for
     * @return  image that corresponds to the imported brickType
     */
    private Bitmap brickImage(GameView gameView, Brick.BrickType brickType)    {

        //  Default image is a basic brick
        Bitmap image = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.basic_brick);

        if  (brickType == Brick.BrickType.LIFE)
            image = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.life_brick);

        if  (brickType == Brick.BrickType.POWERPOINT)
            image = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.power_brick);

        return image;
    }

    public void setLevel (int nLevel)  {
        Level level = new Level();

        switch(nLevel)
        {
            case 1:
                this.mBricks = level.Level(1, basicBrick, powerBrick, lifeBrick);
                break;
            case 2:
                this.mBricks = level.Level(2, basicBrick, powerBrick, lifeBrick);
                break;
            case 3:
                this.mBricks = level.Level(3, basicBrick, powerBrick, lifeBrick);
                break;
            case 4:
                this.mBricks = level.Level(4, basicBrick, powerBrick, lifeBrick);
                break;
            case 5:
                this.mBricks = level.Level(5, basicBrick, powerBrick, lifeBrick);
                break;
        }

    }

    private void loseALife()   {

        if  (this.lives != 0)
        {
            // Clear active bricks
            this.mActiveBricks.clear();
            // Lose a life
            this.lives--;
            // Deduct score gained in current level.
            if(mBrickCount != 0)
                updateScore(-mBrickCount);
            // Reset brick counter.
            this.mBrickCount = 0;
            // Set game state to life lost.
            setState(STATE_LIFE_LOST);
            // Reset paddle, ball, bricks.
            setupBeginning(); }

            else if (this.lives == 0)
        {
            // Set game state to lose.
            setState(STATE_LOSE);

            // Reset score
            setScore(0);

        }
    }

    //Send a score to the View to view
    //Would it be better to do this inside this thread writing it manually on the screen?
    public void setLives(long lives) {

        synchronized (monitor) {
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean("Lives", true);
            b.putString("text", "Lives: " + getScoreString().toString());
            msg.setData(b);
            mHandler.sendMessage(msg);

        }
    }

    public float getLives() {
        return lives;
    }

    public void updatelives(long score) {
        if	(this.lives + score < 0)	{
            setLives(0);
        } else {
            this.setLives(this.lives + score);

        }}}



// This file is part of the course "Begin Programming: Build your first mobile game" from futurelearn.com
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
// You should have received   copy of the GNU General Public License
// along with it.  If not, see <http://www.gnu.org/licenses/>.
