package com.cosmicdan.reignadditionals.blocks;

import java.util.Random;

import com.cosmicdan.reignadditionals.blocks.tileentities.TileEntityCampfire;
import com.cosmicdan.reignadditionals.client.renderers.ModRenderers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCampfireLit extends BlockContainer {
    
    private IIcon[] campfireIcon = new IIcon[6];
    private final Random random = new Random();
    
    public BlockCampfireLit() {
        super(Material.fire);
        setBlockName("campfireLit");
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }
    
    // eject tile entity inventory 
    @Override
    public void breakBlock(World world, int posX, int posY, int posZ, Block block, int metadata) {
        if (!world.isRemote && metadata > 0) {
            TileEntityCampfire tileEntity = (TileEntityCampfire) world.getTileEntity(posX, posY, posZ);
            ItemStack itemStack;
            EntityItem entityItem;
            float ranX;
            float ranY;
            float ranZ;
            for (int i = 0; i < tileEntity.itemSlotStatus.length; i++) {
                itemStack = tileEntity.itemSlot[i];
                if (itemStack != null) {
                    itemStack = itemStack.copy();
                    itemStack.stackSize = 1;
                    ranX = this.random.nextFloat() * 0.8F + 0.1F;
                    ranY = this.random.nextFloat() * 0.8F + 0.1F;
                    ranZ = this.random.nextFloat() * 0.8F + 0.1F;
                    entityItem = new EntityItem(world, (double)((float)posX + ranX), (double)((float)posY + ranY), (double)((float)posZ + ranZ), itemStack);
                    entityItem.motionX = (double)((float)this.random.nextGaussian() * 0.05F);
                    entityItem.motionY = (double)((float)this.random.nextGaussian() * 0.05F);
                    entityItem.motionZ = (double)((float)this.random.nextGaussian() * 0.05F);
                    world.spawnEntityInWorld(entityItem);
                }
            }
        }
        super.breakBlock(world, posX, posY, posZ, block, metadata);
    }
    
    @Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        // don't want to drop the campfire itself
        return null;
    }
    
    
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCampfire(world, metadata);
    }
    
    @Override
    public boolean onBlockActivated(World world, int posX, int posY, int posZ, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntityCampfire tileEntity = (TileEntityCampfire) world.getTileEntity(posX, posY, posZ);
        if (!world.isRemote) {
            if (player.getHeldItem() != null) {
                int blockMeta = world.getBlockMetadata(posX, posY, posZ);
                if (blockMeta > 0 ) {
                    // check for foods to add
                    Item item = player.getHeldItem().getItem();
                    if (item instanceof ItemFood) {
                        // check for cookable meats
                        ItemStack itemStack = player.getHeldItem();
                        
                        if (tileEntity.tryAddItem(itemStack))
                            --itemStack.stackSize;
                        
                        else // slots are all full, save them time and return an item instead
                            tryRemoveItem(world, player, tileEntity);
                    }
                    else if(item == Items.stick) {
                        if (tileEntity.addFuel())
                            --player.getHeldItem().stackSize;
                    }
                    else // not a valid input item, assume the player is trying to remove an item
                        tryRemoveItem(world, player, tileEntity);
                }
            }
            else // player hand is empty, try to remove an item for them
                tryRemoveItem(world, player, tileEntity);
        }
        return true;
    }

    private void tryRemoveItem(World world, EntityPlayer player, TileEntityCampfire tileEntity) {
        ItemStack itemStack = null;
        for (int i = 0; i < tileEntity.itemSlotStatus.length; i++) {
            itemStack = tileEntity.tryRemoveItem();
            if (itemStack != null) {
                EntityItem dropItem = new EntityItem(world, player.posX, player.posY, player.posZ, itemStack.copy());
                dropItem.delayBeforeCanPickup = 0;
                world.spawnEntityInWorld(dropItem);
                // shift-click = remove only one item
                if (player.isSneaking()) break;
            }
        }
    }
    

    @Override
    public int getLightValue(IBlockAccess world, int posX, int posY, int posZ) {
        int currentMeta = world.getBlockMetadata(posX, posY, posZ);
        
        if (currentMeta == 0 || currentMeta == 7)
            return 0;
        
        if(currentMeta > 7)
            return currentMeta + 2;
        else
            return world.getBlockMetadata(posX, posY, posZ) + 9;
    }
   
    /*
     *  transparency stuff (so the blocks surrounding it don't go see-through)
     */
    public boolean renderAsNormalBlock() {
         return false;
    }
    
    public boolean isOpaqueCube() {
         return false;
    }
    
    public int getRenderType() {
        return ModRenderers.CAMPFIRE;
    }
    
    /*
     * Icon stuff for textures
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int icon, int option) {
        if (icon < 4) {
            if (option < 7)
                return this.campfireIcon[icon];
            else if (icon < 2)
                return this.campfireIcon[icon + 4];
        }
        return this.campfireIcon[0];
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegistry) {
        this.campfireIcon[0] = iconRegistry.registerIcon("reignadditionals:campfire_side0");
        this.campfireIcon[1] = iconRegistry.registerIcon("reignadditionals:campfire_side1");
        this.campfireIcon[2] = iconRegistry.registerIcon("reignadditionals:campfire_side2");
        this.campfireIcon[3] = iconRegistry.registerIcon("reignadditionals:campfire_side3");
        this.campfireIcon[4] = iconRegistry.registerIcon("reignadditionals:campfire_strut");
        this.campfireIcon[5] = iconRegistry.registerIcon("reignadditionals:campfire_spitrod");
    }
}
