/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Base;

import net.minecraft.world.World;

public abstract class DynamicStructurePiece extends StructureElement {

	protected final int posY;

	protected DynamicStructurePiece(DimensionStructureGenerator s, int y) {
		super(s);
		posY = y;
	}

	public abstract void generate(World world, int x, int z);

}
