package dev.khanhtimn.efcompat;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue AUTO_SWITCH_ENABLED;
    public static final ModConfigSpec.BooleanValue USE_WEAPON_CATEGORY_DETECTION;
    public static final ModConfigSpec.BooleanValue ENFORCE_MODE_ON_TOGGLE;
    public static final ModConfigSpec.BooleanValue VANILLA_FIRST_PERSON_FOR_NON_COMBAT;
    public static final ModConfigSpec.BooleanValue PUNCHY_IDLE_OVERRIDE;

    static final ModConfigSpec SPEC;

    static {
        BUILDER.translation("config.efcompat.mode_switching").push("mode_switching");

        AUTO_SWITCH_ENABLED = BUILDER
                .comment("\nAutomatically switch to Epic Fight mode when equipping a combat weapon,",
                        "and to vanilla mode when equipping a non-combat item.",
                        "Respects the 'canSwitchPlayerMode' gamerule.",
                        "\nDefault: true")
                .translation("config.efcompat.auto_switch_enabled")
                .define("auto_switch_enabled", true);

        USE_WEAPON_CATEGORY_DETECTION = BUILDER
                .comment("\nUse Epic Fight's WeaponCategory system to auto-detect combat weapons.",
                        "This catches modded weapons registered via class hierarchy, regex, or data packs.",
                        "When disabled, only Epic Fight's manual config lists are used for detection.",
                        "\nDefault: true")
                .translation("config.efcompat.use_weapon_category_detection")
                .define("use_weapon_category_detection", true);

        ENFORCE_MODE_ON_TOGGLE = BUILDER
                .comment("\nPrevent manual mode toggle (default: R key) from switching to an",
                        "inappropriate mode. Combat weapon forces Epic Fight, otherwise forces vanilla.",
                        "Respects the 'canSwitchPlayerMode' gamerule.",
                        "\nDefault: true")
                .translation("config.efcompat.enforce_mode_on_toggle")
                .define("enforce_mode_on_toggle", true);

        BUILDER.pop();

        BUILDER.translation("config.efcompat.rendering").push("rendering");

        VANILLA_FIRST_PERSON_FOR_NON_COMBAT = BUILDER
                .comment("\nUse vanilla first-person hand rendering when holding a non-combat item",
                        "while in Epic Fight mode. Prevents the Epic Fight animated hand model",
                        "from showing for items like torches, food, or blocks.",
                        "\nDefault: true")
                .translation("config.efcompat.vanilla_first_person_for_non_combat")
                .define("vanilla_first_person_for_non_combat", true);

        PUNCHY_IDLE_OVERRIDE = BUILDER
                .comment("\nWhen Punchy is installed, let Punchy handle first-person arm rendering",
                        "during idle states in Epic Fight combat mode.",
                        "Epic Fight retains control during attacks, blocking, and other combat actions.",
                        "Punchy features like weapon inspection still work during idle.",
                        "\nDefault: true")
                .translation("config.efcompat.punchy_idle_override")
                .define("punchy_idle_override", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
