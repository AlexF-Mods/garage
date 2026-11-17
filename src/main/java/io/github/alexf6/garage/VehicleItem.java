package io.github.alexf6.garage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ítem que coloca un vehículo del mod en el mundo conservando su color teñido y su nombre personalizado.
 * Las embarcaciones ({@code watercraft = true}) también pueden colocarse directamente sobre el agua.
 */
public class VehicleItem extends Item {

    private final EntityType<? extends MotorVehicleEntity> entityType;
    private final boolean watercraft;

    public VehicleItem(EntityType<? extends MotorVehicleEntity> entityType, boolean watercraft, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.watercraft = watercraft;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!this.watercraft) return super.use(level, player, hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        if (!level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) return InteractionResult.PASS;
        if (level instanceof ServerLevel serverLevel && !player.mayInteract(serverLevel, hit.getBlockPos())) {
            return InteractionResult.FAIL;
        }
        Vec3 loc = hit.getLocation();
        return place(level, player.getItemInHand(hand), player, new Vec3(loc.x, loc.y - 0.12, loc.z), player.getYRot());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Vec3 spot = Vec3.atBottomCenterOf(context.getClickedPos().relative(context.getClickedFace()));
        return place(context.getLevel(), context.getItemInHand(), context.getPlayer(), spot, context.getRotation());
    }

    private InteractionResult place(Level level, ItemStack stack, Player player, Vec3 spot, float yRot) {
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        MotorVehicleEntity vehicle = this.entityType.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (vehicle == null) return InteractionResult.FAIL;

        vehicle.setPos(spot.x, spot.y, spot.z);
        vehicle.setYRot(yRot);
        if (!level.noCollision(vehicle, vehicle.getBoundingBox())) return InteractionResult.FAIL;

        vehicle.setColor(DyedItemColor.getOrDefault(stack, vehicle.defaultColor()));
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            vehicle.setCustomName(stack.getHoverName());
        }

        serverLevel.addFreshEntity(vehicle);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, spot);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
