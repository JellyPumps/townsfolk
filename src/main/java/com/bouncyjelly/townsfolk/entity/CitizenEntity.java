package com.bouncyjelly.townsfolk.entity;

import com.bouncyjelly.townsfolk.Townsfolk;
import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.citizen.NameGenerator;
import com.bouncyjelly.townsfolk.entity.goal.CitizenOpenDoorGoal;
import com.bouncyjelly.townsfolk.entity.goal.WalkToTargetGoal;
import com.bouncyjelly.townsfolk.entity.goal.WanderNearAnchorGoal;
import com.bouncyjelly.townsfolk.path.CitizenNavigator;
import com.bouncyjelly.townsfolk.place.Place;
import com.bouncyjelly.townsfolk.place.PlaceType;
import com.bouncyjelly.townsfolk.schedule.Activity;
import com.bouncyjelly.townsfolk.schedule.ScheduleResolver;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
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
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CitizenEntity extends PathAwareEntity {
    @Nullable
    private BlockPos walk_target;
    @Nullable private UUID citizen_id;
    private int schedule_retries;
    @Nullable private Activity current_activity;
    @Nullable private BlockPos anchor;
    private int wander_radius;
    private boolean in_zone;
    private String scheduled_key = "";
    @Nullable private BlockPos bed_pos;
    private int bed_search_cooldown;
    private final CitizenNavigator navigator = new CitizenNavigator(this);

    public CitizenNavigator navigator() { return navigator; }

    public CitizenEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setPersistent();
        this.setInvulnerable(true);
        // should no longer need a condition set to path through interactables (doors, trapdoors etc).
    }

    public static DefaultAttributeContainer.Builder createCitizenAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(5, new WalkToTargetGoal(this, 1.0));
        this.goalSelector.add(6, new WanderNearAnchorGoal(this, 0.8));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        // should no longer need a separate goal for interactables.
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
    public boolean canWander() {
        return in_zone && wander_radius > 0 && anchor != null && walk_target == null && !isSleeping();
    }
    @Nullable public BlockPos getAnchor() { return anchor; }
    public int getWanderRadius() { return wander_radius; }

    @Nullable public UUID getCitizenId() { return citizen_id; }

    public void bindToCitizen(Citizen citizen) {
        this.citizen_id = citizen.getId();
        this.setCustomName(Text.literal(citizen.getName()));
        this.setCustomNameVisible(true);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (citizen_id != null) nbt.putUuid("citizen_id", citizen_id);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("citizen_id")) citizen_id = nbt.getUuid("citizen_id");
    }

    private static boolean isBedUsable(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BedBlock
                && state.get(BedBlock.PART) == BedPart.HEAD
                && !state.get(BedBlock.OCCUPIED);
    }

    @Nullable
    private static BlockPos findBed(ServerWorld world, BlockPos around) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(around.add(-8, -2, -8), around.add(8, 2, 8))) {
            if (!world.isChunkLoaded(p) || !isBedUsable(world, p)) continue;
            double d = p.getSquaredDistance(around);
            if (d < bestDist) {
                bestDist = d;
                best = p.toImmutable();
            }
        }
        return best;
    }

    private void startSleeping(ServerWorld world) {
        if (bed_pos == null || !isBedUsable(world, bed_pos)) {
            bed_pos = null;
            return;
        }
        navigator.stop();
        setWalkTarget(null);
        sleep(bed_pos);
        setCustomNameVisible(false);
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isSleeping();
    }

    public String describeState() {
        return "activity=" + (current_activity == null ? "none" : current_activity.asString())
                + " anchor=" + (anchor == null ? "none" : anchor.toShortString())
                + " wander=" + wander_radius
                + " in_zone=" + in_zone
                + " sleeping=" + isSleeping()
                + " bed=" + (bed_pos == null ? "none" : bed_pos.toShortString())
                + " walk_target=" + (walk_target == null ? "none" : walk_target.toShortString())
                + " nav=" + navigator.describe();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        if (age == 20) {
            if (citizen_id == null) adoptNewRecord(serverWorld);
            else checkOrphan(serverWorld);
        }
        navigator.tick();
        tickSchedule(serverWorld);
    }

    private void adoptNewRecord(ServerWorld world) {
        CitizenManager manager = CitizenManager.get(world);
        Citizen citizen = manager.create(NameGenerator.pick(manager, world.getRandom()));
        bindToCitizen(citizen);
    }

    private void tickSchedule(ServerWorld world) {
        if (citizen_id == null || (age + getId()) % 20 != 0) return;
        Citizen citizen = CitizenManager.get(world).getById(citizen_id);
        if (citizen == null) return;

        ScheduleResolver.Destination dest = ScheduleResolver.resolve(world, citizen);
        if (dest == null || dest.place() == null) return;
        Place place = dest.place();
        Activity activity = dest.activity();

        if (isSleeping() && activity != Activity.SLEEP) {
            wakeUp();
            setCustomNameVisible(true);
        }
        if (isSleeping()) return;

        current_activity = activity;
        anchor = place.pos();
        wander_radius = place.type() == PlaceType.SHOP_COUNTER ? 0 : activity.wanderRadius();

        BlockPos target = place.pos();
        double zone = Math.max(3.0, wander_radius + 2.0);

        if (activity == Activity.SLEEP) {
            if (bed_search_cooldown > 0) bed_search_cooldown--;
            if (bed_pos != null && !isBedUsable(world, bed_pos)) bed_pos = null;
            if (bed_pos == null && bed_search_cooldown == 0) {
                bed_pos = findBed(world, place.pos());
                if (bed_pos == null) bed_search_cooldown = 30;
            }
            if (bed_pos != null) {
                target = bed_pos;
                zone = 2.0;
            }
        }

        in_zone = getBlockPos().getSquaredDistance(target) <= zone * zone;

        String key = place.id() + ":" + activity.asString();
        boolean changed = !key.equals(scheduled_key);
        if (changed) {
            scheduled_key = key;
            schedule_retries = 0;
        }

        if (activity == Activity.SLEEP && bed_pos != null && in_zone) {
            startSleeping(world);
            return;
        }

        // placeholder animation
        if (activity == Activity.WORK && in_zone && navigator.isIdle()
                && getRandom().nextInt(4) == 0) {
            swingHand(Hand.MAIN_HAND);
        }

        if (in_zone) {
            schedule_retries = 0;
            return;
        }

        boolean idle = walk_target == null && navigator.isIdle();
        if (changed || (idle && schedule_retries < 3)) {
            if (!changed) schedule_retries++;
            setWalkTarget(target);
        }
    }
}
