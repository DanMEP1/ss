package com.sovok.city.block;

import com.sovok.city.item.ModItems;
import com.sovok.city.system.Econ;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Продмаг: 1 талон = 1 продукт по госцене. */
public class FoodStoreBlock extends Block {
    public FoodStoreBlock(Properties p) { super(p); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!Econ.take(player, ModItems.COUPON.get(), 1)) {
            Econ.msg(player, "msg.sovokcity.need_coupon");
            return InteractionResult.CONSUME;
        }
        Item[] goods = { ModItems.BLACK_BREAD.get(), ModItems.BLACK_BREAD.get(), ModItems.TUSHONKA.get(), ModItems.KOMPOT.get() };
        Item pick = goods[level.random.nextInt(goods.length)];
        Econ.give(player, new ItemStack(pick));
        Econ.msg(player, "msg.sovokcity.bought_ration");
        return InteractionResult.CONSUME;
    }
}
