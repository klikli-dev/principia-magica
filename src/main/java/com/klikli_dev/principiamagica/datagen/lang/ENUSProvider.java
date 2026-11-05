// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.lang;

import com.klikli_dev.principiamagica.PrincipiaMagica;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ENUSProvider extends LanguageProvider {
    public ENUSProvider(PackOutput packOutput) {
        super(packOutput, PrincipiaMagica.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("modmenu.nameTranslation.principiamagica", "Principia Magica");
        this.add("itemGroup.principiamagica", "Principia Magica");

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

        this.add("job_option.principiamagica.pickup_items", "Pick up stray items");
        this.add("job_option.principiamagica.pickup_items.description", "Pick up loose items inside the work zone and deposit them.");
        this.add("job_option.principiamagica.replant", "Replant harvested crops");
        this.add("job_option.principiamagica.replant.description", "Plant a seed on empty farmland after harvesting, preferring the seed that grew there before.");
        this.add("job_option.principiamagica.till", "Till soil near water (needs a hoe)");
        this.add("job_option.principiamagica.till.description", "Turn dirt and grass near water into farmland. Needs a hoe in the main hand and uses its durability.");
        this.add("job_option.principiamagica.bonemeal", "Apply bone meal to crops");
        this.add("job_option.principiamagica.bonemeal.description", "Use bone meal on crops that can still grow. Bone meal is restocked from supply containers.");

        this.add("golem_job.principiamagica.farming", "Farming");
        this.add("item.principiamagica.farming_core", "Farming Core");

        this.add("golem_upgrade.principiamagica.swiftness", "Swiftness");
        this.add("golem_upgrade.principiamagica.swiftness.description", "Faster movement and work speed");
        this.add("golem_upgrade.principiamagica.capacity", "Capacity");
        this.add("golem_upgrade.principiamagica.capacity.description", "+3 golem inventory slots");
        this.add("item.principiamagica.swiftness_upgrade", "Swiftness Upgrade");
        this.add("item.principiamagica.capacity_upgrade", "Capacity Upgrade");

        this.add("gui.principiamagica.golem.title", "%s Golem");
        this.add("gui.principiamagica.golem.info", "%s, %s");
        this.add("gui.principiamagica.golem.no_core", "No core");
        this.add("gui.principiamagica.golem.options", "Job options");
        this.add("gui.principiamagica.golem.option_state", "%s: %s");
        this.add("gui.principiamagica.golem.on", "On");
        this.add("gui.principiamagica.golem.off", "Off");
        this.add("gui.principiamagica.golem.zones", "Work zones (%s)");
        this.add("gui.principiamagica.golem.no_zones", "No work zones, use the baton to add one");
        this.add("gui.principiamagica.golem.zone_row", "Zone %sx%sx%s at %s, %s, %s");
        this.add("gui.principiamagica.golem.containers", "Containers (%s)");
        this.add("gui.principiamagica.golem.no_containers", "No containers, use the baton to link one");
        this.add("gui.principiamagica.golem.role_supply", "Supply");
        this.add("gui.principiamagica.golem.role_deposit", "Deposit");
        this.add("gui.principiamagica.golem.role_both", "Both");
        this.add("gui.principiamagica.golem.remove", "Remove");
        this.add("gui.principiamagica.golem.remove_core", "Remove core");
        this.add("gui.principiamagica.golem.upgrades", "Upgrades (%s/%s)");
        this.add("gui.principiamagica.golem.no_upgrades", "No upgrades, use an upgrade item on the golem");
        this.add("gui.principiamagica.golem.hint_line_1", "Edit zones and containers");
        this.add("gui.principiamagica.golem.hint_line_2", "with the Golem Baton");

        this.add("tooltip.principiamagica.golem.material", "Material: %s");
        this.add("tooltip.principiamagica.golem.upgrades", "Upgrades: %s/%s");
        this.add("item.principiamagica.golem_baton", "Golem Baton");
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

        this.add("message.principiamagica.baton.not_owner", "You do not own this golem");
        this.add("message.principiamagica.baton.no_golem_selected", "Select a golem first by right-clicking it");
        this.add("message.principiamagica.baton.golem_not_found", "The selected golem is not nearby");
        this.add("message.principiamagica.baton.selected", "Golem selected");
        this.add("message.principiamagica.baton.deselected", "Golem deselected");
        this.add("message.principiamagica.baton.copied", "Copied workplace: %s zones, %s container links");
        this.add("message.principiamagica.baton.pasted", "Pasted workplace");
        this.add("message.principiamagica.baton.cleared", "Cleared selection and clipboard");
        this.add("message.principiamagica.baton.corner_set", "First corner set, right-click the opposite corner");
        this.add("message.principiamagica.baton.zone_added", "Work zone added");
        this.add("message.principiamagica.baton.zone_removed", "Work zone removed");
        this.add("message.principiamagica.baton.zone_too_large", "Zone too large, maximum is %s x %s x %s");
        this.add("message.principiamagica.baton.nothing_to_remove", "Nothing to remove here");
        this.add("message.principiamagica.baton.link_set", "Container link: %s");
        this.add("message.principiamagica.baton.link_removed", "Container link removed");
        this.add("message.principiamagica.baton.picked_up", "Golem picked up, its workplace is kept");

        this.add("tooltip.principiamagica.golem_baton.selected", "Selected golem: %s");
        this.add("tooltip.principiamagica.golem_baton.yes", "yes");
        this.add("tooltip.principiamagica.golem_baton.none", "none");
        this.add("tooltip.principiamagica.golem_baton.pending_corner", "Pending corner: %s, %s, %s");
        this.add("tooltip.principiamagica.golem_baton.clipboard_empty", "Clipboard: empty");
        this.add("tooltip.principiamagica.golem_baton.clipboard", "Clipboard: %s zones, %s container links");
        this.add("tooltip.principiamagica.golem_baton.hint_select", "Right-click golem: select / deselect");
        this.add("tooltip.principiamagica.golem_baton.hint_copy_paste", "Shift-right-click golem: copy workplace / paste clipboard");
        this.add("tooltip.principiamagica.golem_baton.hint_link", "Right-click container: cycle supply, deposit, both, removed");
        this.add("tooltip.principiamagica.golem_baton.hint_zone", "Right-click two blocks: create a work zone");
        this.add("tooltip.principiamagica.golem_baton.hint_remove", "Shift-right-click zone or container: remove it");
        this.add("tooltip.principiamagica.golem_baton.hint_pickup", "Shift-left-click own golem: pick up (keeps workplace)");
        this.add("tooltip.principiamagica.golem_baton.hint_clear", "Shift-right-click air: clear selection and clipboard");
    }
}
