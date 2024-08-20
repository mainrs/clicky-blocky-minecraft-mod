package net.zerotask.minecraft.clickthrough.mixin.client;

import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ItemUseMixin {

    // Keeps track of what the user points their crosshair at.
    @Shadow
    public HitResult crosshairTarget;

    // The player entity itself.
    @Shadow
    public ClientPlayerEntity player;
    @Shadow
    public ClientWorld world;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    public void onItemUse_CheckForNewCrosshairTarget(CallbackInfo callbackInfo) {
        this.checkForNewCrosshairTarget();
    }

    @Unique
    private void checkForNewCrosshairTarget() {
        if (crosshairTarget == null) {
            return;
        }

        // Check if the player is aiming at an item frame that is attached to one of the allowed blocks.
        if (crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
            Entity entity = entityHitResult.getEntity();

            if (entity instanceof ItemFrameEntity) {
                if (!player.isSneaking()) {
                    ItemFrameEntity itemFrameEntity = (ItemFrameEntity) entity;

                    // Calculate the potential storage block position.
                    BlockPos containerBlockPos = itemFrameEntity.getAttachedBlockPos().offset(itemFrameEntity.getHorizontalFacing().getOpposite());

                    if (isSupportedBlockAt(containerBlockPos)) {
                        // We found an item frame that is attached to a storage block. Trigger a crosshair change
                        // to focus the container behind the item frame.
                        this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), itemFrameEntity.getHorizontalFacing(), containerBlockPos, false);
                    }
                }
            }
        }

        // Other items that we support are implemented as block entities and thus need to be checked against a block
        // hit target.
    }

    @Unique
    private boolean isSupportedBlockAt(BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        // The ender chest is not part of the lockable/lootable container block entity family and thus requires a
        // manual check.
        //
        // The same goes for the other blocks that are interactable, but do not share a common parent class.
        return blockEntity instanceof LockableContainerBlockEntity || blockEntity instanceof EnderChestBlockEntity || blockEntity instanceof JukeboxBlockEntity;
    }
}
