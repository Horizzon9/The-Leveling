package dev.weaponleveling.server;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.network.UpdateServerSettingsPayload.SettingChange;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Validates menu edits on the logical server before persisting them to the real server config. */
public final class ServerSettingsService {
    private ServerSettingsService() {
    }

    public static boolean mayConfigure(ServerPlayer player) {
        if (!ServerConfig.ALLOW_IN_GAME_CONFIGURATION.get()) {
            return false;
        }
        // Essential-hosted worlds use an integrated server. Its owner is the only player allowed
        // to change shared gameplay settings, even when a guest happens to have elevated commands.
        if (player.server.isSingleplayer()) {
            return player.server.isSingleplayerOwner(player.getGameProfile());
        }
        // Dedicated and externally hosted servers keep their normal server-operator policy.
        return player.hasPermissions(ServerConfig.COMMAND_PERMISSION_LEVEL.get());
    }

    public static ServerSettingsPayload snapshot(ServerPlayer player) {
        return ServerSettingsPayload.fromServer(mayConfigure(player));
    }

    /** @return null on success, otherwise a short translatable-message suffix. */
    public static String apply(ServerPlayer player, List<SettingChange> changes) {
        if (!mayConfigure(player)) {
            return "permission";
        }
        List<ValidatedChange> validated = new ArrayList<>(changes.size());
        try {
            for (SettingChange change : changes) {
                validated.add(validate(change));
            }
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
        for (ValidatedChange change : validated) {
            change.apply();
        }
        ServerConfig.SPEC.save();
        for (ServerPlayer onlinePlayer : player.server.getPlayerList().getPlayers()) {
            LegacyIdentityService.refreshInventory(onlinePlayer);
        }
        return null;
    }

    private static ValidatedChange validate(SettingChange change) {
        String key = change.key().trim();
        String value = change.value().trim();
        return switch (key) {
            case "max_level" -> {
                // Lowering is allowed: items above the new cap keep their stored level, and every
                // level-scaled bonus is capped at maxLevel, so raising the cap again restores them.
                int parsed = parseInt(value, 1, 10_000);
                yield new ValidatedChange(() -> ServerConfig.MAX_LEVEL.set(parsed));
            }
            case "base_xp" -> {
                long parsed = parseLong(value, 1, 1_000_000_000_000L);
                long raw = Math.max(1, Math.round(parsed / ServerConfig.XP_REQUIREMENT_MULTIPLIER.get()));
                yield new ValidatedChange(() -> ServerConfig.BASE_XP.set(raw));
            }
            case "growth_factor" -> {
                double parsed = parseDouble(value, 1, 100);
                yield new ValidatedChange(() -> ServerConfig.GROWTH_FACTOR.set(parsed));
            }
            case "allow_creative_progress" -> booleanChange(value, ServerConfig.ALLOW_CREATIVE_PROGRESS::set);
            case "preserve_broken_items" -> booleanChange(value, ServerConfig.PRESERVE_BROKEN_ITEMS::set);
            case "auto_replace_broken" -> booleanChange(value, ServerConfig.AUTO_REPLACE_BROKEN::set);
            case "relic_level_percent" -> {
                int parsed = parseInt(value, 1, 100);
                yield new ValidatedChange(() -> ServerConfig.RELIC_LEVEL_PERCENT.set(parsed));
            }
            case "legacy_level_percent" -> {
                int parsed = parseInt(value, 1, 100);
                yield new ValidatedChange(() -> ServerConfig.LEGACY_LEVEL_PERCENT.set(parsed));
            }
            case "kill_xp_enabled" -> booleanChange(value, ServerConfig.KILL_XP_ENABLED::set);
            case "player_kill_xp_enabled" -> booleanChange(value, ServerConfig.PLAYER_KILL_XP_ENABLED::set);
            case "kill_bonus" -> doubleChange(value, 0, 1_000_000, ServerConfig.KILL_BONUS::set);
            case "max_xp_per_attack" -> {
                long parsed = parseLong(value, 0, Long.MAX_VALUE);
                yield new ValidatedChange(() -> ServerConfig.MAX_XP_PER_ATTACK.set(parsed));
            }
            case "armor_leveling_enabled" -> booleanChange(value, ServerConfig.ARMOR_LEVELING_ENABLED::set);
            case "armor_xp_per_damage" -> doubleChange(value, 0, 1_000_000,
                    ServerConfig.ARMOR_XP_PER_DAMAGE::set);
            case "armor_max_xp_per_damage_event" -> {
                long parsed = parseLong(value, 0, Long.MAX_VALUE);
                yield new ValidatedChange(() -> ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.set(parsed));
            }
            case "armor_level_bonus_enabled" -> booleanChange(value, ServerConfig.ARMOR_LEVEL_BONUS_ENABLED::set);
            case "armor_flat_bonus_per_level" -> doubleChange(value, 0, 1_000_000,
                    ServerConfig.ARMOR_FLAT_BONUS_PER_LEVEL::set);
            case "armor_max_level_bonus" -> doubleChange(value, 0, 1_000_000,
                    ServerConfig.ARMOR_MAX_LEVEL_BONUS::set);
            case "level_bonus_enabled" -> booleanChange(value, ServerConfig.LEVEL_DAMAGE_BONUS_ENABLED::set);
            case "level_max_multiplier" -> doubleChange(value, 1, 1_000_000,
                    ServerConfig.LEVEL_DAMAGE_MAX_MULTIPLIER::set);
            case "level_curve_exponent" -> doubleChange(value, 0.1, 10,
                    ServerConfig.LEVEL_DAMAGE_CURVE_EXPONENT::set);
            case "auto_detect_external_weapons" -> booleanChange(value,
                    ServerConfig.AUTO_DETECT_EXTERNAL_WEAPONS::set);
            default -> throw new IllegalArgumentException("unknown_setting");
        };
    }

    private static ValidatedChange booleanChange(String value, java.util.function.Consumer<Boolean> setter) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new IllegalArgumentException("invalid_value");
        }
        return new ValidatedChange(() -> setter.accept(Boolean.parseBoolean(value)));
    }

    private static ValidatedChange doubleChange(String value, double min, double max,
                                                java.util.function.Consumer<Double> setter) {
        double parsed = parseDouble(value, min, max);
        return new ValidatedChange(() -> setter.accept(parsed));
    }

    private static int parseInt(String value, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException("out_of_range");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid_value");
        }
    }

    private static long parseLong(String value, long min, long max) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException("out_of_range");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid_value");
        }
    }

    private static double parseDouble(String value, double min, double max) {
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed) || parsed < min || parsed > max) {
                throw new IllegalArgumentException("out_of_range");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid_value");
        }
    }

    private record ValidatedChange(Runnable action) {
        void apply() {
            action.run();
        }
    }
}
