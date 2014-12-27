/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipes.Items;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipe.MultiBlockCastingRecipe;
import Reika.ChromatiCraft.Registry.CrystalElement;

public class CrystalMirrorRecipe extends MultiBlockCastingRecipe {

	public CrystalMirrorRecipe(ItemStack out, ItemStack main) {
		super(out, main);

		this.addAuxItem(new ItemStack(Blocks.glass), -2, -2);
		this.addAuxItem(new ItemStack(Blocks.glass), 0, -2);
		this.addAuxItem(new ItemStack(Blocks.glass), 2, -2);

		this.addAuxItem(this.getChargedShard(CrystalElement.BLUE), -2, 0);
		this.addAuxItem(this.getChargedShard(CrystalElement.BLUE), 2, 0);

		this.addAuxItem(new ItemStack(Items.iron_ingot), -2, 2);
		this.addAuxItem(new ItemStack(Items.iron_ingot), 0, 2);
		this.addAuxItem(new ItemStack(Items.iron_ingot), 2, 2);
	}

}