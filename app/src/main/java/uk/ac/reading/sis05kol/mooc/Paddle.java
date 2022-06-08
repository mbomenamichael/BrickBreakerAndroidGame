package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

public class Paddle extends Sprite  {

    public Paddle(Bitmap image) {
        super(image);
    }

    // moves the paddle toward the x coordinates of the touch
    public void moveTowards (float x)   {
        setSpeed(x - getX(), 0);
    }
    // if paddle reaches destination, return true else return false
    public boolean isPaddleAtDestination(float currentLocation, float destination) {

            currentLocation = Math.round(currentLocation);
            destination = Math.round(destination);
            // comparing two floats... Need to find a way to refine this so it can work.
            if  (currentLocation == destination)   {
                return true;
            }
            return false;
    }

    public void stopPaddle(float x) {
        setSpeed(0, 0);
    }
    public void stopAt(float x) {
        setSpeed(0, 0);
        setPosition(x, getY());
    }

}
