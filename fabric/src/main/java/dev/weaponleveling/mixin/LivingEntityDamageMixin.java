package dev.weaponleveling.mixin;

import dev.weaponleveling.platform.event.DamageEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge'un hasar olaylarinin vanilla karsiliklari. Butun enjeksiyon noktalari 1.21.1
 * bytecode'u incelenerek secildi - tahmin yok.
 *
 * `hurt(DamageSource, float)` govdesi (dogrulandi):
 * <pre>
 *   isInvulnerableTo -> return false
 *   level().isClientSide -> return false
 *   isDeadOrDying -> return false
 *   fire hasari + Fire Resistance -> return false
 *   isSleeping -> stopSleeping()
 *   this.noActionTime = 0;      &lt;-- ON KONTROLLER BITTI, `amount` HENUZ KULLANILMADI
 *   float f = amount; ...       &lt;-- `amount`in ilk kullanimi
 * </pre>
 *
 * `actuallyHurt(DamageSource, float)` govdesi (dogrulandi):
 * <pre>
 *   damageAmount = getDamageAfterArmorAbsorb(...)   // arg slotuna 1. store
 *   damageAmount = getDamageAfterMagicAbsorb(...)   // arg slotuna 2. store  &lt;-- AZALTMA BITTI
 *   float f = damageAmount;
 *   damageAmount = max(damageAmount - absorption, 0) // 3. store (absorption TUKETILDI)
 *   ...
 *   if (damageAmount == 0) return;
 *   recordDamage(...); setHealth(getHealth() - damageAmount); ...
 *   return;                                          &lt;-- TAIL: can DUSURULDU
 * </pre>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    /**
     * Vurusun tamamen iptali. Metodun BASINDA calisir: vanilla zaten reddedecek olsa bile iptal
     * etmek ayni sonucu (false) verdigi icin on kontrolleri burada tekrarlamaya gerek yoktur.
     * Iptal karari yalnizca saldirgana ve kaynaga bakar, hasar miktarina DEGIL - bu yuzden
     * miktar degistirme kancasindan ayri ve ondan once durabiliyor.
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void weaponleveling$cancelIncomingDamage(
        DamageSource source,
        float amount,
        CallbackInfoReturnable<Boolean> callback
    ) {
        if (DamageEvents.shouldCancelIncoming((LivingEntity) (Object) this, source)) {
            callback.setReturnValue(false);
        }
    }

    /**
     * Azaltma oncesi hasar degisikligi (seviye carpani). `noActionTime = 0` atamasindan SONRAYA
     * baglanir: bu nokta vanilla'nin dort erken-cikis kontrolunden sonra, `amount`in ilk
     * kullanimindan oncedir - yani NeoForge'un `LivingIncomingDamageEvent`'i ile ayni asama.
     * Boylece kalkan blogu, knockback ve zirh hesabi dahil her sey degistirilmis degeri gorur.
     */
    @ModifyVariable(
        method = "hurt",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;noActionTime:I",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        ),
        argsOnly = true,
        ordinal = 0
    )
    private float weaponleveling$modifyIncomingDamage(float value, DamageSource source, float amount) {
        return DamageEvents.modifyIncoming((LivingEntity) (Object) this, source, value);
    }

    /**
     * Zirh XP'si icin azaltma SONRASI deger. Arg slotuna yapilan 2. store'a baglanir - yani
     * `getDamageAfterMagicAbsorb` sonucu, absorption cikarilmadan once. Zirhin gercekten
     * karsiladigi baskiyi temsil eder ve tamamen absorbe edilen bir vurus da zirhi egitir.
     * Deger DEGISTIRILMEZ, yalnizca gozlenir.
     */
    @ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 1), argsOnly = true)
    private float weaponleveling$observeMitigatedDamage(float value, DamageSource source, float damageAmount) {
        DamageEvents.fireMitigated((LivingEntity) (Object) this, source, value);
        return value;
    }

    /**
     * Can dusurulduKten sonra uygulanan hasar. TAIL metodun SON return'une baglanir; hasarin 0
     * oldugu erken cikis bu noktaya hic ulasmaz, yani olay yalnizca gercekten can eksildiginde
     * atesler - NeoForge'un `LivingDamageEvent.Post` davranisiyla ayni.
     */
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void weaponleveling$appliedDamage(DamageSource source, float damageAmount, CallbackInfo callback) {
        DamageEvents.fireApplied((LivingEntity) (Object) this, source, damageAmount);
    }
}
