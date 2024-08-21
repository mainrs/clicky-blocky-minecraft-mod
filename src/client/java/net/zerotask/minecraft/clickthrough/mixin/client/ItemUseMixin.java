package net.zerotask.minecraft.clickthrough.mixin.client;

import net.minecraft.block.*;
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
import net.zerotask.minecraft.clickthrough.client.AttachedBlockExtKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(MinecraftClient.class)
public class ItemUseMixin {

    // Required to check if the player is actually looking at an item frame during the interaction event.
    @Shadow
    public HitResult crosshairTarget;

    // Required to check for sneaking.
    @Shadow
    public ClientPlayerEntity player;

    // Required to get the block or block entity at the player's crosshair position.
    @Shadow
    public ClientWorld world;

    private static final Set<Class<? extends Block>> SUPPORTED_BLOCKS = Set.of(
            AnvilBlock.class,
            CartographyTableBlock.class,
            CraftingTableBlock.class,
            GrindstoneBlock.class,
            LoomBlock.class,
            SmithingTableBlock.class,
            StonecutterBlock.class
    );

    private static final Set<Class<? extends BlockEntity>> SUPPORTED_BLOCK_ENTITIES = Set.of(
            BarrelBlockEntity.class,
            BlastFurnaceBlockEntity.class,
            BrewingStandBlockEntity.class,
            ChestBlockEntity.class,
            CrafterBlockEntity.class,
            EnchantingTableBlockEntity.class,
            EnderChestBlockEntity.class,
            FurnaceBlockEntity.class,
            JukeboxBlockEntity.class,
            ShulkerBoxBlockEntity.class,
            SmokerBlockEntity.class
    );

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

                    // Calculate the potential block that we will redirect the crosshair to.
                    BlockPos containerBlockPos = AttachedBlockExtKt.getPossibleAttachedBlock(itemFrameEntity);

                    if (isSupportedRedirectionTargetAvailable(containerBlockPos)) {
                        // We found an item frame that is attached to some kind of interaction target. Trigger a
                        // crosshair change to focus the new target behind the item frame.
                        this.crosshairTarget = new BlockHitResult(crosshairTarget.getPos(), itemFrameEntity.getHorizontalFacing(), containerBlockPos, false);
                    }
                }
            }
        }
    }

    @Unique
    private boolean isSupportedRedirectionTargetAvailable(BlockPos pos) {
        return isSupportedBlockAt(pos) || isSupportedBlockEntityAt(pos);
    }

    @Unique
    private boolean isSupportedBlockAt(BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return SUPPORTED_BLOCKS.contains(block.getClass());
    }

    @Unique
    private boolean isSupportedBlockEntityAt(BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        assert blockEntity != null;

        return SUPPORTED_BLOCK_ENTITIES.contains(blockEntity.getClass());
    }
}
