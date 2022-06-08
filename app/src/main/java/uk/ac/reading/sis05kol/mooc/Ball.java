package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

public class Ball extends Sprite    {

    private float bRadius = 0;


    public Ball(Bitmap image)   {
        super(image);
        bRadius = image.getWidth()/2;
    }
    private boolean isTouching(Ball b)    {

        float distanceSquared = (b.getX()-getX() * b.getX()-getX()) + (b.getY()-getY() * b.getY()-getY());
        float contactDistance = bRadius + b.bRadius;
        float contactDistanceSquared = contactDistance * contactDistance;
        return distanceSquared <= contactDistanceSquared;
    }

    // Circle to other shape collision detection



    private boolean reboundOff(Ball b) {

        boolean rebounded = isTouching(b);

        if  (rebounded) {

            b.setSpeed(-b.getSpeedX(), -b.getSpeedY());
        }
        return rebounded;

    }

}
