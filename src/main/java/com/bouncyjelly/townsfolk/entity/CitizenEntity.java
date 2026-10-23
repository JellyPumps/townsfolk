package com.bouncyjelly.townsfolk.entity;

import com.bouncyjelly.townsfolk.entity.goal.CitizenOpenDoorGoal;
import com.bouncyjelly.townsfolk.entity.goal.WalkToTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CitizenEntity extends PathAwareEntity {
    @Nullable
    private BlockPos walk_target;

    public CitizenEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setPersistent();
        this.setInvulnerable(true);
        ((net.minecraft.entity.ai.pathing.MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);
    }

    public static DefaultAttributeContainer.Builder createCitizenAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(5, new WalkToTargetGoal(this, 1.0));

        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.goalSelector.add(1, new CitizenOpenDoorGoal(this, true));
    }

    @Override
    public boolean isPushable() { return false; }
    @Override
    protected void tickCramming() {}

    public void setWalkTarget(@Nullable BlockPos pos) { this.walk_target = pos; }
    @Nullable
    public BlockPos getWalkTarget() { return this.walk_target; }
}
