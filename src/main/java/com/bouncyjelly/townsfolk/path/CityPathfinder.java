package com.bouncyjelly.townsfolk.path;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CityPathfinder {
    private CityPathfinder() {}

    public enum Move { START, WALK, DIAGONAL, STEP_UP, DROP, CLIMB_UP, CLIMB_DOWN }
    public record Step(BlockPos pos, Move move) {}
    public record Result(@Nullable List<Step> steps, int expanded, long micros, String note) {
        public boolean found() { return steps != null; }
    }
    private record Edge(BlockPos pos, Move move, double cost) {}

    private static final class Node {
        final BlockPos pos;
        final Move move;
        final Node parent;
        final double g;
        final double f;

        Node(BlockPos pos, Move move, Node parent, double g, double f) {
            this.pos = pos;
            this.move = move;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }

    private static final int MAX_DROP = 3;
    private static final double HEURISTIC_WEIGHT = 1.2;

    private static Result fail(String why, int expanded, long t0) {
        return new Result(null, expanded, (System.nanoTime() - t0) / 1000, why);
    }

    private static Result success(Node end, int expanded, long t0) {
        ArrayDeque<Step> reversed = new ArrayDeque<>();
        for (Node n = end; n != null; n = n.parent) reversed.addFirst(new Step(n.pos, n.move));
        return new Result(new ArrayList<>(reversed), expanded, (System.nanoTime() - t0) / 1000, "ok");
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        double dx = Math.abs(a.getX() - b.getX());
        double dz = Math.abs(a.getZ() - b.getZ());
        double dy = Math.abs(a.getY() - b.getY());
        return HEURISTIC_WEIGHT * (Math.max(dx, dz) + 0.414 * Math.min(dx, dz) + dy);
    }

    private static boolean cleanWalk(ServerWorld world, BlockPos n) {
        return Walkability.standable(world, n)
                && Walkability.space(world, n) == Walkability.Space.CLEAR
                && Walkability.space(world, n.up()) == Walkability.Space.CLEAR;
    }

    private static void expand(ServerWorld world, BlockPos p, List<Edge> out) {
        out.clear();
        var blocked = Walkability.Space.BLOCKED;

        for (Direction d : Direction.Type.HORIZONTAL) {
            BlockPos n = p.offset(d);

            if (Walkability.standable(world, n)) {
                out.add(new Edge(n, Move.WALK, 1.0 + Walkability.extraCost(world, n)));
                continue;
            }

            BlockPos up = n.up();
            if (Walkability.space(world, p.up(2)) != blocked && Walkability.standable(world, up)) {
                out.add(new Edge(up, Move.STEP_UP, 1.6 + Walkability.extraCost(world, up)));
                continue;
            }

            if (Walkability.space(world, n) != blocked && Walkability.space(world, n.up()) != blocked) {
                for (int drop = 1; drop <= MAX_DROP; drop++) {
                    BlockPos m = n.down(drop);
                    if (Walkability.standable(world, m)) {
                        out.add(new Edge(m, Move.DROP, 1.0 + 0.6 * drop));
                        break;
                    }
                    if (Walkability.space(world, m) == blocked) break;
                }
            }
        }

        for (Direction a : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
            for (Direction b : new Direction[]{Direction.EAST, Direction.WEST}) {
                BlockPos na = p.offset(a);
                BlockPos nb = p.offset(b);
                BlockPos diag = na.offset(b);
                if (cleanWalk(world, na) && cleanWalk(world, nb) && cleanWalk(world, diag)) {
                    out.add(new Edge(diag, Move.DIAGONAL, 1.45));
                }
            }
        }

        BlockPos above = p.up();
        if (Walkability.isClimbable(world, p) && Walkability.isClimbable(world, above)
                && Walkability.space(world, above) != blocked
                && Walkability.space(world, above.up()) != blocked) {
            out.add(new Edge(above, Move.CLIMB_UP, 1.3));
        }
        BlockPos below = p.down();
        if (Walkability.isClimbable(world, below) && Walkability.space(world, below) != blocked) {
            out.add(new Edge(below, Move.CLIMB_DOWN, 1.0));
        }
    }

    public static Result find(ServerWorld world, BlockPos raw_start, BlockPos raw_goal, int max_nodes) {
        long t0 = System.nanoTime();
        BlockPos start = Walkability.snap(world, raw_start, 1, 1);
        if (start == null) return fail("start is not standable", 0, t0);
        BlockPos goal = Walkability.snap(world, raw_goal, 3, 2);
        if (goal == null) return fail("goal is not standable (or chunk is unloaded)", 0, t0);

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble((Node n) -> n.f));
        Map<Long, Double> best_g = new HashMap<>();
        Set<Long> closed = new HashSet<>();
        List<Edge> edges = new ArrayList<>(24);

        open.add(new Node(start, Move.START, null, 0, heuristic(start, goal)));
        best_g.put(start.asLong(), 0.0);
        int expanded = 0;

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (!closed.add(current.pos.asLong())) continue;
            if (current.pos.equals(goal)) return success(current, expanded, t0);
            if (++expanded > max_nodes) return fail("search budget exhausted (" + max_nodes + " nodes)", expanded, t0);

            expand(world, current.pos, edges);
            for (Edge e : edges) {
                long key = e.pos.asLong();
                if (closed.contains(key)) continue;
                double g = current.g + e.cost();
                Double known = best_g.get(key);
                if (known != null && known <= g) continue;
                best_g.put(key, g);
                open.add(new Node(e.pos(), e.move(), current, g, g + heuristic(e.pos(), goal)));
            }
        }
        return fail("no route exists", expanded, t0);
    }
}
