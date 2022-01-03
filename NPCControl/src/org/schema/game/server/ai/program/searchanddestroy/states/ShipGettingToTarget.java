package org.schema.game.server.ai.program.searchanddestroy.states;

import org.schema.game.server.ai.program.common.states.GettingToTarget;
import org.schema.game.server.ai.program.turret.states.ShootingProcessInterface;
import org.schema.schine.ai.AiEntityStateInterface;

public class ShipGettingToTarget extends GettingToTarget implements ShootingProcessInterface {

	

	public ShipGettingToTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	

}
