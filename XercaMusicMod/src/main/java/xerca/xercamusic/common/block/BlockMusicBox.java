package xerca.xercamusic.common.block;

import net.minecraft.block.*;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import xerca.xercamusic.common.item.ItemInstrument;
import xerca.xercamusic.common.item.Items;
import xerca.xercamusic.common.tile_entity.TileEntityMusicBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMusicBox extends HorizontalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty POWERING = BooleanProperty.create("powering");
    public static final BooleanProperty HAS_MUSIC = BooleanProperty.create("has_music");
    public static final BooleanProperty HAS_INSTRUMENT = BooleanProperty.create("has_instrument");

    public BlockMusicBox() {
        super(Properties.create(Material.WOOD).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD));
        this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, false).
                with(HAS_MUSIC, false).with(HAS_INSTRUMENT, false).with(HORIZONTAL_FACING, Direction.NORTH).with(POWERING, false));
        this.setRegistryName("music_box");
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(POWERED, context.getWorld().isBlockPowered(context.getPos())).
                with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(worldIn != null && !worldIn.isRemote){
            boolean powered = worldIn.isBlockPowered(pos);
            if(powered && state.get(POWERING)){
                return;
            }
            if (powered != state.get(POWERED)) {
                worldIn.setBlockState(pos, state.with(POWERED, powered), 2);
            }
        }
    }

    private void ejectItem(World world, BlockPos pos, BlockState state, boolean isMusic, boolean isBreaking) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof TileEntityMusicBox) {
                TileEntityMusicBox te = (TileEntityMusicBox)tileentity;
                ItemStack itemstack = isMusic ? te.getNoteStack().copy() : new ItemStack(te.getInstrument());
                if (!itemstack.isEmpty()) {
                    if (!isBreaking) {
                        if (isMusic) {
                            te.removeNoteStack();
                            world.setBlockState(pos, state.with(HAS_MUSIC, Boolean.FALSE), 3);
                        }
                        else {
                            te.removeInstrument();
                            world.setBlockState(pos, state.with(HAS_INSTRUMENT, Boolean.FALSE), 3);
                        }
                    }

                    ItemEntity entityitem;
//                    ItemStack itemstack1 = itemstack.copy();
                    if(isMusic){
                        entityitem = new ItemEntity(world, (double)pos.getX(), (double)pos.getY() + 1, (double)pos.getZ(), itemstack);
                        entityitem.setMotion(world.rand.nextDouble() * 0.2 - 0.1, 0.1, world.rand.nextDouble() * 0.2 - 0.1);
                    }
                    else{
                        Direction backFace = state.get(HORIZONTAL_FACING).getOpposite();
                        int xOffset = backFace.getXOffset();
                        int zOffset = backFace.getZOffset();

                        entityitem = new ItemEntity(world, pos.getX() + xOffset*0.625, pos.getY() + 0.5D, pos.getZ() + zOffset*0.625, itemstack);
                        double speed = world.rand.nextDouble() * 0.1 + 0.2;
                        entityitem.setMotion(xOffset * speed, 0.1, zOffset * speed);
                    }

                    entityitem.setDefaultPickupDelay();
                    world.addEntity(entityitem);
                }
            }
        }
    }

    public static void insertMusic(World worldIn, BlockPos pos, BlockState state, ItemStack noteStack) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityMusicBox) {
            ((TileEntityMusicBox) tileentity).setNoteStack(noteStack, true);
            worldIn.setBlockState(pos, state.with(HAS_MUSIC, Boolean.TRUE), 3);
        }
    }

    public static void insertInstrument(World worldIn, BlockPos pos, BlockState state, Item instrument) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityMusicBox) {
            ((TileEntityMusicBox) tileentity).setInstrument(instrument);
            worldIn.setBlockState(pos, state.with(HAS_INSTRUMENT, Boolean.TRUE), 3);
        }
        worldIn.playEvent(null, 1012, pos, 0); // play door close sound
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (hit.getFace() == Direction.UP && state.get(HAS_MUSIC)) {
            if(heldItem.getItem() instanceof ItemInstrument && !state.get(HAS_INSTRUMENT)){
                return ActionResultType.PASS;
            }
            ejectItem(worldIn, pos, state, true, false);
            return ActionResultType.SUCCESS;
        } else if (hit.getFace() == state.get(HORIZONTAL_FACING).getOpposite() && state.get(HAS_INSTRUMENT)) {
            if(heldItem.getItem() == Items.MUSIC_SHEET && !state.get(HAS_MUSIC)){
                return ActionResultType.PASS;
            }
            worldIn.playEvent(null, 1006, pos, 0); //play door open sound
            ejectItem(worldIn, pos, state, false, false);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != Blocks.MUSIC_BOX || newState.getBlock() != Blocks.MUSIC_BOX) {
            ejectItem(worldIn, pos, state, true, true);
            ejectItem(worldIn, pos, state, false, true);

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED, HAS_MUSIC, HAS_INSTRUMENT, HORIZONTAL_FACING, POWERING);
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.getWeakPower(blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (!blockState.get(POWERING)) {
            return 0;
        } else {
            return blockState.get(HORIZONTAL_FACING).rotateYCCW() == side ? 15 : 0;
        }
    }

    // This block should NOT check for weak power, otherwise it transmits input to output and also gets powered by itself
    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side)
    {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityMusicBox();
    }
}
