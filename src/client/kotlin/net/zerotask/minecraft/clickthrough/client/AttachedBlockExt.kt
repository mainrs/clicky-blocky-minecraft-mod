package net.zerotask.minecraft.clickthrough.client

import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.util.math.BlockPos

/**
 * Returns the block position that needs to be checked for block interactions. In the case of an item frame, it is the
 * block behind the item frame.
 */
fun ItemFrameEntity.getPossibleAttachedBlock(): BlockPos =
    attachedBlockPos.offset(horizontalFacing.opposite);
