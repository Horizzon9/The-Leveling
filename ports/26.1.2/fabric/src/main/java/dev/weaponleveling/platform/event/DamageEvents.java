package dev.weaponleveling.platform.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hasar boru hattinin kancalari - NeoForge'un `LivingIncomingDamageEvent` ve
 * `LivingDamageEvent.Pre/Post` olaylarinin bu kod tabanindaki karsiligi. Fabric API'de bu
 * asamalarin hicbirinin karsiligi yoktur; olaylar {@code LivingEntityDamageMixin} tarafindan
 * vanilla `LivingEntity` govdesinden dagitilir.
 *
 * NEDEN TEK BIR "olay nesnesi" YERINE DORT AYRI KANCA: NeoForge'un tek `LivingIncomingDamageEvent`
 * nesnesi hem iptal hem de hasar degistirme yetkisi tasir. Mixin tarafinda bu iki yetki DOGAL
 * OLARAK AYRI iki enjeksiyon noktasina denk duser (iptal metodun basinda, hasar degisikligi
 * vanilla'nin on kontrollerinden sonra). Ayirmak, tek bir olayin iki kez dagitilmasini - ve
 * dolayisiyla yan etkilerin iki kez islemesini - yapisal olarak imkansiz kilar.
 *
 * ENJEKSIYON NOKTALARI 1.21.11 BYTECODE'U ILE DOGRULANDI - bkz {@code LivingEntityDamageMixin}.
 */
public final class DamageEvents {
    private static final List<CancelIncoming> CANCEL_INCOMING = new ArrayList<>();
    private static final List<ModifyIncoming> MODIFY_INCOMING = new ArrayList<>();
    private static final List<MitigatedDamage> MITIGATED = new ArrayList<>();
    private static final List<AppliedDamage> APPLIED = new ArrayList<>();

    private DamageEvents() {
    }

    /** Vurusun hic gerceklesmemesi gerektigine karar verir (orn. kirik silahla saldiri). */
    @FunctionalInterface
    public interface CancelIncoming {
        boolean shouldCancel(LivingEntity target, DamageSource source);
    }

    /** Azaltma oncesi ham hasari degistirir (seviye hasar carpani buraya girer). */
    @FunctionalInterface
    public interface ModifyIncoming {
        float modify(LivingEntity target, DamageSource source, float amount);
    }

    /** Zirh/buyu/efekt azaltmasindan SONRA, absorption TUKETILMEDEN once. */
    @FunctionalInterface
    public interface MitigatedDamage {
        void onMitigated(LivingEntity target, DamageSource source, float mitigatedDamage);
    }

    /** Can gercekten dusurulduKten SONRA, uygulanan hasar degeriyle. */
    @FunctionalInterface
    public interface AppliedDamage {
        void onApplied(LivingEntity target, DamageSource source, float appliedDamage);
    }

    public static void registerCancelIncoming(CancelIncoming listener) {
        CANCEL_INCOMING.add(listener);
    }

    public static void registerModifyIncoming(ModifyIncoming listener) {
        MODIFY_INCOMING.add(listener);
    }

    public static void registerMitigated(MitigatedDamage listener) {
        MITIGATED.add(listener);
    }

    public static void registerApplied(AppliedDamage listener) {
        APPLIED.add(listener);
    }

    // -------------------------------------------------------------------------------------
    // Mixin tarafindan cagirilir
    // -------------------------------------------------------------------------------------

    public static boolean shouldCancelIncoming(LivingEntity target, DamageSource source) {
        for (CancelIncoming listener : CANCEL_INCOMING) {
            if (listener.shouldCancel(target, source)) {
                return true;
            }
        }
        return false;
    }

    public static float modifyIncoming(LivingEntity target, DamageSource source, float amount) {
        float modified = amount;
        for (ModifyIncoming listener : MODIFY_INCOMING) {
            modified = listener.modify(target, source, modified);
        }
        return modified;
    }

    public static void fireMitigated(LivingEntity target, DamageSource source, float mitigatedDamage) {
        for (MitigatedDamage listener : MITIGATED) {
            listener.onMitigated(target, source, mitigatedDamage);
        }
    }

    public static void fireApplied(LivingEntity target, DamageSource source, float appliedDamage) {
        for (AppliedDamage listener : APPLIED) {
            listener.onApplied(target, source, appliedDamage);
        }
    }
}
