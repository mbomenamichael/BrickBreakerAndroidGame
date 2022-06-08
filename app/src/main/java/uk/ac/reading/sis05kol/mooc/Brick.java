package uk.ac.reading.sis05kol.mooc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Brick extends Sprite {


    public enum BrickType {
        BASIC, LIFE, POWERPOINT
    }


    public Brick(Bitmap image) {
        super(image);
    }

}

/*
new Thread((Runnable) () -> {

}).start();
*/