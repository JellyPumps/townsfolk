package com.bouncyjelly.townsfolk.entity.goal;

import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;

public class CitizenOpenDoorGoal extends DoorInteractGoal {

    private final boolean close_door;
    private int forgetTime;

    public CitizenOpenDoorGoal(MobEntity mob, boolean close_door) {
        super(mob);
        this.close_door = close_door;
    }

    @Override
    public boolean shouldContinue() {
        return this.close_door && this.forgetTime > 0 && super.shouldContinue();
    }

    @Override
    public void start() {
        this.forgetTime = 20;
        this.setDoorOpen(true);
    }

    @Override
    public void stop() {
        this.setDoorOpen(false);
    }

    @Override
    public void tick() {
        --this.forgetTime;
        super.tick();
    }
}