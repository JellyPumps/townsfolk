package com.bouncyjelly.townsfolk.entity.goal;

import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WalkToTargetGoal extends Goal {
    private final CitizenEntity citizen;
    private final double speed;
    private int stuck_checks;
    private net.minecraft.util.math.Vec3d last_pos;
    @Nullable
    private BlockPos current_target;

    public WalkToTargetGoal(CitizenEntity citizen, double speed) {
        this.citizen = citizen;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return citizen.getWalkTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return citizen.getWalkTarget() != null && !citizen.getNavigation().isIdle();
    }

    @Override
    public void start() {
        BlockPos t = citizen.getWalkTarget();
        if (t != null) {
            current_target = t;
            stuck_checks = 0;
            last_pos = null;
            citizen.getNavigation().startMovingTo(t.getX() + 0.5, t.getY(), t.getZ() + 0.5, speed);
        }
    }

    @Override
    public void stop() {
        citizen.getNavigation().stop();
        citizen.setWalkTarget(null);
    }

    @Override
    public void tick() {
        BlockPos t = citizen.getWalkTarget();
        if (t != null && !t.equals(current_target)) {
            start();
            return;
        }
        if (citizen.age % 20 != 0) return;
        var pos = citizen.getPos();
        if (last_pos != null && pos.squaredDistanceTo(last_pos) < 0.01) {
            stuck_checks++;
        } else {
            stuck_checks = 0;
        }
        last_pos = pos;
        if (stuck_checks >= 5) {
            stop();
        }
    }
}
