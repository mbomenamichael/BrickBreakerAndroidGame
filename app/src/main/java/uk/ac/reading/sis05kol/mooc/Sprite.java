package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.constraint.solver.widgets.Rectangle;

public class Sprite {

    // Image used to represent Sprite in the game
    private Bitmap sImage;

    // X and Y coordinates for Sprite's position
    private float sY = -100;
    private float sX = -100;

    // How fast the Sprite will move (pixels per second)
    private float sSpeedX = 0;
    private float sSpeedY = 0;

    // How big the Sprite is
    private float sWidth = 0;
    private float sHeight = 0;
    private float sHalfWidth = 0;
    private float sHalfHeight = 0;

    /**
     * Sprite class, default properties of every object of type Sprite
     * @param image image of the object for game
     */
    public Sprite (Bitmap image){

        sImage = image;
        sWidth= image.getWidth();
        sHeight = image.getHeight();
        sHalfWidth = sWidth / 2;
        sHalfHeight = sHeight / 2;

    }


    public float getX()     { return this.sX; }
    public float getY()     { return this.sY; }
    public float getWidth() { return this.sWidth; }
    public float getHeight(){ return this.sHeight; }

    // Frame/proportions of the object. E.g. the left side of object is the x coord minus half of the objects width.
    public float getLeft()  { return sX-sHalfWidth; }       // Left side of object
    public float getRight() { return sX+sHalfWidth; }       // Right side of object
    public float getTop()   { return sY-sHalfHeight; }      // Top of object
    public float getBottom(){ return sY+sHalfHeight; }      // Bottom of object

    public boolean contains(float x, float y) {
        return getLeft() <= x && x <= getRight() && getTop() <= y && y <= getBottom();
    }


    public void setPosition(float x, float y) {
        sX = x;
        sY = y;
    }

    /**
     * Draw the image on the canvas, centred at the current position
     * @param canvas    Canvas that the game is made on
     */
    public void draw(Canvas canvas) {
        // drawBitmap uses top left corner as reference,
        // null means that we will use the image without any extra features (called Paint)
        canvas.drawBitmap( sImage, getLeft(), getTop(), null);
    }

    // Takes object and checks if objects overlap eachother during runtime
    public boolean topOverlaps( Sprite s ) { return getTop() <= s.getBottom() && getTop() >= s.getTop(); }
    public boolean bottomOverlaps( Sprite s ) { return getBottom() >= s.getTop() && getBottom() <= s.getBottom(); }
    public boolean leftOverlaps( Sprite s ) { return getLeft() >= s.getLeft() && getLeft() <= s.getRight(); }
    public boolean rightOverlaps( Sprite s ) { return getRight() <= s.getRight() && getRight() >= s.getLeft(); }
    public boolean widthOverlap( Sprite s ) { return s.getLeft() <= getRight() && s.getRight() >= getLeft(); }
    public boolean heightOverlap( Sprite s ) { return s.getTop() <= getBottom() && s.getBottom() >= getTop(); }

    /**
     * Returns boolean using all functions above to whether objects are overlapping during runtime
     * @param s Sprite - Object (Paddle, Brick, Ball...)
     * @return
     */
    public boolean isOverlapping( Sprite s ) {
        return ((topOverlaps(s) || bottomOverlaps(s)) && widthOverlap(s)) ||
                ((leftOverlaps(s) || rightOverlaps(s)) && heightOverlap(s));
    }

    // Once the overlap type is retrieved, get overlap size from these functions.
    public float topOverlap( Sprite s ) { return s.getBottom() - getTop(); }
    public float bottomOverlap( Sprite s ) { return getBottom() - s.getTop(); }
    public float leftOverlap( Sprite s ) { return s.getRight() - getLeft(); }
    public float rightOverlap( Sprite s ) { return getRight() - s.getLeft(); }

    /**
     * set and get speed/direction
     * @param dx    new speed x coord value
     * @param dy    new speed y coord value
     */
    public void setSpeed( float dx, float dy ) {
        sSpeedX = dx;
        sSpeedY = dy;
    }
    public float getSpeedX() { return sSpeedX; }
    public float getSpeedY() { return sSpeedY; }
    public boolean isMovingLeft() { return sSpeedX < 0; }
    public boolean isMovingRight() { return sSpeedX > 0; }
    public boolean isMovingDown() { return sSpeedY > 0; }
    public boolean isMovingUp() { return sSpeedY < 0; }
    public float getSpeed() { return (float) Math.sqrt((sSpeedX*sSpeedX)+(sSpeedY * sSpeedY)); }
    // Move object opposite direction it was going toward
    public void moveX() { sSpeedX = -sSpeedX; }
    public void moveY() { sSpeedY = -sSpeedY; }

    /**
     * Updates position of object according to current speed and time elapsed
     * @param secondsElapsed    seconds elapsed during runtime
     */
    public void move(float secondsElapsed) {
        sX = sX + secondsElapsed * sSpeedX;
        sY = sY + secondsElapsed * sSpeedY;
    }

    // Collision detection functions between sprites
    public boolean topHit(Sprite s)   { return isMovingUp() && topOverlaps(s) && widthOverlap(s); }
    public boolean bottomHit(Sprite s){ return isMovingDown() && bottomOverlaps(s) && widthOverlap(s); }
    public boolean leftHit(Sprite s)  { return isMovingLeft() && leftOverlaps(s) && heightOverlap(s); }
    public boolean rightHit(Sprite s) { return isMovingRight() && rightOverlaps(s) && heightOverlap(s); }
    public boolean hit(Sprite s)      { return topHit(s) || bottomHit(s) || leftHit(s) || rightHit(s); }

    public boolean reboundOff(Sprite s) {

        boolean moved = false;

        // If the ball hits the sides of other objects, then it moves along the X axis
        // If ball hits top or bottom of other objects, then it moves along the Y axis
        // If it hits a corner, it can move along both axis (Most common occurence)

        // Check the amount of horizontal and vertical overlap
        // and rebound off the object in favour of the direction with the smallest overlap
        // or if the overlaps are the same, rebound off both coords

        // horizontal overlap is from left or right overlap
        // we want to find the smallest overlap, so use Float.MAX_VALUE as the "no overlap" value
        float horizontalOverlap =
                leftHit(s)? leftOverlap(s):
                        rightHit(s)? rightOverlap(s):
                                Float.MAX_VALUE;

        // vertical overlap is from top or bottom overlap
        float verticalOverlap =
                topHit(s)? topOverlap(s) :
                        bottomHit(s)? bottomOverlap(s) :
                                Float.MAX_VALUE;

        // if horizontal overlap is smaller,
        // it was a horizontal hit with a pre-existing vertical overlap,
        // so bounce horizontally
        if ( horizontalOverlap < verticalOverlap ) {
            // change speed to negative value of itself. E.g. xSpeed = -50 -> moveX() -> xSpeed = 50
            moveX();
            moved = true;
        }
        // if vertical overlap is smaller,
        // it was a vertical hit with a pre-existing horizontal overlap,
        // so bounce vertically
        if ( verticalOverlap < horizontalOverlap ) {
            moveY();
            moved = true;
        }
        // both are the same, so if they are valid,
        // it was a corner hit, so bounce both ways
        else if ( horizontalOverlap != Float.MAX_VALUE ) {
            moveX();
            moveY();
            moved = true;
        }
        return moved;
    }

}
