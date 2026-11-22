// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.job;

/**
 * A boolean option a job declares; values are stored per golem in its workplace.
 * Enum/number options can be added later by generalizing the value type.
 */
public record JobOption(String key, boolean defaultValue) {

    public String translationKey() {
        return "job_option.principiamagica." + this.key;
    }

    public String descriptionKey() {
        return this.translationKey() + ".description";
    }
}
