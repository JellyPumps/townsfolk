package com.bouncyjelly.townsfolk.item;

import com.bouncyjelly.townsfolk.citizen.Citizen;
import com.bouncyjelly.townsfolk.citizen.CitizenManager;
import com.bouncyjelly.townsfolk.entity.CitizenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class CitizenConfigItem extends Item {

    public CitizenConfigItem(Settings settings) {
        super(settings);
    }

    public static boolean canConfigure(ServerPlayerEntity player) {
        return player.hasPermissionLevel(2) || player.isCreative();
    }

    public static boolean isHolding(ServerPlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof CitizenConfigItem
                || player.getOffHandStack().getItem() instanceof CitizenConfigItem;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof CitizenEntity citizenEntity)) return ActionResult.PASS;
        if (user.getWorld().isClient) return ActionResult.SUCCESS;
        if (!(user instanceof ServerPlayerEntity player)) return ActionResult.PASS;

        if (!canConfigure(player)) {
            player.sendMessage(Text.literal("You don't have permission to configure citizens."), true);
            return ActionResult.FAIL;
        }
        if (citizenEntity.getCitizenId() == null) {
            player.sendMessage(Text.literal("This citizen has no record. Use /townsfolk create instead."), true);
            return ActionResult.FAIL;
        }
        Citizen record = CitizenManager.get(player.getServerWorld()).getById(citizenEntity.getCitizenId());
        if (record == null) {
            player.sendMessage(Text.literal("This citizen's record is missing."), true);
            return ActionResult.FAIL;
        }

        // temporary, replaced in step 3
        player.sendMessage(Text.literal("Citizen: " + record.getName()), false);
        return ActionResult.SUCCESS;
    }
}