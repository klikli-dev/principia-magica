// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.NodeConnectionRendererType;
import com.klikli_dev.principiamagica.PrincipiaMagica;
import com.klikli_dev.principiamagica.datagen.book.khemeia.KhemeiaCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.scribespath.ScribesPathCategoryProvider;
import com.klikli_dev.principiamagica.datagen.book.wetclay.WetClayCategoryProvider;
import com.klikli_dev.principiamagica.registry.CreativeModeTabRegistry;

public class PrincipiaBookProvider extends SingleBookSubProvider {

    public static final String BOOK_ID = "principia";
    public static final String THEME_ID = "clay";

    public PrincipiaBookProvider() {
        super(BOOK_ID, PrincipiaMagica.MODID);
    }

    @Override
    protected void registerDefaultMacros() {
    }

    @Override
    protected void generateCategories() {
        this.add(new ScribesPathCategoryProvider(this).generate());
        this.add(new KhemeiaCategoryProvider(this).generate());
        this.add(new WetClayCategoryProvider(this).generate());
    }

    @Override
    protected String bookName() {
        return "Principia";
    }

    @Override
    protected String bookTooltip() {
        return "A notebook of copied tablets.\n\u00A7o(In-Game Guide for Principia Magica)\u00A7r";
    }

    @Override
    protected BookModel additionalSetup(BookModel book) {
        return super.additionalSetup(book)
                .withGenerateBookItem(false)
                .withCustomBookItem(this.modLoc(BOOK_ID))
                .withCreativeTab(CreativeModeTabRegistry.PRINCIPIA_MAGICA.getId())
                .withGenerateEntryHierarchyResearch(true)
                // Use a clay themed skin for the book. The theme keeps the default layout, palette and node
                // settings and only serves custom textures from the "clay" theme folder.
                .withTheme(theme -> theme
                        .withId(this.modLoc(THEME_ID))
                        // Node connections are drawn as dynamic lines instead of the routed, texture-based arrows.
                        .withNode(node -> node.withConnectionRenderer(NodeConnectionRendererType.DIRECT))
                        // 16px item icons are drawn at 12px, leaving a 1px gap inside the 3px clay button border.
                        // The clay frame body sits a few px inside the frame box, so the side buttons
                        // extend 19 px under the frame. This draws the 44 px category button at its full
                        // width at rest, and the right-side buttons clamp to 16 px to show the full sprite.
                        .withLayout(layout -> layout
                                .withCategoryButtonIconScale(0.75f)
                                // The clay button's notch bevel reaches x=14 of the sprite, and the 12 px scaled icon
                                // would start at x=10. Shift it 6 px right to leave a 1 px gap.
                                .withCategoryButtonIconXOffset(6)
                                .withSideButtonFrameOverlap(19)));
    }
}
