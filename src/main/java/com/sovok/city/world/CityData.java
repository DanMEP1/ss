package com.sovok.city.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Сохраняемые данные города: где что стоит, построен ли он. */
public class CityData extends SavedData {
    public static final int RADIUS = 48;

    public boolean built = false;
    public BlockPos center = BlockPos.ZERO;
    public BlockPos plaza = BlockPos.ZERO;
    public BlockPos jail = BlockPos.ZERO;
    public BlockPos raidPoint = BlockPos.ZERO;
    public BlockPos ration = BlockPos.ZERO;
    public BlockPos casino = BlockPos.ZERO;
    public BlockPos station = BlockPos.ZERO;
    public BlockPos hideout = BlockPos.ZERO;
    public BlockPos illMin = BlockPos.ZERO;
    public BlockPos illMax = BlockPos.ZERO;

    public static CityData get(ServerLevel any) {
        ServerLevel overworld = any.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CityData::new, CityData::load, null), "sovokcity_city");
    }

    public boolean inCity(Vec3 v) {
        return built && Math.abs(v.x - center.getX()) <= RADIUS && Math.abs(v.z - center.getZ()) <= RADIUS;
    }

    public boolean inIllegal(Vec3 v) {
        if (!built) return false;
        AABB box = new AABB(illMin.getX(), illMin.getY(), illMin.getZ(),
                illMax.getX() + 1, illMax.getY() + 1, illMax.getZ() + 1);
        return box.contains(v);
    }

    private static void put(CompoundTag t, String k, BlockPos p) { t.putLong(k, p.asLong()); }
    private static BlockPos read(CompoundTag t, String k) { return t.contains(k) ? BlockPos.of(t.getLong(k)) : BlockPos.ZERO; }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider provider) {
        t.putBoolean("built", built);
        put(t, "center", center); put(t, "plaza", plaza); put(t, "jail", jail);
        put(t, "raid", raidPoint); put(t, "ration", ration); put(t, "casino", casino);
        put(t, "station", station); put(t, "hideout", hideout);
        put(t, "illMin", illMin); put(t, "illMax", illMax);
        return t;
    }

    public static CityData load(CompoundTag t, HolderLookup.Provider provider) {
        CityData d = new CityData();
        d.built = t.getBoolean("built");
        d.center = read(t, "center"); d.plaza = read(t, "plaza"); d.jail = read(t, "jail");
        d.raidPoint = read(t, "raid"); d.ration = read(t, "ration"); d.casino = read(t, "casino");
        d.station = read(t, "station"); d.hideout = read(t, "hideout");
        d.illMin = read(t, "illMin"); d.illMax = read(t, "illMax");
        return d;
    }
}
