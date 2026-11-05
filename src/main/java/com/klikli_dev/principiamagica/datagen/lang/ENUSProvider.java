// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.lang;

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

    @Override
    protected void addTranslations() {
        this.add("modmenu.nameTranslation.principiamagica", "Principia Magica");
        this.add("itemGroup.principiamagica", "Principia Magica");

        this.add("advancements.principiamagica.the_clay_remembers.title", "The Clay Remembers");
        this.add("advancements.principiamagica.the_clay_remembers.description", "Something was pressed into this clay long ago. You should write it down.");
        this.add("advancements.principiamagica.a_wedge_of_copper.title", "A Wedge of Copper");
        this.add("advancements.principiamagica.a_wedge_of_copper.description", "Carve the clay as the scribe did.");
        this.add("advancements.principiamagica.wet_clay.title", "Wet Clay");
        this.add("advancements.principiamagica.wet_clay.description", "Shape a blank tablet, ready to receive a word.");
        this.add("item.principiamagica.principia", "Principia");
        this.add("item.principiamagica.wet_tablet", "Wet Tablet");

        this.add("golem_material.principiamagica.wood", "Wood");
        this.add("golem_material.principiamagica.stone", "Stone");
        this.add("golem_material.principiamagica.metal", "Metal");

        this.add("attribute.principiamagica.golem_work_speed", "Golem Work Speed");
        this.add("attribute.principiamagica.golem_work_range", "Golem Work Range");
        this.add("attribute.principiamagica.golem_inventory_slots", "Golem Inventory Slots");

        this.add("entity.principiamagica.golem", "Golem");
        this.add("item.principiamagica.golem", "Golem");

        this.add("golem_mode.principiamagica.work", "Work");
        this.add("golem_mode.principiamagica.follow", "Follow");
        this.add("golem_mode.principiamagica.stay", "Stay");
        this.add("message.principiamagica.golem.mode", "Golem mode: %s");
        this.add("message.principiamagica.golem.job_already_set", "This golem already has a job");

        this.add("golem_status.principiamagica.ok", "Working");
        this.add("golem_status.principiamagica.no_job", "No job");
        this.add("golem_status.principiamagica.no_zone", "No work zone");
        this.add("golem_status.principiamagica.missing_tool", "Missing tool");
        this.add("golem_status.principiamagica.no_deposit", "No deposit container");
        this.add("golem_status.principiamagica.deposit_full", "Deposit full");
        this.add("golem_status.principiamagica.needs_supplies", "Needs supplies");
        this.add("golem_status.principiamagica.ok.description", "The golem is working on its job.");
        this.add("golem_status.principiamagica.no_job.description", "Insert a job core to give the golem a job.");
        this.add("golem_status.principiamagica.no_zone.description", "Add a work zone with the Copper Stylus.");
        this.add("golem_status.principiamagica.missing_tool.description", "The job needs its tool in the golem's main hand.");
        this.add("golem_status.principiamagica.no_deposit.description", "Link a container with the deposit or both role.");
        this.add("golem_status.principiamagica.deposit_full.description", "The deposit containers have no room left.");
        this.add("golem_status.principiamagica.needs_supplies.description", "A needed item is missing and no supply container holds it.");

        this.add("job_option.principiamagica.pickup_items", "Pick up stray items");
        this.add("job_option.principiamagica.pickup_items.description", "Pick up loose items inside the work zone and deposit them.");
        this.add("job_option.principiamagica.replant", "Replant harvested crops");
        this.add("job_option.principiamagica.replant.description", "Plant a seed on empty farmland after harvesting, preferring the seed that grew there before.");
        this.add("job_option.principiamagica.clear_obstructions", "Clear obstructions");

        this.add("job_option.principiamagica.clear_obstructions.description", "Clear grass, flowers and similar plants from the work area so crops can be planted. Seeds dropped by grass go into the golem's inventory.");

        this.add("job_option.principiamagica.till", "Till soil near water");
        this.add("job_option.principiamagica.till.description", "Turn dirt and grass near water into farmland. Needs a hoe in the main hand and uses its durability.");
        this.add("job_option.principiamagica.bonemeal", "Apply bone meal to crops");
        this.add("job_option.principiamagica.bonemeal.description", "Use bone meal on crops that can still grow. Bone meal is restocked from supply containers.");
        this.add("job_option.principiamagica.replant_saplings", "Replant saplings");
        this.add("job_option.principiamagica.replant_saplings.description", "Plant a matching sapling on the stump of each felled tree. Saplings are restocked from supply containers.");
        this.add("job_option.principiamagica.clear_leaves", "Clear blocking leaves");
        this.add("job_option.principiamagica.clear_leaves.description", "Break natural leaves around a trunk when they stop the golem from reaching it. Player-placed leaves are never broken.");
        this.add("job_option.principiamagica.bonemeal_saplings", "Apply bone meal to saplings");
        this.add("job_option.principiamagica.bonemeal_saplings.description", "Use bone meal on saplings in the work area so trees grow faster. Bone meal is restocked from supply containers.");

        this.add("golem_job.principiamagica.farming", "Farming");
        this.add("item.principiamagica.farming_core", "Farming Core");
        this.add("golem_job.principiamagica.lumberjack", "Lumberjack");
        this.add("item.principiamagica.lumberjack_core", "Lumberjack Core");

        this.add("golem_upgrade.principiamagica.swiftness", "Swiftness");
        this.add("golem_upgrade.principiamagica.swiftness.description", "Faster movement and work speed");
        this.add("golem_upgrade.principiamagica.capacity", "Capacity");
        this.add("golem_upgrade.principiamagica.capacity.description", "+3 golem inventory slots");
        this.add("item.principiamagica.swiftness_upgrade", "Swiftness Upgrade");
        this.add("item.principiamagica.capacity_upgrade", "Capacity Upgrade");

        this.add("gui.principiamagica.golem.title", "%s Golem");
        this.add("gui.principiamagica.golem.no_core", "No core");
        this.add("gui.principiamagica.golem.options", "Job options");
        this.add("gui.principiamagica.golem.no_job_hint", "Insert a job core into the golem to give it a job. Right-click the golem with a core.");
        this.add("gui.principiamagica.golem.material", "Material: %s");
        this.add("gui.principiamagica.golem.job", "Job: %s");
        this.add("gui.principiamagica.golem.mode", "Mode");
        this.add("gui.principiamagica.golem.remove_core", "Remove core");
        this.add("gui.principiamagica.golem.remove_core.tooltip", "Takes the job core out of the golem and returns it to you");
        this.add("gui.principiamagica.golem.tab.overview", "Overview");
        this.add("gui.principiamagica.golem.tab.job", "Job");
        this.add("gui.principiamagica.golem.tab.workplace", "Workplace");
        this.add("gui.principiamagica.golem.tab.upgrades", "Upgrades");
        this.add("gui.principiamagica.golem.tab.inventory", "Inventory");
        this.add("gui.principiamagica.golem.back", "Back");
        this.add("gui.principiamagica.golem.zones", "Work zones (%s)");
        this.add("gui.principiamagica.golem.no_zones", "No work zones, use the stylus to add one");
        this.add("gui.principiamagica.golem.zone_row", "Zone %sx%sx%s at %s, %s, %s");
        this.add("gui.principiamagica.golem.containers", "Containers (%s)");
        this.add("gui.principiamagica.golem.no_containers", "No containers, use the stylus to link one");
        this.add("gui.principiamagica.golem.workplace_hint", "Edit zones and containers with the Copper Stylus");
        this.add("gui.principiamagica.golem.zone.title", "Zone %s");
        this.add("gui.principiamagica.golem.zone.size", "Size: %s x %s x %s");
        this.add("gui.principiamagica.golem.zone.from", "From: %s, %s, %s");
        this.add("gui.principiamagica.golem.zone.to", "To: %s, %s, %s");
        this.add("gui.principiamagica.golem.zone.remove", "Remove zone");
        this.add("gui.principiamagica.golem.link.title", "Container");
        this.add("gui.principiamagica.golem.link.position", "Position: %s, %s, %s");
        this.add("gui.principiamagica.golem.link.side", "Side: %s");
        this.add("gui.principiamagica.golem.link.role", "Role");
        this.add("gui.principiamagica.golem.link.remove", "Remove container");
        this.add("gui.principiamagica.golem.role_supply", "Supply");
        this.add("gui.principiamagica.golem.role_deposit", "Deposit");
        this.add("gui.principiamagica.golem.role_both", "Both");
        this.add("gui.principiamagica.golem.upgrades", "Upgrades (%s/%s)");
        this.add("gui.principiamagica.golem.no_upgrades", "No upgrades, use an upgrade item on the golem");
        this.add("gui.principiamagica.golem.upgrade.effects", "Effects");
        this.add("gui.principiamagica.golem.upgrade.remove", "Remove upgrade");
        this.add("gui.principiamagica.golem.attribute", "%s %s");
        this.add("gui.principiamagica.golem.inventory.tool", "Tool");
        this.add("gui.principiamagica.golem.inventory.golem", "Golem inventory");

        this.add("tooltip.principiamagica.golem.material", "Material: %s");
        this.add("tooltip.principiamagica.golem.upgrades", "Upgrades: %s/%s");
        this.add("item.principiamagica.copper_stylus", "Copper Stylus");
        this.add("golem_role.principiamagica.supply", "Supply");
        this.add("golem_role.principiamagica.deposit", "Deposit");
        this.add("golem_role.principiamagica.both", "Supply and deposit");

        this.add("message.principiamagica.golem.job_removed", "Job core removed");
        this.add("message.principiamagica.golem.job_swapped", "Job core swapped, previous core returned");
        this.add("message.principiamagica.golem.no_job", "This golem has no job");
        this.add("message.principiamagica.golem.not_owner", "You do not own this golem");
        this.add("message.principiamagica.golem.upgrade_added", "Upgrade installed: %s");
        this.add("message.principiamagica.golem.upgrade_duplicate", "This golem already has this upgrade");
        this.add("message.principiamagica.golem.upgrade_no_slot", "This golem has no free upgrade slot");

        this.add("message.principiamagica.stylus.not_owner", "You do not own this golem");
        this.add("message.principiamagica.stylus.no_golem_selected", "Select a golem first by right-clicking it");
        this.add("message.principiamagica.stylus.golem_not_found", "The selected golem is not nearby");
        this.add("message.principiamagica.stylus.selected", "Golem selected");
        this.add("message.principiamagica.stylus.deselected", "Golem deselected");
        this.add("message.principiamagica.stylus.copied", "Copied workplace: %s zones, %s container links");
        this.add("message.principiamagica.stylus.pasted", "Pasted workplace");
        this.add("message.principiamagica.stylus.cleared", "Cleared selection and clipboard");
        this.add("message.principiamagica.stylus.corner_set", "First corner set, right-click the opposite corner");
        this.add("message.principiamagica.stylus.zone_added", "Work zone added");
        this.add("message.principiamagica.stylus.zone_removed", "Work zone removed");
        this.add("message.principiamagica.stylus.zone_too_large", "Zone too large, maximum is %s x %s x %s");
        this.add("message.principiamagica.stylus.nothing_to_remove", "Nothing to remove here");
        this.add("message.principiamagica.stylus.link_set", "Container link: %s");
        this.add("message.principiamagica.stylus.link_removed", "Container link removed");
        this.add("message.principiamagica.stylus.picked_up", "Golem picked up, its workplace is kept");

        this.add("tooltip.principiamagica.copper_stylus.selected", "Selected golem: %s");
        this.add("tooltip.principiamagica.copper_stylus.yes", "yes");
        this.add("tooltip.principiamagica.copper_stylus.none", "none");
        this.add("tooltip.principiamagica.copper_stylus.pending_corner", "Pending corner: %s, %s, %s");
        this.add("tooltip.principiamagica.copper_stylus.clipboard_empty", "Clipboard: empty");
        this.add("tooltip.principiamagica.copper_stylus.clipboard", "Clipboard: %s zones, %s container links");
        this.add("tooltip.principiamagica.copper_stylus.hint_select", "Right-click golem: select / deselect");
        this.add("tooltip.principiamagica.copper_stylus.hint_copy_paste", "Shift-right-click golem: copy workplace / paste clipboard");
        this.add("tooltip.principiamagica.copper_stylus.hint_link", "Right-click container: cycle supply, deposit, both, removed");
        this.add("tooltip.principiamagica.copper_stylus.hint_zone", "Right-click two blocks: create a work zone");
        this.add("tooltip.principiamagica.copper_stylus.hint_remove", "Shift-right-click zone or container: remove it");
        this.add("tooltip.principiamagica.copper_stylus.hint_pickup", "Shift-left-click own golem: pick up (keeps workplace)");
        this.add("tooltip.principiamagica.copper_stylus.hint_clear", "Shift-right-click air: clear selection and clipboard");

        this.langCache.data().forEach(this::add);
    }
}
