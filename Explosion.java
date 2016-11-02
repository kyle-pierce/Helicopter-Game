package com.kp.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Explosion {
    private int x;
    private int y;
    private int height;
    private int width;
    private int row;
    private Animation animation;

    public Explosion(Bitmap res, int x, int y, int width, int height, int numFrames) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        Bitmap[] image = new Bitmap[numFrames];

        for (int i = 0; i < numFrames; i++) {
            if (i % 5 == 0 && i > 0) {
                row++;
            }
            image[i] = Bitmap.createBitmap(res, (i - (5 * row)) * width, row * height, width, height);
        }
        animation = new Animation(image);
        animation.setDelay(10);
    }

    public void update() {
        if (!animation.playedOnce()) {
            animation.update();
        }
    }

    public void draw(Canvas canvas) {
        if (!animation.playedOnce()) {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }
    }

    public int getHeight() {
        return height;
    }
}

