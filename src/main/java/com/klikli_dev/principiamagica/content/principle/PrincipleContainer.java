// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.principle;

/**
 * Implemented by block entities that hold principles, such as the Catch Trough, Wabet Basin and Steeping Jar.
 */
public interface PrincipleContainer {

    PrincipleContents getPrinciples();

    /**
     * Offers principles to this container.
     * <p>
     * Partial acceptance is allowed up to the capacity of the container.
     *
     * @param offered  the principles to insert.
     * @param simulate if true, the container is not modified and only the result is computed.
     * @return the leftover: the part of {@code offered} that was NOT accepted. Empty if everything was accepted.
     */
    PrincipleContents insert(PrincipleContents offered, boolean simulate);
}
