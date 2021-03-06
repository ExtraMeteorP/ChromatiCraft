/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.World.Dimension.Structure.Music;

import java.util.LinkedList;

import Reika.ChromatiCraft.Base.DimensionStructureGenerator;
import Reika.ChromatiCraft.Base.StructurePiece;
import Reika.DragonAPI.Instantiable.Worldgen.ChunkSplicedGenerationCache;
import Reika.DragonAPI.Libraries.MathSci.ReikaMusicHelper.MusicKey;

public class MusicPuzzle extends StructurePiece {

	private final LinkedList<MusicKey> melody = new LinkedList();

	MusicPuzzle(DimensionStructureGenerator s) {
		super(s);
	}

	@Override
	public void generate(ChunkSplicedGenerationCache world, int x, int y, int z) {

	}

}
