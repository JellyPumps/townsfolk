package com.bouncyjelly.townsfolk.entity.goal;

import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class WanderNearAnchorGoal extends Goal {
    private final CitizenEntity citizen;
    private final double speed;

    public WanderNearAnchorGoal(CitizenEntity citizen, double speed) {
        this.citizen = citizen;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return citizen.canWander() && citizen.getRandom().nextInt(60) == 0;
    }

    @Override
    public boolean shouldContinue() {
        return citizen.getWalkTarget() == null && !citizen.getNavigation().isIdle();
    }

    @Override
    public void start() {
        BlockPos anchor = citizen.getAnchor();
        int r = citizen.getWanderRadius();
        if (anchor == null || r <= 0) return;

        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos candidate = anchor.add(
                    citizen.getRandom().nextBetween(-r, r), 0,
                    citizen.getRandom().nextBetween(-r, r));
            Path path = citizen.getNavigation().findPathTo(candidate, 0);
            if (path != null && path.reachesTarget()) {
                citizen.getNavigation().startMovingAlong(path, speed);
                return;
            }
        }
    }

    @Override
    public void stop() {
        citizen.getNavigation().stop();
    }
}
