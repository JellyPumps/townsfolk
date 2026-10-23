package com.bouncyjelly.townsfolk.entity;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.entity.goal.CitizenOpenDoorGoal;
import com.bouncyjelly.townsfolk.entity.goal.WalkToTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CitizenEntity extends PathAwareEntity {
    @Nullable
    private BlockPos walk_target;
    @Nullable private UUID citizen_id;


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

    private void checkOrphan(ServerWorld world) {
        CitizenManager manager = CitizenManager.get(world);
        if (manager.getById(citizen_id) == null && !manager.hasLoadFailed()) {
            Townsfolk.LOGGER.info("Removing orphaned citizen entity {}", getUuid());
            discard();
        }
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void tickCramming() {}
    public void setWalkTarget(@Nullable BlockPos pos) { this.walk_target = pos; }
    @Nullable
    public BlockPos getWalkTarget() { return this.walk_target; }

    @Nullable public UUID getCitizenId() { return citizen_id; }

    public void bindToCitizen(Citizen citizen) {
        this.citizen_id = citizen.getId();
        this.setCustomName(Text.literal(citizen.getName()));
        this.setCustomNameVisible(true);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (citizen_id != null) nbt.putUuid("CitizenId", citizen_id);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("CitizenId")) citizen_id = nbt.getUuid("CitizenId");
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        if (citizen_id != null && age == 20) checkOrphan(serverWorld);
    }

}
