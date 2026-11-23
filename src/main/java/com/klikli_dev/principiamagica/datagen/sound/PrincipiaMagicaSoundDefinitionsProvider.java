// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.sound;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * Sounds are composed from vanilla sound files with pitch and volume variation, since there are no recorded sounds yet.
 */
public class PrincipiaMagicaSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public PrincipiaMagicaSoundDefinitionsProvider(PackOutput output) {
        super(output, PrincipiaMagica.MODID);
    }

    @Override
    public void registerSounds() {
        add(SoundRegistry.LIBATION_SLAB_POUR, definition()
                .subtitle("subtitles.principiamagica.libation_slab.pour")
                .with(
                        sound("minecraft:item/bucket/empty1").volume(0.5F).pitch(0.8F),
                        sound("minecraft:item/bucket/empty2").volume(0.5F).pitch(1.0F),
                        sound("minecraft:item/bucket/empty3").volume(0.5F).pitch(1.2F)));

        add(SoundRegistry.LIBATION_SLAB_DISSOLVE, definition()
                .subtitle("subtitles.principiamagica.libation_slab.dissolve")
                .with(
                        sound("minecraft:block/bubble_column/bubble1").volume(0.2F).pitch(1.4F),
                        sound("minecraft:block/bubble_column/bubble2").volume(0.2F).pitch(1.6F),
                        sound("minecraft:block/bubble_column/bubble3").volume(0.2F).pitch(1.8F),
                        sound("minecraft:random/fizz").volume(0.1F).pitch(1.5F)));

        add(SoundRegistry.LIBATION_SLAB_PLACE_ITEM, definition()
                .subtitle("subtitles.principiamagica.libation_slab.place_item")
                .with(
                        sound("minecraft:block/decorated_pot/insert1").volume(0.6F).pitch(1.0F),
                        sound("minecraft:block/decorated_pot/insert2").volume(0.6F).pitch(1.1F),
                        sound("minecraft:block/decorated_pot/insert3").volume(0.6F).pitch(0.9F),
                        sound("minecraft:block/decorated_pot/insert4").volume(0.6F).pitch(1.05F)));

        add(SoundRegistry.CATCH_TROUGH_STEEP, definition()
                .subtitle("subtitles.principiamagica.catch_trough.steep")
                .with(
                        sound("minecraft:block/brewing_stand/brew1").volume(0.5F).pitch(1.0F),
                        sound("minecraft:block/brewing_stand/brew2").volume(0.5F).pitch(1.1F),
                        sound("minecraft:block/amethyst/shimmer").volume(0.3F).pitch(1.5F)));

        add(SoundRegistry.VESSEL_TAP_OPEN, definition()
                .subtitle("subtitles.principiamagica.vessel.tap_open")
                .with(
                        sound("minecraft:block/copper_trapdoor/toggle1").volume(0.5F).pitch(1.3F),
                        sound("minecraft:block/copper_trapdoor/toggle2").volume(0.5F).pitch(1.4F),
                        sound("minecraft:block/copper_trapdoor/toggle3").volume(0.5F).pitch(1.2F)));

        add(SoundRegistry.VESSEL_TAP_CLOSE, definition()
                .subtitle("subtitles.principiamagica.vessel.tap_close")
                .with(
                        sound("minecraft:block/copper_trapdoor/toggle1").volume(0.5F).pitch(0.8F),
                        sound("minecraft:block/copper_trapdoor/toggle2").volume(0.5F).pitch(0.9F),
                        sound("minecraft:block/copper_trapdoor/toggle4").volume(0.5F).pitch(0.7F)));

        add(SoundRegistry.VESSEL_CURE, definition()
                .subtitle("subtitles.principiamagica.vessel.cure")
                .with(
                        sound("minecraft:block/bubble_column/upwards_ambient1").volume(0.15F).pitch(0.9F),
                        sound("minecraft:block/bubble_column/upwards_ambient2").volume(0.15F).pitch(1.0F),
                        sound("minecraft:block/bubble_column/upwards_ambient3").volume(0.15F).pitch(1.1F)));

        add(SoundRegistry.DRYING_SLAB_DRIED, definition()
                .subtitle("subtitles.principiamagica.drying_slab.dried")
                .with(
                        sound("minecraft:step/sand1").volume(0.5F).pitch(1.2F),
                        sound("minecraft:step/sand2").volume(0.5F).pitch(1.3F),
                        sound("minecraft:step/grass1").volume(0.4F).pitch(1.4F)));

        add(SoundRegistry.PHIAL_FILL, definition()
                .subtitle("subtitles.principiamagica.phial.fill")
                .with(
                        sound("minecraft:item/bottle/fill1").volume(0.6F).pitch(0.95F),
                        sound("minecraft:item/bottle/fill2").volume(0.6F).pitch(1.0F),
                        sound("minecraft:item/bottle/fill3").volume(0.6F).pitch(1.05F),
                        sound("minecraft:item/bottle/fill4").volume(0.6F).pitch(1.1F)));

        add(SoundRegistry.PHIAL_EMPTY, definition()
                .subtitle("subtitles.principiamagica.phial.empty")
                .with(
                        sound("minecraft:item/bucket/empty1").volume(0.4F).pitch(1.25F),
                        sound("minecraft:item/bucket/empty2").volume(0.4F).pitch(1.35F),
                        sound("minecraft:item/bucket/empty3").volume(0.4F).pitch(1.45F)));

        add(SoundRegistry.NATRON_SPONGE_BLOT, definition()
                .subtitle("subtitles.principiamagica.natron_sponge.blot")
                .with(
                        sound("minecraft:block/sponge/absorb1").volume(0.7F).pitch(1.1F),
                        sound("minecraft:block/sponge/absorb2").volume(0.7F).pitch(1.2F),
                        sound("minecraft:block/sponge/absorb3").volume(0.7F).pitch(1.0F),
                        sound("minecraft:random/fizz").volume(0.15F).pitch(1.4F)));
    }
}
