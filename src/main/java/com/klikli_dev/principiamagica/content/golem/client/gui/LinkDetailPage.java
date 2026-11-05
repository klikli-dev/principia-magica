// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.gui;

import com.klikli_dev.principiamagica.client.gui.ContentRect;
import com.klikli_dev.principiamagica.client.gui.widget.ItemIconWidget;
import com.klikli_dev.principiamagica.client.gui.widget.LabelWidget;
import com.klikli_dev.principiamagica.client.gui.widget.SegmentedControl;
import com.klikli_dev.principiamagica.client.gui.widget.TextButton;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerLink;
import com.klikli_dev.principiamagica.content.golem.workplace.ContainerRole;
import com.klikli_dev.principiamagica.network.Networking;
import com.klikli_dev.principiamagica.network.messages.MessageRemoveLink;
import com.klikli_dev.principiamagica.network.messages.MessageSetLinkRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A container link: block, position, side and the role the golem uses it for. Pops back to the list if the link disappears.
 */
final class LinkDetailPage extends GolemDetailPage {

    private final int index;

    LinkDetailPage(Supplier<GolemView> viewSupplier, int index) {
        super(viewSupplier, Component.translatable("gui.principiamagica.golem.link.title"));
        this.index = index;
    }

    @Override
    protected void build(ContentRect bounds) {
        int y = this.addHeader(bounds);
        int x = bounds.x() + 8;
        int width = 160;

        this.add(new ItemIconWidget(x, y, () -> this.link().map(link -> GolemGuiText.blockItem(link.pos())).orElse(ItemStack.EMPTY)));
        this.add(new LabelWidget(x + 22, y + 4, width - 22, () -> this.link().map(link -> GolemGuiText.blockName(link.pos())).orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 22, width, () -> this.link()
                .map(link -> Component.translatable("gui.principiamagica.golem.link.position", link.pos().getX(), link.pos().getY(), link.pos().getZ()))
                .orElse(Component.empty())));
        this.add(new LabelWidget(x, y + 34, width, () -> this.link()
                .map(link -> Component.translatable("gui.principiamagica.golem.link.side", link.side().getName()))
                .orElse(Component.empty())));

        this.add(new LabelWidget(x, y + 48, width, () -> Component.translatable("gui.principiamagica.golem.link.role")));
        this.add(new SegmentedControl<ContainerRole>(x, y + 58, width, 18, List.of(ContainerRole.values()), GolemGuiText::roleName,
                () -> this.link().map(ContainerLink::role).orElse(ContainerRole.BOTH),
                role -> Networking.sendToServer(new MessageSetLinkRole(this.golemId(), this.index, role))));

        this.add(new TextButton(x, y + 88, 120, 18, Component.translatable("gui.principiamagica.golem.link.remove"), () -> {
            Networking.sendToServer(new MessageRemoveLink(this.golemId(), this.index));
            this.context().pop();
        }));
    }

    @Override
    public void tick() {
        if (this.view().workplaceKnown() && this.link().isEmpty()) {
            this.context().pop();
        }
    }

    private Optional<ContainerLink> link() {
        List<ContainerLink> links = this.view().workplace().links();
        return this.index < links.size() ? Optional.of(links.get(this.index)) : Optional.empty();
    }
}
