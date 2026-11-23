package com.sovok.city.system;

import com.mojang.brigadier.Command;
import com.sovok.city.SovokCity;
import com.sovok.city.item.ModItems;
import com.sovok.city.world.CityBuilder;
import com.sovok.city.world.CityData;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Вход в игру, стартовый набор, пропаганда, комендантский час, команды. */
@EventBusSubscriber(modid = SovokCity.MODID)
public class CityEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || PlayerStats.inCity(p)) return;
        ServerLevel ow = p.server.overworld();
        CityBuilder.ensureBuilt(ow);
        CityData d = CityData.get(ow);
        PlayerStats.setWelcomed(p);
        BlockPos pl = d.plaza;
        p.teleportTo(ow, pl.getX() + 0.5, pl.getY(), pl.getZ() + 0.5, 0f, 0f);
        Econ.give(p, new ItemStack(ModItems.COUPON.get(), 3));
        Econ.giveRubles(p, 20);
        Econ.give(p, new ItemStack(ModItems.MUG.get()));
        Econ.give(p, new ItemStack(ModItems.BLACK_BREAD.get(), 2));
        Econ.chat(p, "msg.sovokcity.welcome");
        Econ.chat(p, "msg.sovokcity.places", fmt(d.ration), fmt(d.casino), fmt(d.station));
    }

    private static String fmt(BlockPos b) { return b.getX() + " " + b.getY() + " " + b.getZ(); }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        if (!e.isWasDeath()) return;
        restoreAfterDeath(e.getOriginal(), e.getEntity());
    }

    private static void restoreAfterDeath(net.minecraft.world.entity.player.Player from, net.minecraft.world.entity.player.Player to) {
        if (PlayerStats.inCity(from)) PlayerStats.setWelcomed(to);
        PlayerStats.setLastCouponDay(to, PlayerStats.lastCouponDay(from));
        PlayerStats.setJailUntil(to, PlayerStats.jailUntil(from));
        PlayerStats.setThirst(to, 100);
        PlayerStats.setBody(to, 36.6);
        PlayerStats.setColdSecs(to, 0);
        PlayerStats.setDysSecs(to, 0);
        PlayerStats.setChill(to, 0);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.isEndConquered()) return;
        if (p.getRespawnPosition() != null) return; // спим в своей кровати — не трогаем
        ServerLevel ow = p.server.overworld();
        CityData d = CityData.get(ow);
        if (!d.built) return;
        p.teleportTo(ow, d.plaza.getX() + 0.5, d.plaza.getY(), d.plaza.getZ() + 0.5, 0f, 0f);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        ServerLevel ow = server.overworld();
        long dt = ow.getDayTime() % 24000L;
        int tick = server.getTickCount();
        String key = null;
        if (tick % 4800 == 0) key = "msg.sovokcity.prop." + (1 + ow.random.nextInt(6));
        else if (dt == 12000 && tick % 2 == 0) key = "msg.sovokcity.curfew_soon";
        else if (dt == 13000 && tick % 2 == 0) key = "msg.sovokcity.curfew_now";
        else if (dt == 23000 && tick % 2 == 0) key = "msg.sovokcity.curfew_end";
        if (key == null) return;
        CityData d = CityData.get(ow);
        if (!d.built) return;
        for (ServerPlayer p : ow.players()) {
            if (d.inCity(p.position())) p.sendSystemMessage(Component.translatable(key));
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("sovok")
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    CityData d = CityData.get(p.serverLevel());
                    if (!d.built) { ctx.getSource().sendSuccess(() -> Component.literal("City not built yet"), false); return 0; }
                    Econ.chat(p, "msg.sovokcity.places", fmt(d.ration), fmt(d.casino), fmt(d.station));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("pardon").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    PlayerStats.setJailUntil(p, 1); // освободится на ближайшем тике
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("hideout").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    CityData d = CityData.get(p.serverLevel());
                    if (!d.built) return 0;
                    p.teleportTo(p.serverLevel(), d.hideout.getX() + 0.5, d.hideout.getY(), d.hideout.getZ() + 0.5, 0f, 0f);
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
