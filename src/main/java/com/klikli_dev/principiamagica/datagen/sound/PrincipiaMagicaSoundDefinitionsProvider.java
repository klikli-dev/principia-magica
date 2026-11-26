// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.sound;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.registry.SoundRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinition.SoundType;
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

        // Libation & Khemeia
        add(SoundRegistry.LIBATION_SLAB_TAKE_ITEM, definition()
                .subtitle("subtitles.principiamagica.libation_slab.take_item")
                .with(event(SoundEvents.ITEM_PICKUP).volume(0.5F)));

        add(SoundRegistry.LIBATION_SPOUT_OPEN, definition()
                .subtitle("subtitles.principiamagica.libation_spout.open")
                .with(event(SoundEvents.STONE_BUTTON_CLICK_ON)));

        add(SoundRegistry.LIBATION_SPOUT_CLOSE, definition()
                .subtitle("subtitles.principiamagica.libation_spout.close")
                .with(event(SoundEvents.STONE_BUTTON_CLICK_OFF)));

        add(SoundRegistry.CATCH_TROUGH_PLACE_ITEM, definition()
                .subtitle("subtitles.principiamagica.catch_trough.place_item")
                .with(event(SoundEvents.DECORATED_POT_INSERT).volume(0.6F)));

        add(SoundRegistry.CATCH_TROUGH_TAKE_ITEM, definition()
                .subtitle("subtitles.principiamagica.catch_trough.take_item")
                .with(event(SoundEvents.ITEM_PICKUP).volume(0.5F)));

        add(SoundRegistry.DRYING_SLAB_PLACE_ITEM, definition()
                .subtitle("subtitles.principiamagica.drying_slab.place_item")
                .with(event(SoundEvents.DECORATED_POT_INSERT).volume(0.6F)));

        add(SoundRegistry.DRYING_SLAB_TAKE_ITEM, definition()
                .subtitle("subtitles.principiamagica.drying_slab.take_item")
                .with(event(SoundEvents.ITEM_PICKUP).volume(0.5F)));

        add(SoundRegistry.STEEPING_JAR_LID_CLOSE, definition()
                .subtitle("subtitles.principiamagica.steeping_jar.lid_close")
                .with(event(SoundEvents.DECORATED_POT_HIT).volume(0.5F).pitch(0.85F)));

        add(SoundRegistry.STEEPING_JAR_LID_OPEN, definition()
                .subtitle("subtitles.principiamagica.steeping_jar.lid_open")
                .with(event(SoundEvents.DECORATED_POT_HIT).volume(0.5F).pitch(1.15F)));

        add(SoundRegistry.PRINCIPLE_SELECT, definition()
                .with(event(SoundEvents.UI_STONECUTTER_SELECT_RECIPE).volume(0.35F).pitch(1.2F)));

        // Golem
        add(SoundRegistry.GOLEM_PLACE, definition()
                .subtitle("subtitles.principiamagica.golem.place")
                .with(event(SoundEvents.DECORATED_POT_PLACE)));

        add(SoundRegistry.GOLEM_PICK_UP, definition()
                .subtitle("subtitles.principiamagica.golem.pick_up")
                .with(event(SoundEvents.DECORATED_POT_PLACE).volume(0.8F).pitch(1.25F)));

        add(SoundRegistry.GOLEM_HURT, definition()
                .subtitle("subtitles.principiamagica.golem.hurt")
                .with(event(SoundEvents.DECORATED_POT_HIT)));

        add(SoundRegistry.GOLEM_DEATH, definition()
                .subtitle("subtitles.principiamagica.golem.death")
                .with(event(SoundEvents.DECORATED_POT_SHATTER)));

        add(SoundRegistry.GOLEM_HEAL, definition()
                .subtitle("subtitles.principiamagica.golem.heal")
                .with(event(SoundEvents.DECORATED_POT_INSERT).pitch(1.2F)));

        add(SoundRegistry.GOLEM_AXIOM_INSERT, definition()
                .subtitle("subtitles.principiamagica.golem.axiom_insert")
                .with(event(SoundEvents.DECORATED_POT_INSERT)));

        add(SoundRegistry.GOLEM_AXIOM_REMOVE, definition()
                .subtitle("subtitles.principiamagica.golem.axiom_remove")
                .with(event(SoundEvents.DECORATED_POT_INSERT).pitch(0.75F)));

        add(SoundRegistry.GOLEM_EQUIP_TOOL, definition()
                .subtitle("subtitles.principiamagica.golem.equip_tool")
                .with(event(SoundEvents.ARMOR_EQUIP_GENERIC.value())));

        add(SoundRegistry.GOLEM_PICKUP_ITEM, definition()
                .subtitle("subtitles.principiamagica.golem.pickup_item")
                .with(event(SoundEvents.ITEM_PICKUP).volume(0.3F)));

        // Copper Stylus
        add(SoundRegistry.STYLUS_SELECT, definition()
                .subtitle("subtitles.principiamagica.stylus.select")
                .with(event(SoundEvents.COPPER_BULB_TURN_ON)));

        add(SoundRegistry.STYLUS_DESELECT, definition()
                .subtitle("subtitles.principiamagica.stylus.deselect")
                .with(event(SoundEvents.COPPER_BULB_TURN_OFF)));

        add(SoundRegistry.STYLUS_CARVE, definition()
                .subtitle("subtitles.principiamagica.stylus.carve")
                .with(event(SoundEvents.BRUSH_GRAVEL)));

        add(SoundRegistry.STYLUS_PICK_UP_CORNER, definition()
                .subtitle("subtitles.principiamagica.stylus.pick_up_corner")
                .with(event(SoundEvents.BRUSH_GRAVEL).pitch(1.2F)));

        add(SoundRegistry.STYLUS_ADJUST_HEIGHT, definition()
                .subtitle("subtitles.principiamagica.stylus.adjust_height")
                .with(event(SoundEvents.STONE_BUTTON_CLICK_ON).volume(0.5F)));

        add(SoundRegistry.STYLUS_ERASE, definition()
                .subtitle("subtitles.principiamagica.stylus.erase")
                .with(event(SoundEvents.CALCITE_BREAK).pitch(0.8F)));

        add(SoundRegistry.STYLUS_LINK_SUPPLY, definition()
                .subtitle("subtitles.principiamagica.stylus.link_supply")
                .with(event(SoundEvents.BARREL_OPEN)));

        add(SoundRegistry.STYLUS_LINK_DEPOSIT, definition()
                .subtitle("subtitles.principiamagica.stylus.link_deposit")
                .with(event(SoundEvents.BARREL_CLOSE)));

        add(SoundRegistry.STYLUS_LINK_BOTH, definition()
                .subtitle("subtitles.principiamagica.stylus.link_both")
                .with(event(SoundEvents.NOTE_BLOCK_CHIME.value())));

        add(SoundRegistry.STYLUS_UNLINK, definition()
                .subtitle("subtitles.principiamagica.stylus.unlink")
                .with(event(SoundEvents.NOTE_BLOCK_BASS.value()).pitch(0.6F)));

        add(SoundRegistry.STYLUS_FAIL, definition()
                .subtitle("subtitles.principiamagica.stylus.fail")
                .with(event(SoundEvents.NOTE_BLOCK_BASS.value()).pitch(0.5F)));

        // Additional Libation, Golem, and Imprint Tablet sounds
        add(SoundRegistry.LIBATION_SLAB_DISSOLVING, definition()
                .subtitle("subtitles.principiamagica.libation_slab.dissolving")
                .with(event(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP).volume(0.15F).pitch(1.4F)));

        add(SoundRegistry.SPOUT_DRIP, definition()
                .subtitle("subtitles.principiamagica.spout.drip")
                .with(event(SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON).volume(0.2F)));

        add(SoundRegistry.GOLEM_TOOL_STOW, definition()
                .subtitle("subtitles.principiamagica.golem.tool_stow")
                .with(event(SoundEvents.BUNDLE_INSERT).volume(0.35F)));

        add(SoundRegistry.GOLEM_TOOL_UNSTOW, definition()
                .subtitle("subtitles.principiamagica.golem.tool_unstow")
                .with(event(SoundEvents.BUNDLE_REMOVE_ONE).volume(0.35F)));

        add(SoundRegistry.IMPRINT_TABLET_COPY, definition()
                .subtitle("subtitles.principiamagica.imprint_tablet.copy")
                .with(event(SoundEvents.CALCITE_PLACE)));

        add(SoundRegistry.IMPRINT_TABLET_PASTE, definition()
                .subtitle("subtitles.principiamagica.imprint_tablet.paste")
                .with(event(SoundEvents.BRUSH_SAND)));

        add(SoundRegistry.IMPRINT_TABLET_CLEAR, definition()
                .subtitle("subtitles.principiamagica.imprint_tablet.clear")
                .with(event(SoundEvents.CALCITE_BREAK).volume(0.7F).pitch(1.1F)));

        add(SoundRegistry.GOLEM_WOOD_PLACE, definition()
                .subtitle("subtitles.principiamagica.golem.wood.place")
                .with(event(SoundEvents.WOOD_PLACE)));

        add(SoundRegistry.GOLEM_WOOD_PICK_UP, definition()
                .subtitle("subtitles.principiamagica.golem.wood.pick_up")
                .with(event(SoundEvents.WOOD_PLACE).volume(0.8F).pitch(1.25F)));
    }

    private static SoundDefinition.Sound event(SoundEvent sound) {
        return sound(sound.location(), SoundType.EVENT);
    }
}
