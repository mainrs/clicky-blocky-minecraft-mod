package net.zerotask.minecraft.clickthrough

import net.minecraft.util.math.BlockPos

interface IsAttachableToBlock {
    fun getAttachedBlockPos(): BlockPos
}
