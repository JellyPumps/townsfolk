package com.bouncyjelly.townsfolk.entity.goal;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WalkToTargetGoal extends Goal {

    private static final int SEARCH_BUDGET = 6000;

    private final CitizenEntity citizen;
    private final double speed;
    @Nullable private BlockPos current_target;

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
        return citizen.getWalkTarget() != null && !citizen.navigator().isIdle();
    }

    @Override
    public void start() {
        BlockPos target = citizen.getWalkTarget();
        current_target = target;
        if (target == null) return;
        if (!citizen.navigator().moveTo(target, speed, SEARCH_BUDGET)) {
            Townsfolk.LOGGER.warn("{} cannot reach {}: {}", citizen.getName().getString(),
                    target.toShortString(), citizen.navigator().lastFailure());
        }
    }

    @Override
    public void tick() {
        BlockPos target = citizen.getWalkTarget();
        if (target != null && !target.equals(current_target)) start();
    }

    @Override
    public void stop() {
        current_target = null;
        citizen.navigator().stop();
        citizen.setWalkTarget(null);
    }
}