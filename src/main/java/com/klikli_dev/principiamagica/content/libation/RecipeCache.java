// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.libation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Remembers the recipe a libation block entity matched for its contents, so that it is not looked up every tick.
 * <p>
 * An entry is reused while its contents are unchanged and the recipe manager is the one it was looked up with. A datapack
 * reload builds a new recipe manager, see {@code ReloadableServerResources}, so every entry is looked up again after a reload,
 * even when the contents did not change. Missing recipes are remembered the same way, and are looked up again after a reload.
 *
 * @param <T> the type of the recipe.
 */
public final class RecipeCache<T extends Recipe<?>> {

    private @Nullable RecipeManager manager;
    private @Nullable RecipeHolder<T> recipe;
    private boolean stale = true;

    /**
     * Marks the entry stale, so that the next lookup asks the recipe manager again. Call when the contents change, and when the
     * block entity loads.
     */
    public void invalidate() {
        this.stale = true;
    }

    /**
     * @param level  the level of the block entity, the recipe manager is taken from its server.
     * @param lookup looks the recipe up, used when the entry is stale or the recipe manager changed.
     * @return the recipe, or null if there is none or the level is not a server level.
     */
    public @Nullable RecipeHolder<T> get(Level level, Supplier<Optional<RecipeHolder<T>>> lookup) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return this.get(serverLevel.getServer().getRecipeManager(), lookup);
    }

    /**
     * @param current the recipe manager the recipe must belong to.
     * @param lookup  looks the recipe up, used when the entry is stale or the recipe manager is not {@code current}.
     * @return the recipe, or null if there is none.
     */
    public @Nullable RecipeHolder<T> get(RecipeManager current, Supplier<Optional<RecipeHolder<T>>> lookup) {
        if (this.stale || current != this.manager) {
            this.recipe = lookup.get().orElse(null);
            this.manager = current;
            this.stale = false;
        }
        return this.recipe;
    }
}
