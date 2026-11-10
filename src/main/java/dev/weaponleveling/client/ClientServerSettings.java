package dev.weaponleveling.client;

import dev.weaponleveling.network.ServerSettingsPayload;
import dev.weaponleveling.core.xp.XpCurve;

/** Client cache of the authoritative settings sent by the connected server. */
public final class ClientServerSettings {
    private static volatile ServerSettingsPayload current = ServerSettingsPayload.defaults();

    private ClientServerSettings() {
    }

    public static ServerSettingsPayload current() {
        return current;
    }

    public static void update(ServerSettingsPayload settings) {
        current = settings;
    }

    public static long requiredXp(int level) {
        ServerSettingsPayload settings = current;
        if (level >= settings.maxLevel()) {
            return 0;
        }
        double growth = settings.scaleCurveWithMaxLevel()
                ? XpCurve.scaledGrowthFactor(settings.growthFactor(), settings.maxLevel())
                : settings.growthFactor();
        return new XpCurve(settings.maxLevel(), settings.baseXp(), growth, settings.xpTable()).requiredXp(level);
    }
}
