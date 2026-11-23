// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * The animation controllers of the golem and the work animations it plays for the tool it holds.
 */
public final class GolemAnimations {

    public static final String WORK_CONTROLLER = "work";

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.golem.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.golem.walk");
    private static final RawAnimation CARRY_IDLE_ANIM = RawAnimation.begin().thenLoop("animation.golem.carry_idle");
    private static final RawAnimation CARRY_WALK_ANIM = RawAnimation.begin().thenLoop("animation.golem.carry_walk");
    private static final RawAnimation ARM_SWING_RIGHT_ANIM = RawAnimation.begin().thenPlay("animation.golem.arm_swing_right");
    private static final RawAnimation AXE_CHOP_ANIM = RawAnimation.begin().thenPlay("animation.golem.axe_chop");
    private static final RawAnimation HOE_TILL_ANIM = RawAnimation.begin().thenPlay("animation.golem.hoe_till");
    private static final RawAnimation STOW_ANIM = RawAnimation.begin().thenPlay("animation.golem.stow");
    private static final RawAnimation UNSTOW_ANIM = RawAnimation.begin().thenPlay("animation.golem.unstow");

    /**
     * The triggerable animation that moves the tool from the hand to the back.
     */
    public static final String STOW = "stow";

    /**
     * The triggerable animation that moves the tool from the back to the hand.
     */
    public static final String UNSTOW = "unstow";

    private GolemAnimations() {
    }

    public static void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<GolemEntity>("base", 5, GolemAnimations::baseAnimationHandler),
                new AnimationController<GolemEntity>(WORK_CONTROLLER, 0, test -> PlayState.STOP)
                        .triggerableAnim("arm_swing_right", ARM_SWING_RIGHT_ANIM)
                        .triggerableAnim("axe_chop", AXE_CHOP_ANIM)
                        .triggerableAnim("hoe_till", HOE_TILL_ANIM)
                        .triggerableAnim(STOW, STOW_ANIM)
                        .triggerableAnim(UNSTOW, UNSTOW_ANIM)
        );
    }

    /**
     * @return the name of the work animation matching the tool. Work animations only move the job arm (right hand); the other arm
     * keeps hanging down from the base idle/walk animation.
     */
    public static String workAnimation(ItemStack tool) {
        if (tool.is(ItemTags.AXES)) {
            return "axe_chop";
        } else if (tool.is(ItemTags.HOES)) {
            return "hoe_till";
        } else {
            return "arm_swing_right";
        }
    }

    /**
     * The body animation: idle or walk, with the carrying variants while the golem holds an item between its hands.
     */
    private static PlayState baseAnimationHandler(AnimationTest<GolemEntity> test) {
        boolean carrying = test.animatable().isCarrying();
        RawAnimation animation;
        if (carrying) {
            animation = test.isMoving() ? CARRY_WALK_ANIM : CARRY_IDLE_ANIM;
        } else {
            animation = test.isMoving() ? WALK_ANIM : IDLE_ANIM;
        }

        return test.setAndContinue(animation);
    }
}
