/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipes.Items;

import net.minecraft.item.ItemStack;
import Reika.ChromatiCraft.Auxiliary.Interfaces.ShardGroupingRecipe;
import Reika.ChromatiCraft.Auxiliary.RecipeManagers.CastingRecipe.MultiBlockCastingRecipe;
import Reika.ChromatiCraft.Registry.CrystalElement;

public class TransformationCoreRecipe extends MultiBlockCastingRecipe implements ShardGroupingRecipe {

	public TransformationCoreRecipe(ItemStack out, ItemStack main) {
		super(out, main);

		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), -2, -2);
		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), -4, -4);

		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), 2, -2);
		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), 4, -4);

		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), -2, 2);
		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), -4, 4);

		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), 2, 2);
		this.addAuxItem(this.getChargedShard(CrystalElement.GRAY), 4, 4);

		this.addAuxItem(this.getChargedShard(CrystalElement.BLACK), 2, 0);
		this.addAuxItem(this.getChargedShard(CrystalElement.BLACK), -2, 0);
		this.addAuxItem(this.getChargedShard(CrystalElement.BLACK), 0, 2);
		this.addAuxItem(this.getChargedShard(CrystalElement.BLACK), 0, -2);

		this.addRune(CrystalElement.GRAY, 3, 0, -2);
		this.addRune(CrystalElement.YELLOW, -3, 0, 2);
	}

}
