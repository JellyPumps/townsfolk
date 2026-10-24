package com.bouncyjelly.townsfolk.path;

import com.bouncyjelly.townsfolk.Townsfolk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class Walkability {

    public static final TagKey<Block> AVOID =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(Townsfolk.MOD_ID, "avoid"));
    public static final TagKey<Block> OPENABLE =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(Townsfolk.MOD_ID, "openable"));

    public enum Space { CLEAR, OPENABLE, BLOCKED }

    private Walkability() {}

    private static boolean readable(World world, BlockPos pos) {
        return !world.isOutOfHeightLimit(pos) && world.isChunkLoaded(pos);
    }

    public static Space space(World world, BlockPos pos) {
        if (!readable(world, pos)) return Space.BLOCKED;
        BlockState state = world.getBlockState(pos);
        if (state.isIn(AVOID)) return Space.BLOCKED;
        if (!state.getFluidState().isEmpty()) return Space.BLOCKED;

        VoxelShape shape = state.getCollisionShape(world, pos);
        boolean low = shape.isEmpty() || shape.getMax(Direction.Axis.Y) <= 0.5;

        if (state.isIn(OPENABLE)) {
            if (!state.contains(Properties.OPEN)) return Space.BLOCKED;
            if (state.get(Properties.OPEN) || low) return Space.CLEAR;
            return Space.OPENABLE;
        }
        if (state.isIn(BlockTags.CLIMBABLE)) return Space.CLEAR;
        return low ? Space.CLEAR : Space.BLOCKED;
    }
    
    public static boolean supports(World world, BlockPos floor_pos) {
        if (!readable(world, floor_pos)) return false;
        BlockState state = world.getBlockState(floor_pos);
        if (state.isIn(AVOID) || state.isIn(BlockTags.CLIMBABLE)) return false;
        if (!state.getFluidState().isEmpty()) return false;
        VoxelShape shape = state.getCollisionShape(world, floor_pos);
        if (shape.isEmpty()) return false;
        double top = shape.getMax(Direction.Axis.Y);
        return top > 0.5 && top <= 1.0;
    }

    public static boolean isClimbable(World world, BlockPos pos) {
        return readable(world, pos) && world.getBlockState(pos).isIn(BlockTags.CLIMBABLE);
    }

    public static boolean standable(World world, BlockPos p) {
        if (space(world, p) == Space.BLOCKED || space(world, p.up()) == Space.BLOCKED) return false;
        if (isClimbable(world, p)) return true;
        return supports(world, p.down());
    }

    public static double extraCost(World world, BlockPos p) {
        return (space(world, p) == Space.OPENABLE ? 2.0 : 0.0)
                + (space(world, p.up()) == Space.OPENABLE ? 2.0 : 0.0);
    }

    @Nullable
    public static BlockPos snap(World world, BlockPos pos, int down, int up) {
        for (int r = 0; r <= 1; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    for (int i = 0; i <= Math.max(down, up); i++) {
                        if (i <= up && standable(world, pos.add(dx, i, dz))) return pos.add(dx, i, dz);
                        if (i > 0 && i <= down && standable(world, pos.add(dx, -i, dz))) return pos.add(dx, -i, dz);
                    }
                }
            }
        }
        return null;
    }

    public static void setOpen(World world, BlockPos pos, boolean open) {
        BlockState state = world.getBlockState(pos);
        if (!state.isIn(OPENABLE) || !state.contains(Properties.OPEN)) return;
        if (state.get(Properties.OPEN) == open) return;
        if (state.getBlock() instanceof DoorBlock door) {
            door.setOpen(null, world, state, pos, open); // plays the door sound
        } else {
            world.setBlockState(pos, state.with(Properties.OPEN, open), Block.NOTIFY_LISTENERS);
        }
    }
}