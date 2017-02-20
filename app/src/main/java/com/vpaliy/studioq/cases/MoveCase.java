package com.vpaliy.studioq.cases;


public class MoveCase extends Case{

    private String destination;

    private boolean move;

    public MoveCase move() {
        this.move=true;
        return this;
    }

    @Override
    public void execute() {

    }
}
