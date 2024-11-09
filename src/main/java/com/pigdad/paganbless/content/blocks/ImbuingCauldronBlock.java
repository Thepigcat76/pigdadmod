package com.pigdad.paganbless.content.blocks;

import com.mojang.serialization.MapCodec;
import com.pigdad.paganbless.registries.PBBlockEntities;
import com.pigdad.paganbless.content.blockentities.ImbuingCauldronBlockEntity;
import com.pigdad.paganbless.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

@SuppressWarnings({"deprecation", "OptionalUsedAsFieldOrParameterType"})
public class ImbuingCauldronBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = com.pigdad.paganbless.utils.BlockStateProperties.ACTIVE;
    public static final VoxelShape SHAPE = Stream.of(
            Block.box(1, 2.5, 1, 15, 7.5, 15),
            Block.box(3, 0, 3, 13, 2, 13),
            Block.box(2, 7.5, 2, 14, 9.5, 14),
            Block.box(2, 1.5, 2, 14, 2.5, 14)
    ).reduce(Shapes::or).get();

    public ImbuingCauldronBlock(Properties p_49224_) {
        super(p_49224_);
        registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ImbuingCauldronBlock::new);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof ImbuingCauldronBlockEntity blockEntity1) {
                blockEntity1.drop();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        p_49915_.add(ACTIVE);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, PBBlockEntities.IMBUING_CAULDRON.get(),
                (pLevel, pPos, pState, pBlockEntity) -> {
                    if (pLevel.isClientSide()) pBlockEntity.clientTick();
                    else pBlockEntity.serverTick();
                }
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ImbuingCauldronBlockEntity(p_153215_, p_153216_);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack p_316304_, BlockState p_316362_, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult p_316140_) {
        ImbuingCauldronBlockEntity blockEntity = (ImbuingCauldronBlockEntity) level.getBlockEntity(blockPos);
        IFluidHandlerItem fluidHandlerItem = player.getItemInHand(interactionHand).getCapability(Capabilities.FluidHandler.ITEM);
        ItemStackHandler itemHandler = blockEntity.getItemHandler();
        FluidTank fluidHandler = blockEntity.getFluidTank();
        if (player.isShiftKeyDown()) {
            blockEntity.turn();
            if (blockEntity.getFluidTank().getFluidAmount() >= 800) {
                if (blockEntity.getFluidTank().getFluidInTank(0).is(Fluids.WATER)) {
                    level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.PLAYER_SWIM, SoundSource.PLAYERS, 0.2F, 1.0F);
                }
            } else {
                level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.2F, 1.0F);
            }
            blockEntity.setProgress(blockEntity.getProgress() + 5);
            return ItemInteractionResult.SUCCESS;
        }

        if (!player.isShiftKeyDown()) {
            OptionalInt slot = getHitSlot(p_316140_);
            return insertAndExtract(player, level, interactionHand, itemHandler, fluidHandler, fluidHandlerItem, slot);
        }
        return ItemInteractionResult.FAIL;
    }

    private OptionalInt getHitSlot(BlockHitResult hitResult) {
        OptionalInt optionalInt = getRelativeHitCoordinatesForBlockFace(hitResult).map((pos) -> {
            int i = pos.y >= 0.5F ? 0 : 1;
            if (i == 0 && pos.x > 0.5f) {
                return OptionalInt.of(3);
            }
            int j = getSection(pos.x);
            return OptionalInt.of(5 - (j + i * 3));
        }).orElseGet(OptionalInt::empty);
        if (optionalInt.isPresent() && optionalInt.getAsInt() == 5) {
            return OptionalInt.of(4);
        }
        return optionalInt;
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        if (direction != Direction.UP) {
            return Optional.empty();
        } else {
            BlockPos blockpos = hitResult.getBlockPos().relative(direction);
            Vec3 vec3 = hitResult.getLocation().subtract(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            float x = (float) vec3.x();
            float z = (float) vec3.z();

            return Optional.of(new Vec2(z, x));
        }
    }

    private static int getSection(float x) {
        if (x < 0.375F) {
            return 0;
        } else {
            return x < 0.6875F ? 1 : 2;
        }
    }

    private static ItemInteractionResult insertAndExtract(Player player, Level level, InteractionHand interactionHand, IItemHandler itemHandler, FluidTank fluidHandler, IFluidHandler fluidHandlerItem, OptionalInt slot) {
        if (!player.getItemInHand(interactionHand).isEmpty() && fluidHandlerItem == null) {
            insert(player, interactionHand, itemHandler, slot);
            return ItemInteractionResult.SUCCESS;
        } else if (player.getItemInHand(interactionHand).isEmpty()) {
            extract(player, itemHandler, slot);
            return ItemInteractionResult.SUCCESS;
        }

        return fluidIO(player.getItemInHand(interactionHand), player, interactionHand, fluidHandler, fluidHandlerItem);
    }

    private static ItemInteractionResult fluidIO(ItemStack stack, Player player, InteractionHand hand, IFluidHandler tankFluidHandler, IFluidHandler itemFluidHandler) {
        if (itemFluidHandler != null && !(stack.getItem() instanceof BucketItem)) {
            FluidStack fluidInItemTank = itemFluidHandler.getFluidInTank(0);
            IFluidHandler fluidHandler0 = tankFluidHandler;
            IFluidHandler fluidHandler1 = itemFluidHandler;

            if (!fluidInItemTank.isEmpty()) {
                fluidInItemTank.getFluid().getPickupSound().ifPresent(player::playSound);
                fluidHandler0 = itemFluidHandler;
                fluidHandler1 = tankFluidHandler;
            } else {
                SoundEvent sound = tankFluidHandler.getFluidInTank(0).getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if (sound != null) {
                    player.playSound(sound);
                }
            }

            FluidStack drained = fluidHandler0.drain(fluidHandler0.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE);
            int filled = fluidHandler1.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            fluidHandler0.fill(drained.copyWithAmount(drained.getAmount() - filled), IFluidHandler.FluidAction.EXECUTE);

            return ItemInteractionResult.SUCCESS;
        } else if (itemFluidHandler != null && stack.getItem() instanceof BucketItem) {
            FluidStack fluidInItemTank = itemFluidHandler.getFluidInTank(0);
            if (fluidInItemTank.isEmpty() && tankFluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE).getAmount() == 1000) {
                ItemStack filledBucket = ItemUtils.createFilledResult(stack, player, tankFluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE).getFluid().getBucket().getDefaultInstance());
                player.setItemInHand(hand, filledBucket);
                tankFluidHandler.getFluidInTank(0).getFluid().getPickupSound().ifPresent(player::playSound);
                return ItemInteractionResult.SUCCESS;
            } else if (!fluidInItemTank.isEmpty() && tankFluidHandler.fill(fluidInItemTank.copyWithAmount(1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                tankFluidHandler.fill(fluidInItemTank.copyWithAmount(1000), IFluidHandler.FluidAction.EXECUTE);
                ItemStack emptyBucket = ItemUtils.createFilledResult(stack, player, BucketItem.getEmptySuccessItem(stack, player));
                player.setItemInHand(hand, emptyBucket);
                SoundEvent sound = tankFluidHandler.getFluidInTank(0).getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if (sound != null) {
                    player.playSound(sound);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.FAIL;
    }

    private static void insert(Player player, InteractionHand interactionHand, IItemHandler itemHandler, OptionalInt slot) {
        if (slot.isPresent()) {
            int slot1 = slot.getAsInt();
            ItemStack remainder = itemHandler.insertItem(slot1, player.getItemInHand(interactionHand).copy(), false);
            player.setItemInHand(interactionHand, remainder);
        }
    }

    private static void extract(Player player, IItemHandler itemHandler, OptionalInt slot) {
        if (slot.isPresent()) {
            int slot1 = slot.getAsInt();
            if (!itemHandler.getStackInSlot(5).isEmpty()) {
                slot1 = 5;
            }
            ItemStack remainder = itemHandler.extractItem(slot1, itemHandler.getStackInSlot(slot1).getCount(), false);
            ItemHandlerHelper.giveItemToPlayer(player, remainder, player.getInventory().selected);
        }
    }
}
