/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Block.Dimension;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.Interfaces.DecoType;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaItems;
import Reika.ChromatiCraft.Render.ISBRH.DimensionDecoRenderer;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;

public class BlockDimensionDeco extends Block {

	private static final IIcon[][] icons = new IIcon[2][16];

	public static enum DimDecoTypes implements DecoType {
		MIASMA(),
		FLOATSTONE();

		public static DimDecoTypes[] list = values();

		public ItemStack getItem() {
			return new ItemStack(ChromaBlocks.DIMGEN.getBlockInstance(), 1, this.ordinal());
		}

		public boolean hasBlockRender() {
			return this == FLOATSTONE;
		}

		public IIcon getOverlay() {
			return icons[1][this.ordinal()];
		}
	}

	public BlockDimensionDeco(Material mat) {
		super(mat);
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return icons[0][meta];
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		for (int i = 0; i < 16; i++) {
			icons[0][i] = ico.registerIcon("chromaticraft:dimgen/underlay_"+i);
			icons[1][i] = ico.registerIcon("chromaticraft:dimgen/overlay_"+i);
		}
	}

	@Override
	public final int getRenderType() {
		return ChromatiCraft.proxy.dimgenRender;
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public final int getRenderBlockPass() {
		return 1;
	}

	@Override
	public Item getItemDropped(int meta, Random r, int fortune) {
		return ChromaItems.DIMGEN.getItemInstance();
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public final boolean canRenderInPass(int pass) {
		DimensionDecoRenderer.renderPass = pass;
		return pass <= 1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return DimDecoTypes.list[meta].hasBlockRender() ? ReikaAABBHelper.getBlockAABB(x, y, z) : null;
	}



}
