package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Random;

public class Level  {

    private int basicBricks;
    private int powerBricks;
    private int lifeBricks;
    private Bitmap basicBrick, powerBrick, lifeBrick;
    private int nBasic, nPower, nLife;
    private Brick[] mBricks;
    private ArrayList<Brick> nBricks = new ArrayList<>();
    private Random rnd = new Random();


    ArrayList<Brick> Level(int level, Bitmap bBrick, Bitmap pBrick, Bitmap lBrick) {
        // Giving images to represent bricks.
        this.basicBrick = bBrick;
        this.powerBrick = pBrick;
        this.lifeBrick = lBrick;

        setupLevel(level);

        return nBricks;
    }

    public void setupLevel(int level)    {

        switch(level)   {
            case 1:
                basicBricks = 6;
                powerBricks = 0;
                lifeBricks = 0;
                break;

            case 2:
                basicBricks = 14;
                powerBricks = 1;
                lifeBricks = 0;
                break;
            // Level 3 is a random level with random numbers of bricks (random numbers of brick-types)
            case 3:
                basicBricks = rnd.nextInt(16);
                powerBricks = rnd.nextInt(3);
                lifeBricks = rnd.nextInt(3);
                break;

            case 4:
                basicBricks = 31;
                powerBricks = 1;
                lifeBricks = 0;
                break;

            case 5:
                basicBricks = 40;
                powerBricks = 5;
                lifeBricks = 3;
                break;
        }

        this.nBasic = basicBricks;
        this.nPower = powerBricks;
        this.nLife = lifeBricks;

        setBricks(nBasic, nPower, nLife);
    }

    /**
     * Sets bitmap images for each element in the array of bricks.
     * @param nBasicBrick   number of basic bricks
     * @param nPowerBrick   number of power bricks
     * @param nLifeBrick    number of life bricks
     */
    public void setBricks (int nBasicBrick, int nPowerBrick, int nLifeBrick)    {

        int rndBrick = 0;
        Random rnd = new Random();

        //  Add all the level's brick quantities together
        int totalBricks = nBasicBrick + nLifeBrick + nPowerBrick;

        // Set this mBricks variable to the size of totalBricks
        //this.mBricks = new Brick[totalBricks];

        // Set all bricks to basic
        for (int i = 0; i < totalBricks; i++)   {
            //mBricks[i] = new Brick(basicBrick);
            this.nBricks.add(i, new Brick(basicBrick));
        }

        for (int i = 0; i < nPowerBrick; i++)   {
            rndBrick = rnd.nextInt(totalBricks);
            //mBricks[rndBrick] = new Brick(powerBrick);
            this.nBricks.add(rndBrick, new Brick(powerBrick));
        }

        for (int i = 0; i < nLifeBrick; i++)    {
            rndBrick = rnd.nextInt(totalBricks);
            //mBricks[rndBrick] = new Brick(lifeBrick);
            this.nBricks.add(rndBrick, new Brick(lifeBrick));
        }


    }

}
