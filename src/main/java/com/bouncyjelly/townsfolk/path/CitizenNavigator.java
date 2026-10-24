package com.bouncyjelly.townsfolk.path;

import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CitizenNavigator {
    private static final int MAX_REPATHS = 2;
    private final CitizenEntity citizen;
    private final List<BlockPos> opened_doors = new ArrayList<>();

    public CitizenNavigator(CitizenEntity citizen) {
        this.citizen = citizen;
    }

    @Nullable private List<CityPathfinder.Step> steps;
    @Nullable private BlockPos goal;
    private int index;
    private double speed;
    private int max_nodes;
    private int repaths;
    private int stuck_checks;
    @Nullable private Vec3d last_pos;
    private String last_failure = "";

    public boolean isIdle() { return steps == null; }
    public String lastFailure() { return last_failure; }

    public boolean moveTo(BlockPos target, double speed, int max_nodes) {
        this.goal = target;
        this.speed = speed;
        this.max_nodes = max_nodes;
        this.repaths = 0;
        return search();
    }

    public void stop() {
        steps = null;
        goal = null;
    }

    private boolean search() {
        if (!(citizen.getWorld() instanceof ServerWorld world) || goal == null) {
            steps = null;
            return false;
        }
        CityPathfinder.Result r = CityPathfinder.find(world, citizen.getBlockPos(), goal, max_nodes);
        stuck_checks = 0;
        last_pos = null;
        if (!r.found()) {
            steps = null;
            last_failure = r.note();
            return false;
        }
        steps = r.steps();
        index = 1;
        last_failure = "";
        return true;
    }

    public void tick() {
        if (!(citizen.getWorld() instanceof ServerWorld world)) return;
        if (!opened_doors.isEmpty() && citizen.age % 10 == 0) closeDoorsBehind(world);
        if (steps == null) return;

        if (index >= steps.size()) {
            steps = null;
            goal = null;
            return;
        }

        CityPathfinder.Step next = steps.get(index);
        BlockPos np = next.pos();

        if (!Walkability.standable(world, np)) {
            repath("route changed");
            return;
        }
        openDoorsAhead(world);

        Vec3d pos = citizen.getPos();
        double dx = np.getX() + 0.5 - pos.x;
        double dz = np.getZ() + 0.5 - pos.z;
        double dy = np.getY() - pos.y;

        if (dx * dx + dz * dz < 0.12 && Math.abs(dy) < 0.7) {
            index++;
            stuck_checks = 0;
            return;
        }

        citizen.getMoveControl().moveTo(np.getX() + 0.5, np.getY(), np.getZ() + 0.5, speed);

        if (next.move() == CityPathfinder.Move.CLIMB_UP && citizen.isClimbing()) {
            Vec3d v = citizen.getVelocity();
            citizen.setVelocity(v.x, 0.2, v.z);
        }

        if (citizen.age % 20 == 0) checkStuck();
    }

    private void checkStuck() {
        Vec3d pos = citizen.getPos();
        if (last_pos != null && pos.squaredDistanceTo(last_pos) < 0.01) stuck_checks++;
        else stuck_checks = 0;
        last_pos = pos;
        if (stuck_checks >= 3) repath("stuck");
    }

    private void repath(String why) {
        if (repaths++ >= MAX_REPATHS) {
            last_failure = "gave up after " + MAX_REPATHS + " re-plans (" + why + ")";
            steps = null;
            goal = null;
            return;
        }
        search();
    }

    private void openDoorsAhead(ServerWorld world) {
        int end = Math.min(index + 2, steps.size());
        for (int i = index; i < end; i++) {
            BlockPos p = steps.get(i).pos();
            for (BlockPos cell : new BlockPos[]{p, p.up()}) {
                if (Walkability.space(world, cell) == Walkability.Space.OPENABLE) {
                    Walkability.setOpen(world, cell, true);
                    if (!opened_doors.contains(cell)) opened_doors.add(cell);
                }
            }
        }
    }

    private void closeDoorsBehind(ServerWorld world) {
        Iterator<BlockPos> it = opened_doors.iterator();
        while (it.hasNext()) {
            BlockPos d = it.next();
            double distSq = citizen.getPos().squaredDistanceTo(d.getX() + 0.5, d.getY() + 0.5, d.getZ() + 0.5);
            boolean occupied = !world.getOtherEntities(null, new Box(d), e -> e instanceof LivingEntity).isEmpty();
            if (distSq > 12.25 && !occupied) { // more than 3.5 blocks away
                Walkability.setOpen(world, d, false);
                it.remove();
            }
        }
    }
}
