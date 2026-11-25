// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.compat.modonomicon.page;

import com.klikli_dev.modonomicon.api.ModonomiconConstants;
import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.page.BookRecipePage;
import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import com.klikli_dev.modonomicon.client.render.page.PageWithTextRenderer;
import com.klikli_dev.modonomicon.fluid.FluidHolder;
import com.klikli_dev.modonomicon.fluid.NeoFluidHolder;
import com.klikli_dev.principiamagica.client.principle.PrincipleRenderer;
import com.klikli_dev.principiamagica.client.recipe.ClientRecipes;
import com.klikli_dev.principiamagica.content.principle.PrincipleStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Base of the renderers of the principle recipe pages.
 * <p>
 * The recipes are looked up in the recipes synced to the client, and drawn with the slots of the vanilla recipe pages.
 * Principles are drawn as their icon with the amount in mB below, and show their name and amount as a tooltip.
 * Nothing here depends on a recipe viewer.
 *
 * @param <R> the recipe type the page shows.
 * @param <P> the page type.
 */
public abstract class PrincipiaRecipePageRenderer<R extends Recipe<?>, P extends BookRecipePage<R>> extends BookPageRenderer<P> implements PageWithTextRenderer {

    /**
     * Top of the first recipe, like the other recipe pages.
     */
    public static final int Y = 4;

    protected static final int RECIPE_GAP = 4;
    protected static final int TITLE_HEIGHT = 12;
    protected static final int MISSING_HEIGHT = 12;

    protected static final int SLOT_SIZE = 22;
    /**
     * Distance between the left edges of two neighbouring slots.
     */
    protected static final int SLOT_PITCH = 30;
    protected static final int ICON_OFFSET = 3;
    protected static final int ICON_SIZE = 16;
    protected static final int ARROW_SIZE = 9;
    protected static final int LINE_HEIGHT = 9;
    protected static final int TEXT_COLOR = 0xFF404040;

    /**
     * Y of the labels below the slots, e.g. the amount of a principle.
     */
    protected static final int LABEL_Y = SLOT_SIZE + 2;
    /**
     * Y of the station icon, which sits below the arrow.
     */
    protected static final int STATION_Y = LABEL_Y;
    /**
     * Y of the time line, below the station icon.
     */
    protected static final int TIME_Y = STATION_Y + ICON_SIZE + 2;
    /**
     * Y of a second line below the time, e.g. the minimum light of the Wabet Basin.
     */
    protected static final int HINT_Y = TIME_Y + LINE_HEIGHT;

    /**
     * Height of a recipe that shows a time line, measured from its first slot.
     */
    protected static final int TIME_HEIGHT = TIME_Y + LINE_HEIGHT;
    /**
     * Height of a recipe that shows a time and a hint line, measured from its first slot.
     */
    protected static final int HINT_HEIGHT = HINT_Y + LINE_HEIGHT;
    /**
     * Height of a recipe that shows only the station, measured from its first slot.
     */
    protected static final int STATION_HEIGHT = STATION_Y + ICON_SIZE;

    private static final Component PLUS = Component.literal("+");
    private static final GuiSprite SLOT = new GuiSprite(modonomicon("modonomicon/themes/default/content/pages/recipes/crafting_slot"), SLOT_SIZE, SLOT_SIZE);
    private static final GuiSprite ARROW = new GuiSprite(modonomicon("modonomicon/themes/default/content/pages/recipes/crafting_arrow"), ARROW_SIZE, ARROW_SIZE);

    private final Class<R> recipeClass;

    protected PrincipiaRecipePageRenderer(P page, Class<R> recipeClass) {
        super(page);
        this.recipeClass = recipeClass;
    }

    private static Identifier modonomicon(String path) {
        return Identifier.fromNamespaceAndPath("modonomicon", path);
    }

    /**
     * @return the x of the arrow for a recipe with the given number of input slots, relative to the recipe.
     */
    protected static int arrowX(int inputCount) {
        return inputCount * SLOT_PITCH - 6;
    }

    /**
     * @return the x of the output slot for a recipe with the given number of input slots, relative to the recipe.
     */
    protected static int outputX(int inputCount) {
        return arrowX(inputCount) + ARROW_SIZE + 2;
    }

    /**
     * @return the width of a recipe with the given number of input slots and a single output.
     */
    protected static int rowWidth(int inputCount) {
        return outputX(inputCount) + SLOT_SIZE;
    }

    /**
     * @return the width of the recipe, measured from its first slot.
     */
    protected abstract int getRecipeWidth(R recipe);

