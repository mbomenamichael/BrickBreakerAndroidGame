package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Random;

public class BricksStructure {

    Bitmap basicBrick, powerBrick, lifeBrick;

    Brick[] BricksStructure(Brick[] bricksStructure, Bitmap image)    {



        return bricksStructure;
    }

    public Brick[] setBricks (Brick[] mBricks, int nBasicBrick, int nPowerBrick, int nLifeBrick) {

        Random rnd = new Random();


            //  Add all bricks together
            int totalBricks = nBasicBrick + nLifeBrick + nPowerBrick;

            // Set this mBricks variable to the size of totalBricks
            mBricks = new Brick[totalBricks];



            int rndBrick = 0;
            // Sets all bricks to basic
            for (int i = 0; i < totalBricks; i++) {
                mBricks[i] = new Brick(basicBrick);
            }

            for (int i = 0; i < nLifeBrick; i++) {
                rndBrick = rnd.nextInt(totalBricks);
                mBricks[rndBrick] = new Brick(powerBrick);
            }

            for (int i = 0; i < nPowerBrick; i++) {
                rndBrick = rnd.nextInt(totalBricks);
                mBricks[rndBrick] = new Brick(lifeBrick);
            }

            return mBricks;



    }}
