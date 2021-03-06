/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Magic.Interfaces;

import Reika.ChromatiCraft.Registry.CrystalElement;

public interface CrystalReceiver extends CrystalNetworkTile, EnergyBeamReceiver {

	/** Returns the amount successfully added. */
	public int receiveElement(CrystalElement e, int amt);

	public void onPathBroken(CrystalElement e);

	public int getReceiveRange();

	public void onPathCompleted();

	//public void markSource(WorldLocation loc);

}