    /**
     * @return the height of the recipe below its title.
     */
    protected abstract int getRecipeHeight(R recipe);

    /**
     * Draws the recipe. The recipe is centred on the page, and y is the top of its slots.
     */
    protected abstract void drawRecipe(GuiGraphicsExtractor guiGraphics, R recipe, int x, int y, int mouseX, int mouseY);

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float ticks) {
        int textY = this.layoutRecipes(guiGraphics, mouseX, mouseY);

        this.renderBookTextHolder(guiGraphics, this.getPage().getText(), 0, textY, BookEntryScreen.PAGE_WIDTH, this.getAvailablePageHeight() - textY);

        var style = this.getClickedComponentStyleAt(mouseX, mouseY);
        if (style != null) {
            //mouseX/mouseY are page-local (pMouseX - bookLeft - page.left), tooltips need screen coordinates
            this.parentScreen.renderComponentHoverEffect(guiGraphics, style, mouseX + this.parentScreen.getBookLeft() + this.left, mouseY + this.parentScreen.getBookTop() + this.top);
        }
    }

    @Override
    public int getTextY() {
        return this.layoutRecipes(null, 0, 0);
    }

    @Nullable
    @Override
    public Style getClickedComponentStyleAt(double pMouseX, double pMouseY) {
        if (pMouseX > 0 && pMouseY > 0) {
            var textY = this.getTextY();
            var bounds = this.getBookTextHolderBounds(0, textY, BookEntryScreen.PAGE_WIDTH, this.getAvailablePageHeight() - textY);
            var textStyle = this.getClickedComponentStyleAtForTextHolder(this.page.getText(), bounds.x, bounds.y, bounds.width, bounds.height, pMouseX, pMouseY);
            if (textStyle != null) {
                return textStyle;
            }
        }
        return super.getClickedComponentStyleAt(pMouseX, pMouseY);
    }

    /**
     * Draws the recipes if a graphics is given, and computes the y below them in both cases.
     */
    private int layoutRecipes(@Nullable GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int y = Y;
        var key1 = this.page.getRecipeKey1();
        if (key1 != null) {
            y = this.layoutRecipe(guiGraphics, this.page.getTitle1(), key1, y, mouseX, mouseY);
        }

        var key2 = this.page.getRecipeKey2();
        if (key2 != null) {
            y += key1 != null ? RECIPE_GAP : 0;
            y = this.layoutRecipe(guiGraphics, this.page.getTitle2(), key2, y, mouseX, mouseY);
        }
        return y;
    }

    private int layoutRecipe(@Nullable GuiGraphicsExtractor guiGraphics, BookTextHolder title, ResourceKey<Recipe<?>> key, int y, int mouseX, int mouseY) {
        int contentY = y;
        if (!title.isEmpty()) {
            if (guiGraphics != null) {
                this.renderTitle(guiGraphics, title, false, BookEntryScreen.PAGE_WIDTH / 2, y);
            }
            contentY += TITLE_HEIGHT;
        }

        R recipe = this.findRecipe(key);
        if (recipe == null) {
            if (guiGraphics != null) {
                this.drawWrappedStringNoShadow(guiGraphics,
                        Component.translatable(ModonomiconConstants.I18n.Gui.RECIPE_PAGE_RECIPE_MISSING, key.identifier().toString()).withStyle(ChatFormatting.RED),
                        0, contentY, -1, BookEntryScreen.PAGE_WIDTH);
            }
            return contentY + MISSING_HEIGHT;
        }

        if (guiGraphics != null) {
            int x = (BookEntryScreen.PAGE_WIDTH - this.getRecipeWidth(recipe)) / 2;
            this.drawRecipe(guiGraphics, recipe, x, contentY, mouseX, mouseY);
        }
        return contentY + this.getRecipeHeight(recipe);
    }

    @Nullable
    private R findRecipe(@Nullable ResourceKey<Recipe<?>> key) {
        if (key == null) {
            return null;
        }

        RecipeHolder<?> holder = ClientRecipes.byKey(key);
        if (holder == null || !this.recipeClass.isInstance(holder.value())) {
            return null;
        }
        return this.recipeClass.cast(holder.value());
    }

    /**
     * Draws a slot with a principle, its amount below, and its name and amount as a tooltip while hovered.
     */
    protected void drawPrinciple(GuiGraphicsExtractor guiGraphics, PrincipleStack stack, int x, int y, int mouseX, int mouseY) {
        this.drawSlot(guiGraphics, x, y);
        PrincipleRenderer.renderIcon(guiGraphics, stack.principle(), x + ICON_OFFSET, y + ICON_OFFSET);

        Font font = Minecraft.getInstance().font;
        Component amount = Component.literal(String.valueOf(stack.amount()));
        guiGraphics.text(font, amount, x + SLOT_SIZE / 2 - font.width(amount) / 2, y + LABEL_Y, TEXT_COLOR, false);

        if (isHovered(x, y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
            List<Component> tooltip = List.of(stack.principle().value().prefixedName(), stack.amountText().copy().withStyle(ChatFormatting.GRAY));
            guiGraphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), this.screenX(mouseX), this.screenY(mouseY));
        }
    }

    /**
     * Draws a slot with a fluid, with the given amount in mB below it.
     */
    protected void drawFluid(GuiGraphicsExtractor guiGraphics, FluidStack fluid, int x, int y, int mouseX, int mouseY) {
        this.drawSlot(guiGraphics, x, y);
        this.parentScreen.renderFluidStacks(guiGraphics, x + ICON_OFFSET, y + ICON_OFFSET, mouseX, mouseY,
                List.<FluidHolder>of(new NeoFluidHolder(fluid)), FluidType.BUCKET_VOLUME);

        Font font = Minecraft.getInstance().font;
        Component amount = Component.literal(String.valueOf(fluid.getAmount()));
        guiGraphics.text(font, amount, x + SLOT_SIZE / 2 - font.width(amount) / 2, y + LABEL_Y, TEXT_COLOR, false);
    }

    /**
     * Draws a slot with an ingredient, which shows the items of the ingredient in turn.
     */
    protected void drawIngredient(GuiGraphicsExtractor guiGraphics, Ingredient ingredient, int x, int y, int mouseX, int mouseY) {
        this.drawSlot(guiGraphics, x, y);
        this.parentScreen.renderIngredient(guiGraphics, x + ICON_OFFSET, y + ICON_OFFSET, mouseX, mouseY, ingredient);
    }

    /**
     * Draws a slot with an item.
     */
    protected void drawItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        this.drawSlot(guiGraphics, x, y);
        this.parentScreen.renderItemStack(guiGraphics, x + ICON_OFFSET, y + ICON_OFFSET, mouseX, mouseY, stack);
    }

    /**
     * Draws the icon of a station, without a slot.
     */
    protected void drawStation(GuiGraphicsExtractor guiGraphics, ItemLike station, int x, int y, int mouseX, int mouseY) {
        this.parentScreen.renderItemStack(guiGraphics, x, y, mouseX, mouseY, new ItemStack(station));
    }

    protected void drawSlot(GuiGraphicsExtractor guiGraphics, int x, int y) {
        SLOT.extractRenderState(guiGraphics, x, y);
    }

    /**
     * Draws the arrow between the inputs and the output. The arrow is centred on the slots.
     */
    protected void drawArrow(GuiGraphicsExtractor guiGraphics, int x, int y) {
        ARROW.extractRenderState(guiGraphics, x, y + (SLOT_SIZE - ARROW_SIZE) / 2);
    }

    /**
     * Draws a plus centred on the given x, between two slots.
     */
    protected void drawPlus(GuiGraphicsExtractor guiGraphics, int centerX, int y) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.text(font, PLUS, centerX - font.width(PLUS) / 2, y + (SLOT_SIZE - LINE_HEIGHT) / 2, TEXT_COLOR, false);
    }

    /**
     * Draws a line of text centred on the given x.
     */
    protected void drawCentered(GuiGraphicsExtractor guiGraphics, Component text, int centerX, int y) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.text(font, text, centerX - font.width(text) / 2, y, TEXT_COLOR, false);
    }

    /**
     * @return the time in seconds, for recipes whose time depends on the light.
     */
    protected static Component timeAtFullLight(int ticks) {
        return Component.translatable("jei.principiamagica.time_at_full_light", seconds(ticks));
    }

    /**
     * @return the time in seconds, for recipes whose time does not depend on the light.
     */
    protected static Component time(int ticks) {
        return Component.translatable("jei.principiamagica.time", seconds(ticks));
    }

    private static String seconds(int ticks) {
        if (ticks % 20 == 0) {
            return String.valueOf(ticks / 20);
        }
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }

    private static boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * Mouse positions are page-local, tooltips need screen coordinates.
     */
    private int screenX(int mouseX) {
        return mouseX + this.parentScreen.getBookLeft() + this.left;
    }

    private int screenY(int mouseY) {
        return mouseY + this.parentScreen.getBookTop() + this.top;
    }
}
