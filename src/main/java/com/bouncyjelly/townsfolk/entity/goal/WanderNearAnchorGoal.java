package com.bouncyjelly.townsfolk.entity.goal;

import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import com.bouncyjelly.townsfolk.path.Walkability;
import net.minecraft.entity.ai.goal.Goal;
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

    private static final int WANDER_BUDGET = 300;

    @Override
    public boolean shouldContinue() {
        return citizen.getWalkTarget() == null && !citizen.navigator().isIdle();
    }

    @Override
    public void start() {
        BlockPos anchor = citizen.getAnchor();
        int r = citizen.getWanderRadius();
        if (anchor == null || r <= 0) return;

        for (int attempt = 0; attempt < 3; attempt++) {
            BlockPos candidate = anchor.add(
                    citizen.getRandom().nextBetween(-r, r), 0,
                    citizen.getRandom().nextBetween(-r, r));
            BlockPos spot = Walkability.snap(citizen.getWorld(), candidate, 2, 2);
            if (spot == null) continue;
            if (citizen.navigator().moveTo(spot, speed, WANDER_BUDGET)) return;
        }
    }

    @Override
    public void stop() {
        if (citizen.getWalkTarget() == null) citizen.navigator().stop();
    }
}
