package com.waterfairy.widget.baseView;

/**
 * Created by water_fairy on 2017/6/13.
 * 995637517@qq.com
 */

public class Coordinate {

    public Coordinate() {

    }

    public Coordinate(float x, float y) {
        this.x = x;
        this.y = y;

    }

    public Coordinate setExtra(float extra) {
        this.extra = extra;
        return this;
    }

    public float getX() {
        return x;
    }

    public Coordinate setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Coordinate setY(float y) {
        this.y = y;
        return this;
    }

    public float getExtra() {
        return extra;
    }

    public float extra;
    public float x;
    public float y;
}
