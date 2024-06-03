package com.pigdad.paganbless.registries.blockentities;

import com.pigdad.paganbless.registries.PBBlockEntities;
import com.pigdad.paganbless.registries.PBTags;
import com.pigdad.paganbless.registries.items.BolineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HerbalistBenchBlockEntity extends ContainerBlockEntity {
    private int cuts;

    public HerbalistBenchBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PBBlockEntities.HERBALIST_BENCH.get(), blockPos, blockState);
        addItemHandler(2, (slot, stack) -> switch (slot) {
            case 0 -> !(stack.is(PBTags.ItemTags.PAGAN_TOOLS));
            case 1 -> stack.is(PBTags.ItemTags.PAGAN_TOOLS);
            default -> false;
        });
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);
        setCuts(0);
    }

    public int getCuts() {
        return cuts;
    }

    public void setCuts(int cuts) {
        this.cuts = cuts;
    }

    public void incrCuts() {
        setCuts(getCuts() + 1);
    }

    @Override
    protected void saveOther(CompoundTag tag) {
        super.saveOther(tag);
        tag.putInt("cuts", this.cuts);
    }

    @Override
    protected void loadOther(CompoundTag tag) {
        super.loadOther(tag);
        this.cuts = tag.getInt("cuts");
    }
}