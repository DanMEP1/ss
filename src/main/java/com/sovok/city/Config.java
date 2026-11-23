package com.sovok.city;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> UPGRADER_ID = B
            .comment("ID предмета Upgrader из мода Upgrader Items (проверьте через F3+H в игре)")
            .define("upgraderItemId", "upgrader_items:upgrader");
    public static final ModConfigSpec.IntValue UPGRADER_PRICE = B
            .comment("Цена Upgrader у крупье подпольного казино (в рублях)")
            .defineInRange("upgraderPrice", 40, 1, 10000);
    public static final ModConfigSpec.IntValue JAIL_SECONDS = B
            .comment("Срок ареста в секундах")
            .defineInRange("jailSeconds", 180, 10, 3600);
    public static final ModConfigSpec.IntValue PATROL_COUNT = B
            .comment("Сколько милиционеров патрулирует город")
            .defineInRange("patrolCount", 8, 1, 40);
    public static final ModConfigSpec.BooleanValue CURFEW = B
            .comment("Комендантский час: ночью на улице города нельзя находиться")
            .define("curfew", true);

    public static final ModConfigSpec SPEC = B.build();
}
