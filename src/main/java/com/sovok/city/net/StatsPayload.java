package com.sovok.city.net;

import com.sovok.city.SovokCity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Синхронизация параметров реализма с клиентом для HUD. */
public record StatsPayload(int thirst, int body10, int coldSecs, int dysSecs, int jailSecs, int alert)
        implements CustomPacketPayload {

    public static final Type<StatsPayload> TYPE = new Type<>(SovokCity.id("stats"));

    public static final StreamCodec<ByteBuf, StatsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StatsPayload::thirst,
            ByteBufCodecs.VAR_INT, StatsPayload::body10,
            ByteBufCodecs.VAR_INT, StatsPayload::coldSecs,
            ByteBufCodecs.VAR_INT, StatsPayload::dysSecs,
            ByteBufCodecs.VAR_INT, StatsPayload::jailSecs,
            ByteBufCodecs.VAR_INT, StatsPayload::alert,
            StatsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
