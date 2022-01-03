package org.schema.game.server.ai.program.searchanddestroy;

import me.iron.npccontrol.triggers.Utility;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.ai.program.common.states.ShootAtTarget;
import org.schema.game.server.ai.program.common.states.Waiting;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyProgram;
import org.schema.game.server.ai.program.searchanddestroy.states.*;
import org.schema.game.server.ai.program.searchanddestroy.states.EvadingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.Rally;
import org.schema.game.server.ai.program.searchanddestroy.states.SeachingForTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipEngagingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipGettingToTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipMovingToSector;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipShootAtTarget;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.*;

import javax.vecmath.Vector3f;

public class SimpleSearchAndDestroyMachine extends FiniteStateMachine<String> {
	//parameters where to move, ship will constantly try to assume these params.
	Vector3i moveSector = new Vector3i(32,80,0); //move to this sector
	Vector3f movePos = new Vector3f(VoidSystem.SYSTEM_SIZE_HALF,VoidSystem.SYSTEM_SIZE_HALF,VoidSystem.SYSTEM_SIZE_HALF); //move to this pos in the sector
	Vector3f moveDir = new Vector3f(); //assume this rotational vector at the pos

	public SimpleSearchAndDestroyMachine(AiEntityStateInterface obj, SimpleSearchAndDestroyProgram program) {
		super(obj, program, "");
	}

	public void addTransition(State from, Transition t, State to) {
		from.addTransition(t, to);

	}

	@Override
	public void update() throws FSMException {
		//TODO remove transitions that appear in all states to be handled globally, not by individual states
		//if health low -> rally
		//if stop -> wait
		//if restart -> wait
		//addTransition(getFsm().getCurrentState(), Transition.RESTART,);


		super.update();
	}

	@Override
	public void createFSM(String parameter) {
		AiEntityStateInterface gObj = getObj();

		//movement machine
		org.schema.game.server.ai.program.searchanddestroy.states.ShipMovingToSector movingToSector = new ShipMovingToSector(gObj);
		setStartingState(movingToSector);
	}

	@Override
	public void onMsg(Message message) {

	}

	public Vector3i getMoveSector() {
		return moveSector;
	}

	public void setMoveSector(Vector3i moveSector) {
		this.moveSector = moveSector;
	}

	public Vector3f getMovePos() {
		return movePos;
	}

	public void setMovePos(Vector3f movePos) {
		this.movePos = movePos;
	}

	public Vector3f getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(Vector3f moveDir) {
		this.moveDir = moveDir;
	}
}
