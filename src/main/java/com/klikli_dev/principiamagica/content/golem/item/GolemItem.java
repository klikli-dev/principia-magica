// SPDX-FileCopyrightText: 2026 klikli-dev
//
// SPDX-License-Identifier: MIT

package com.klikli_dev.principiamagica.content.golem.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.klikli_dev.principiamagica.content.golem.client.GolemItemRenderer;
import com.klikli_dev.principiamagica.content.golem.entity.GolemEntity;
import com.klikli_dev.principiamagica.content.golem.entity.GolemStorage;
import com.klikli_dev.principiamagica.content.golem.progression.GolemProgression;
import com.klikli_dev.principiamagica.content.golem.spec.GolemMaterial;
import com.klikli_dev.principiamagica.content.golem.spec.GolemSpec;
import com.klikli_dev.principiamagica.content.golem.workplace.Workplace;
import com.klikli_dev.principiamagica.registry.DataComponentRegistry;
import com.klikli_dev.principiamagica.registry.EntityTypeRegistry;
import com.klikli_dev.principiamagica.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A golem that is not placed. It is rendered with the golem model, textured by the material of its spec.
 */
public class GolemItem extends Item implements GeoItem {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.golem.idle");

    private final AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    public GolemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GolemItem>("base", 5, test -> test.setAndContinue(IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private @Nullable GeoItemRenderer<GolemItem> renderer;

            @Override
            public GeoItemRenderer<GolemItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GolemItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    public static ItemStack createStack(GolemSpec spec) {
        ItemStack stack = new ItemStack(ItemRegistry.GOLEM.get());
        stack.set(DataComponentRegistry.GOLEM_SPEC.get(), spec);
        return stack;
    }

    /**
     * Places the golem at the clicked block. A golem picked up with a stylus keeps its workplace, re-anchored to the new position.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
        if (spec == null) {
            return InteractionResult.FAIL;
        }

        GolemEntity golem = EntityTypeRegistry.GOLEM.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (golem == null) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        golem.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player != null ? player.getYRot() : 0.0F, 0.0F);
        golem.setSpec(spec);
        golem.setHealth(golem.getMaxHealth());
        golem.setAnchor(GlobalPos.of(serverLevel.dimension(), pos));

        Workplace stored = stack.get(DataComponentRegistry.GOLEM_WORKPLACE.get());
        if (stored != null) {
            golem.updateWorkplace(current -> StylusActions.toAbsolute(stored, pos));
        }

        GolemStorage storage = stack.get(DataComponentRegistry.GOLEM_STORAGE.get());
        if (storage != null) {
            storage.restoreTo(golem);
        }

        if (player != null) {
            golem.setOwner(player);
            stack.consume(1, player);
        } else {
            stack.shrink(1);
        }

        serverLevel.addFreshEntity(golem);
        if (player instanceof ServerPlayer serverPlayer) {
            GolemProgression.onGolemPlaced(serverPlayer);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
        if (spec == null) {
            return super.getName(stack);
        }

        return Component.translatable("item.principiamagica.golem.named", Component.translatable(spec.material().value().getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
        GolemSpec spec = stack.get(DataComponentRegistry.GOLEM_SPEC.get());
        if (spec == null) {
            return;
        }

        appendSpecTooltip(spec, adder);
    }

    public static void appendSpecTooltip(GolemSpec spec, Consumer<Component> adder) {
        GolemMaterial material = spec.material().value();
        adder.accept(Component.translatable("tooltip.principiamagica.golem.material",
                Component.translatable(material.getDescriptionId())).withStyle(ChatFormatting.GRAY));
    }
}
