// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.client.render.workplace;

import net.minecraft.util.Mth;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Fades an element in and out when its key changes. While the key is unchanged, the alpha moves towards 1; when the key
 * changes, the old element fades out first and the new key is only adopted once the alpha reached 0.
 */
public class FadingElementTracker<K> {

    public static final float STEP = 1.0F / 8.0F;

    @Nullable
    private K key;
    private float previousAlpha;
    private float alpha;

    /**
     * @param wanted the key that should be visible now, or null if nothing should be visible
     */
    public void tick(@Nullable K wanted) {
        this.previousAlpha = this.alpha;
        if (wanted != null && Objects.equals(wanted, this.key)) {
            this.alpha = Math.min(1.0F, this.alpha + STEP);
            return;
        }

        this.alpha = Math.max(0.0F, this.alpha - STEP);
        if (this.alpha == 0.0F) {
            this.key = wanted;
        }
    }

    @Nullable
    public K key() {
        return this.key;
    }

    public float alpha(float partialTick) {
        return Mth.lerp(partialTick, this.previousAlpha, this.alpha);
    }

    public boolean isVisible(float partialTick) {
        return this.key != null && this.alpha(partialTick) > 0.0F;
    }
}
