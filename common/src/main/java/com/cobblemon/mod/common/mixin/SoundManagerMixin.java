/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.sound.battle.BattleMusicController;
import com.cobblemon.mod.common.duck.SoundManagerDuck;
import com.cobblemon.mod.common.duck.SoundSystemDuck;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements SoundManagerDuck {

    @Shadow
    public abstract boolean isPlaying(SoundInstance sound);

    @Shadow @Final
    private SoundSystem soundSystem;

    // Never-ending ambient loops are a special exception (they are only initiated once) and should always be paused if filtered.
    private boolean isAmbientLoop(SoundInstance sound) {
        return sound instanceof BiomeEffectSoundPlayer.MusicLoop || sound instanceof AmbientSoundLoops.Underwater;
    }

    private boolean filterCondition(SoundInstance sound) {
        return !isAmbientLoop(sound) &&
            this.isPlaying(BattleMusicController.INSTANCE.getMusic()) &&
                BattleMusicController.INSTANCE.getFilteredCategories().contains(sound.getCategory());
    }

    private boolean ambientLoopCondition(SoundInstance sound) {
        return isAmbientLoop(sound) &&
            this.isPlaying(BattleMusicController.INSTANCE.getMusic()) &&
                BattleMusicController.INSTANCE.getFilteredCategories().contains(sound.getCategory());
    }

    /** Pauses the queried SoundInstance(s). If id is null, will pause all sounds belonging to the SoundCategory. */
    @Override
    public void pauseSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
        ((SoundSystemDuck)soundSystem).pauseSounds(id, category);
    }

    /** Resumes the queried SoundInstance(s). If id is null, will resume all sounds belonging to the SoundCategory. */
    @Override
    public void resumeSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
        ((SoundSystemDuck)soundSystem).resumeSounds(id, category);
    }

    /** Stops the queried SoundInstance(s). If id is null, will stop all sounds belonging to the SoundCategory. */
    @Override
    public void stopSounds(@Nullable Identifier id, @Nullable SoundCategory category) {
        ((SoundSystemDuck)soundSystem).stopSounds(id, category);
    }

    /** Blocks filtered sounds from being played while a BattleMusicInstance is in progress. */
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void playStart(SoundInstance sound, CallbackInfo cb) {
        if (filterCondition(sound)) cb.cancel();
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    public void playStart(SoundInstance sound, int delay, CallbackInfo cb) {
        if (filterCondition(sound)) cb.cancel();
    }

    /** Pauses ambient loops while a BattleMusicInstance is in progress. */
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("TAIL"))
    public void playEnd(SoundInstance sound, CallbackInfo cb) {
        if (ambientLoopCondition(sound)) this.pauseSounds(sound.getId(), SoundCategory.AMBIENT);
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("TAIL"))
    public void playEnd(SoundInstance sound, int delay, CallbackInfo cb) {
        if (ambientLoopCondition(sound)) this.pauseSounds(sound.getId(), SoundCategory.AMBIENT);
    }
}
