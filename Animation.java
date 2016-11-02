package com.kp.game;

import android.graphics.Bitmap;

public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    // public setFrames(Bitmap[] frames);
    public Animation(Bitmap[] frames) {
        this.frames = frames;
        startTime = System.nanoTime();
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public int currentFrame() {
        return currentFrame;
    }

    public boolean playedOnce() {
        return playedOnce;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if (currentFrame == frames.length) {
            currentFrame = 0;
            playedOnce = true;
        }
    }

    public Bitmap getImage() {
        return frames[currentFrame];
    }
}
