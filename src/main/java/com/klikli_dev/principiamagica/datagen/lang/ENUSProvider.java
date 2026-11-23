// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.lang;

import com.klikli_dev.principiamagica.content.principle.Principle;
import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ENUSProvider extends LanguageProvider {
    private final LanguageProviderCache langCache;

    public ENUSProvider(PackOutput packOutput, LanguageProviderCache langCache) {
        super(packOutput, PrincipiaMagica.MODID, "en_us");
        this.langCache = langCache;
    }

    private void addPrinciple(String name, String displayName, String description) {
        this.add("principle." + PrincipiaMagica.MODID + "." + name, displayName);
        this.add("principle." + PrincipiaMagica.MODID + "." + name + ".description", description);
    }

    @Override
    protected void addTranslations() {
        this.add("modmenu.nameTranslation.principiamagica", "Principia Magica");
        this.add("itemGroup.principiamagica", "Principia Magica");

        this.add("block.principiamagica.libation_slab", "Libation Slab");
        this.add("block.principiamagica.libation_spout", "Libation Spout");
        this.add("block.principiamagica.vessel_tap", "Vessel Tap");
        this.add("block.principiamagica.catch_trough", "Catch Trough");
        this.add("block.principiamagica.wabet_basin", "Wabet Basin");
        this.add("block.principiamagica.steeping_jar", "Steeping Jar");
        this.add("block.principiamagica.drying_slab", "Drying Slab");

        this.add("subtitles.principiamagica.libation_slab.pour", "Principles pour");
        this.add("subtitles.principiamagica.libation_slab.dissolve", "Item dissolves");
        this.add("subtitles.principiamagica.libation_slab.place_item", "Item placed");
        this.add("subtitles.principiamagica.catch_trough.steep", "Item steeps");
        this.add("subtitles.principiamagica.vessel.tap_open", "Tap opens");
        this.add("subtitles.principiamagica.vessel.tap_close", "Tap closes");
        this.add("subtitles.principiamagica.vessel.cure", "Principles cure");
        this.add("subtitles.principiamagica.drying_slab.dried", "Item dries");

        this.add("message.principiamagica.libation_slab.not_washable", "Nothing can be washed from this");
        this.add("message.principiamagica.libation_slab.full", "The slab is full");
        this.add("message.principiamagica.libation_slab.other_item", "The slab already holds a different item");
        this.add("message.principiamagica.libation_slab.water_full", "The slab already holds water");
        this.add("message.principiamagica.libation_slab.water_only", "The slab only takes water");

        this.addPrinciple("silt", "Silt", "Foundation. The weight and permanence of earth, stone and clay.");
        this.addPrinciple("gleam", "Gleam", "Radiance. The clarity of sky, light and polished things.");
        this.addPrinciple("sap", "Sap", "Vitality. The growth and moisture of plants and flesh.");
        this.addPrinciple("ash", "Ash", "Severing. The dryness and edge of things that have ended.");
        this.addPrinciple("loam", "Loam", "Fertile, sun-baked river earth.");
        this.addPrinciple("shard", "Shard", "A kiln-hardened terracotta edge.");
        this.addPrinciple("myrrh", "Myrrh", "A sun-cured golden resin.");
        this.addPrinciple("glaze", "Glaze", "A luminous, glassy coating.");
        this.addPrinciple("mire", "Mire", "Clinging sludge of the bog.");
        this.addPrinciple("bitumen", "Bitumen", "A dark mineral pitch that seals and binds.");
        this.addPrinciple("unguent", "Unguent", "A fermented anointing oil that quickens clay.");
        this.addPrinciple("kohl", "Kohl", "A pigment of soot and minerals.");
        this.addPrinciple("alabaster", "Alabaster", "Translucent stone that holds light in solid mass.");
        this.addPrinciple("marrow", "Marrow", "Living vitality kept in dry bone.");
        this.addPrinciple("faience", "Faience", "A radiant blue-gold ceramic.");
        this.addPrinciple("kyphi", "Kyphi", "Temple incense of great purity.");
        this.addPrinciple("stele", "Stele", "Monumental carved stone.");
        this.addPrinciple("sinew", "Sinew", "Living tissue that binds a body together.");
        this.addPrinciple("brine", "Brine", "Bitter primordial saltwater.");
        this.addPrinciple("veil", "Veil", "Shadowed sight.");

        this.add("item.principiamagica.principia", "Principia");
        this.add("item.principiamagica.axiom_tablet", "Axiom Tablet");
        this.add("item.principiamagica.quickened_clay", "Quickened Clay");
        this.add("item.principiamagica.axiom_of_harvest_unfired", "Unfired Axiom of Harvest");
        this.add("item.principiamagica.axiom_of_felling_unfired", "Unfired Axiom of Felling");
        this.add("item.principiamagica.axiom_of_harvest_unfired_steeped", "Steeped Unfired Axiom of Harvest");
        this.add("item.principiamagica.axiom_of_felling_unfired_steeped", "Steeped Unfired Axiom of Felling");
        this.add("item.principiamagica.axiom_of_harvest", "Axiom of Harvest");
        this.add("item.principiamagica.axiom_of_felling", "Axiom of Felling");

        this.add("golem_material.principiamagica.wood", "Wooden");

        this.add("attribute.principiamagica.golem_work_speed", "Golem Work Speed");

        this.add("entity.principiamagica.golem", "Golem");
        this.add("item.principiamagica.golem", "Golem");
        this.add("item.principiamagica.golem.named", "%s Golem");

        this.add("golem_mode.principiamagica.work", "Work");
        this.add("golem_mode.principiamagica.follow", "Follow");
        this.add("golem_mode.principiamagica.stay", "Stay");
        this.add("message.principiamagica.golem.mode", "Golem mode: %s");
        this.add("message.principiamagica.golem.job_already_set", "This golem already has a job");

        this.add("golem_task.principiamagica.idle", "Idle");
        this.add("golem_task.principiamagica.wait", "Waiting for deposit");
        this.add("golem_task.principiamagica.harvest", "Harvest %s");
        this.add("golem_task.principiamagica.plant", "Planting %s");
        this.add("golem_task.principiamagica.till", "Tilling %s");
        this.add("golem_task.principiamagica.bonemeal", "Fertilise %s");
        this.add("golem_task.principiamagica.clear", "Clearing %s");
        this.add("golem_task.principiamagica.fell", "Felling %s");
        this.add("golem_task.principiamagica.pickup", "Pick up %s");
        this.add("golem_task.principiamagica.deposit", "Deposit %s");
        this.add("golem_task.principiamagica.restock", "Restock %s");
        this.add("golem_task.principiamagica.follow", "Following you");
        this.add("golem_task.principiamagica.stay", "Staying");
        this.add("golem_task.principiamagica.work_area", "the work area");
        this.add("message.principiamagica.golem.slots_full", "Both of the golem's slots are full");
        this.add("golem_status.principiamagica.ok", "Working");
        this.add("golem_status.principiamagica.no_purpose", "Purposeless");
        this.add("golem_status.principiamagica.no_zone", "No work zone");
        this.add("golem_status.principiamagica.missing_tool", "Missing tool");
        this.add("golem_status.principiamagica.no_deposit", "No deposit container");
        this.add("golem_status.principiamagica.deposit_full", "Deposit full");
        this.add("golem_status.principiamagica.needs_supplies", "Needs supplies");
        this.add("golem_status.principiamagica.ok.description", "The golem is working on its job.");
        this.add("golem_status.principiamagica.no_purpose.description", "Without an axiom it can only stay or follow you.");
        this.add("golem_status.principiamagica.no_zone.description", "Without a work area the golem has nothing to work on.");
        this.add("golem_status.principiamagica.missing_tool.description", "Without the right tool it cannot harvest, plant or chop.");
        this.add("golem_status.principiamagica.no_deposit.description", "Harvested items go into a container it can reach.");
        this.add("golem_status.principiamagica.deposit_full.description", "The containers it deposits into have no room left.");
        this.add("golem_status.principiamagica.needs_supplies.description", "A needed item is missing and no supply container holds it.");

        this.add("job_option.principiamagica.pickup_items", "Pick up stray items");
        this.add("job_option.principiamagica.pickup_items.description", "Pick up loose items inside the work zone and deposit them.");
        this.add("job_option.principiamagica.replant", "Replant harvested crops");
        this.add("job_option.principiamagica.replant.description", "Plant a seed on empty farmland after harvesting, preferring the seed that grew there before.");
        this.add("job_option.principiamagica.clear_obstructions", "Clear obstructions");

        this.add("job_option.principiamagica.clear_obstructions.description", "Clear grass, flowers and similar plants from the work area so crops can be planted. Seeds dropped by grass go into the golem's inventory.");

        this.add("job_option.principiamagica.till", "Till soil near water");
        this.add("job_option.principiamagica.till.description", "Turn dirt and grass near water into farmland. Needs a hoe in the main hand and uses its durability.");
        this.add("job_option.principiamagica.bonemeal", "Bone meal on crops");
        this.add("job_option.principiamagica.bonemeal.description", "Use bone meal on crops that can still grow. Bone meal is restocked from supply containers.");
        this.add("job_option.principiamagica.replant_saplings", "Replant saplings");
        this.add("job_option.principiamagica.replant_saplings.description", "Plant a matching sapling on the stump of each felled tree. Saplings are restocked from supply containers.");
        this.add("job_option.principiamagica.clear_leaves", "Clear blocking leaves");
        this.add("job_option.principiamagica.clear_leaves.description", "Break natural leaves around a trunk when they stop the golem from reaching it. Player-placed leaves are never broken.");
        this.add("job_option.principiamagica.bonemeal_saplings", "Bone meal on saplings");
        this.add("job_option.principiamagica.bonemeal_saplings.description", "Use bone meal on saplings in the work area so trees grow faster. Bone meal is restocked from supply containers.");

        this.add("golem_job.principiamagica.farming", "Farming");
        this.add("golem_job.principiamagica.lumberjack", "Lumberjack");

        this.add("gui.principiamagica.golem.title", "%s Golem");
        this.add("gui.principiamagica.golem.no_axiom", "No axiom");
        this.add("gui.principiamagica.golem.tab.golem", "Golem");
        this.add("gui.principiamagica.golem.tab.job", "Job");
        this.add("gui.principiamagica.golem.tab.job.disabled", "Give the golem an axiom to unlock its job");
        this.add("gui.principiamagica.golem.axiom.tooltip", "Place an axiom to give the golem a job");
        this.add("gui.principiamagica.golem.hand.tooltip", "Hand: the golem's tool while it works, or an item it carries");
        this.add("gui.principiamagica.golem.back.tooltip", "Back: the golem's tool, stowed while it carries an item");
        this.add("gui.principiamagica.golem.health", "Health: %s / %s");
        this.add("gui.principiamagica.golem.task_at", "at %s");
        this.add("gui.principiamagica.golem.zones", "%s zones");
        this.add("gui.principiamagica.golem.no_deposit", "No deposit");
        this.add("gui.principiamagica.golem.no_supply", "No supply");
        this.add("gui.principiamagica.golem.workplace.tooltip", "Edit with the Stylus");
        this.add("gui.principiamagica.golem.none_linked", "None linked");
        this.add("gui.principiamagica.golem.zones.title", "Work zones: %s");
        this.add("gui.principiamagica.golem.zones.description", "Areas carved with the Stylus where this golem works");
        this.add("gui.principiamagica.golem.deposit.title", "Deposit");
        this.add("gui.principiamagica.golem.deposit.description", "Containers where the golem stores its output");
        this.add("gui.principiamagica.golem.supply.title", "Supply");
        this.add("gui.principiamagica.golem.supply.description", "Containers the golem takes seeds, saplings and bone meal from");
        this.add("gui.principiamagica.golem.mode.work.tooltip", "The golem works its job");
        this.add("gui.principiamagica.golem.mode.work.disabled", "Work needs an axiom");
        this.add("gui.principiamagica.golem.mode.follow.tooltip", "The golem follows you");
        this.add("gui.principiamagica.golem.mode.stay.tooltip", "The golem stays where it is");
        this.add("hud.principiamagica.stylus.role.supply", "Supply");
        this.add("hud.principiamagica.stylus.role.deposit", "Deposit");
        this.add("hud.principiamagica.stylus.role.both", "Both");
        this.add("hud.principiamagica.stylus.pending_corner", "Corner pending");
        this.add("hud.principiamagica.stylus.moving_corner", "Moving corner");
        this.add("hud.principiamagica.stylus.corner_locked", "Corner locked (look away to release)");
        this.add("key_combo.principiamagica.right_click", "Right Click");
        this.add("key_combo.principiamagica.crouch_right_click", "Crouch + Right Click");
        this.add("key_combo.principiamagica.crouch_left_click", "Crouch + Left Click");
        this.add("key_combo.principiamagica.crouch_scroll", "Crouch + Scroll");

        this.add("hud.principiamagica.stylus.hint.lift_next", "%s: adjust corner height");
        this.add("hud.principiamagica.stylus.hint.carve_opposite", "%s a block: carve the opposite corner");
        this.add("hud.principiamagica.stylus.hint.place_moved_corner", "%s a block: place corner");
        this.add("hud.principiamagica.stylus.hint.cancel", "%s air: cancel");
        this.add("hud.principiamagica.stylus.hint.move_closer", "Move closer to the golem to cancel the move");
        this.add("hud.principiamagica.stylus.hint.discard_move", "%s air: discard the moved work area");
        this.add("hud.principiamagica.stylus.hint.lift_corner", "%s: move corner up / down");
        this.add("hud.principiamagica.stylus.hint.move_corner", "%s: pick up and move corner");
        this.add("hud.principiamagica.stylus.hint.erase", "%s: erase work area");
        this.add("hud.principiamagica.stylus.hint.corner_options", "Aim at a corner glyph for more options");

        this.add("hud.principiamagica.imprint_tablet.clipboard_empty", "Clipboard: empty");
        this.add("hud.principiamagica.imprint_tablet.clipboard", "Clipboard: %s footprints, %s container links");
        this.add("hud.principiamagica.imprint_tablet.hint.copy", "%s: copy workplace");
        this.add("hud.principiamagica.imprint_tablet.hint.paste", "%s: paste clipboard");
        this.add("hud.principiamagica.imprint_tablet.hint.clear", "%s: clear clipboard");
        this.add("hud.principiamagica.stylus.hint.cycle_link", "%s: cycle link");
        this.add("hud.principiamagica.stylus.hint.unlink", "%s: unlink");
        this.add("hud.principiamagica.stylus.hint.select_golem", "%s a golem: select");
        this.add("hud.principiamagica.stylus.hint.select", "%s: select");
        this.add("hud.principiamagica.stylus.hint.deselect", "%s: deselect");
        this.add("hud.principiamagica.libation.empty", "Empty");
        this.add("hud.principiamagica.libation.status.curing", "Curing");
        this.add("hud.principiamagica.libation.status.spout_open", "Spout open");
        this.add("hud.principiamagica.libation.status.spout_closed", "Spout closed");
        this.add("hud.principiamagica.libation.status.no_spout", "No spout attached");
        this.add("hud.principiamagica.libation.status.tap_open", "Tap open");
        this.add("hud.principiamagica.libation.status.tap_closed", "Tap closed");
        this.add("hud.principiamagica.libation.status.no_tap", "No tap attached");
        this.add("hud.principiamagica.libation.status.no_target", "Nothing below to receive the drain");
        this.add("hud.principiamagica.libation.status.no_recipe", "No recipe");
        this.add("hud.principiamagica.libation.status.too_dark", "Too dark");
        this.add("hud.principiamagica.libation.status.dried", "Dried");
        this.add("hud.principiamagica.libation.status.dissolving", "Dissolving");
        this.add("hud.principiamagica.libation.status.drying", "Drying");
        this.add("hud.principiamagica.libation.status.output_full", "Output full");
        this.add("hud.principiamagica.libation.status.steeping", "Steeping");
        this.add("hud.principiamagica.libation.status.missing_principles", "Missing principles");
        this.add("hud.principiamagica.libation.water", "Water %s/%s mB");
        this.add("principle.principiamagica.amount", "%s mB");
        this.add(Principle.PREFIXED_NAME_KEY, "Principle: %s");
        this.add("jei.principiamagica.principle.amount", "Amount: %s");
        this.add("jei.principiamagica.libation", "Libation");
        this.add("jei.principiamagica.wabet_curing", "Wabet Curing");
        this.add("jei.principiamagica.steeping_jar_curing", "Steeping Jar Curing");
        this.add("jei.principiamagica.steeping", "Steeping");
        this.add("jei.principiamagica.drying", "Drying");
        this.add("jei.principiamagica.curing.light_min", "Needs light ≥ %s");
        this.add("jei.principiamagica.time_at_full_light", "%s s at full light");
        this.add("jei.principiamagica.time", "%s s");
        this.add("gui.principiamagica.golem.status_hint.no_purpose", "Give it an axiom");
        this.add("gui.principiamagica.golem.status_hint.no_zone", "Carve a work area with the Stylus");
        this.add("gui.principiamagica.golem.status_hint.missing_tool", "Put a matching tool in its hand");
        this.add("gui.principiamagica.golem.status_hint.no_deposit", "Link a chest with the Stylus");
        this.add("gui.principiamagica.golem.status_hint.deposit_full", "Empty it or link another one");
        this.add("gui.principiamagica.golem.status_hint.needs_supplies", "Stock a chest with supplies");

        this.add("tooltip.principiamagica.golem.material", "Material: %s");
        this.add("message.principiamagica.golem.destroyed", "Your %s was destroyed at %s, %s, %s.");
        this.add("item.principiamagica.copper_stylus", "Copper Stylus");
        this.add("item.principiamagica.imprint_tablet", "Imprint Tablet");
        this.add("golem_role.principiamagica.supply", "Supply");
        this.add("golem_role.principiamagica.deposit", "Deposit");
        this.add("golem_role.principiamagica.both", "Supply and deposit");

        this.add("message.principiamagica.golem.job_removed", "Axiom removed");
        this.add("message.principiamagica.golem.job_swapped", "Axiom swapped, previous axiom returned");
        this.add("message.principiamagica.golem.no_job", "This golem has no job");
        this.add("message.principiamagica.golem.not_owner", "You do not own this golem");

        this.add("message.principiamagica.stylus.not_owner", "You do not own this golem");
        this.add("message.principiamagica.stylus.no_golem_selected", "Select a golem first by right-clicking it");
        this.add("message.principiamagica.stylus.golem_not_found", "The selected golem is not nearby");
        this.add("message.principiamagica.stylus.selected", "Golem selected");
        this.add("message.principiamagica.stylus.deselected", "Golem deselected");
        this.add("message.principiamagica.stylus.cleared", "Cleared selection");
        this.add("message.principiamagica.stylus.corner_set", "First glyph carved, carve the opposite corner");
        this.add("message.principiamagica.stylus.corner_cancelled", "Pending corner cancelled");
        this.add("message.principiamagica.stylus.corner_picked_up", "Corner picked up, right-click a block to place it");
        this.add("message.principiamagica.stylus.corner_moved", "Work area corner moved");
        this.add("message.principiamagica.stylus.corner_move_cancelled", "Corner move cancelled");
        this.add("message.principiamagica.stylus.corner_move_golem_not_found", "Move closer to the golem to cancel the corner move, or crouch to discard it");
        this.add("message.principiamagica.stylus.corner_move_discarded", "Moved work area discarded, it is lost");
        this.add("message.principiamagica.stylus.corner_occupied", "Another corner is already at that block");
        this.add("message.principiamagica.stylus.out_of_bounds", "Reached the world height limit");
        this.add("message.principiamagica.stylus.footprint_carved", "Footprint carved");
        this.add("message.principiamagica.stylus.footprint_erased", "Work area erased");
        this.add("message.principiamagica.stylus.footprint_too_large", "Footprint too large, maximum is %s x %s");
        this.add("message.principiamagica.stylus.footprint_moved", "Work area moved %s");
        this.add("message.principiamagica.stylus.corner_lifted", "Corner lifted %s");
        this.add("message.principiamagica.stylus.nothing_to_remove", "Nothing to remove here");
        this.add("message.principiamagica.stylus.link_set", "Container link: %s");
        this.add("message.principiamagica.stylus.link_removed", "Container link removed");
        this.add("message.principiamagica.stylus.picked_up", "Golem picked up, its workplace is kept");

        this.add("tooltip.principiamagica.copper_stylus.selected", "Selected golem: %s");
        this.add("tooltip.principiamagica.copper_stylus.yes", "yes");
        this.add("tooltip.principiamagica.copper_stylus.none", "none");
        this.add("tooltip.principiamagica.copper_stylus.pending_corner", "Pending glyph: %s, %s, %s");
        this.add("tooltip.principiamagica.copper_stylus.hint_select", "%s golem: select / deselect");
        this.add("tooltip.principiamagica.copper_stylus.hint_pickup", "%s own golem: pick up (keeps workplace)");

        this.add("tooltip.principiamagica.imprint_tablet.clipboard_empty", "Clipboard: empty");
        this.add("tooltip.principiamagica.imprint_tablet.clipboard", "Clipboard: %s footprints, %s container links");
        this.add("tooltip.principiamagica.imprint_tablet.hint_copy_paste", "%s golem: copy workplace / paste clipboard");
        this.add("tooltip.principiamagica.imprint_tablet.hint_clear", "%s: clear clipboard");
        this.add("message.principiamagica.imprint_tablet.copied", "Copied workplace: %s footprints, %s container links");
        this.add("message.principiamagica.imprint_tablet.pasted", "Pasted workplace");
        this.add("message.principiamagica.imprint_tablet.cleared", "Cleared clipboard");

        this.langCache.data().forEach(this::add);
    }
}
