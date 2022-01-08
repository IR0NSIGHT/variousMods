package org.schema.schine.graphicsengine.forms.debug;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import com.bulletphysics.linearmath.Transform;
import me.iron.npccontrol.pathing.sm.StellarPosition;
import org.lwjgl.opengl.GL11;
import org.schema.common.util.linAlg.TransformTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;

public class DebugLine extends DebugGeometry {
	public Vector3f pointA;
	public Vector3f pointB;
	public StellarPosition spointA = new StellarPosition();
	public StellarPosition spointB= new StellarPosition();

	public DebugLine(Vector3f pointA, Vector3f pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
	}

	public DebugLine(PacketReadBuffer buffer) throws IOException {
		readFromBuffer(buffer);
	}

	public DebugLine(Vector3f pointA, Vector3f pointB, Vector4f color) {
		this(pointA, pointB);
		this.color = color;
	}

	public DebugLine(Vector3f pointA, Vector3f pointB, Vector4f color, long lifetime) {
		this(pointA, pointB);
		this.LIFETIME = lifetime;
		this.color = color;
	}

	public DebugLine(StellarPosition pointA, StellarPosition pointB, Vector4f color, long lifetime) {
		this(pointA.getPosition(), pointB.getPosition());
		this.spointA = pointA;
		this.spointB = pointB;
		this.LIFETIME = lifetime;
		this.color = color;
	}

	public void drawRaw() {
		Vector3i playerSector = GameClientState.instance.getPlayer().getCurrentSector();
		pointA = spointA.getRelativePosition(playerSector);
		pointB = spointB.getRelativePosition(playerSector);
		if (color != null) {
			GlUtil.glColor4f(color.x, color.y, color.z, color.w * getAlpha());
		} else {
			GlUtil.glColor4f(1, 1, 1, getAlpha());
		}
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(pointA.x, pointA.y, pointA.z);
		GL11.glVertex3f(pointB.x, pointB.y, pointB.z);
		GL11.glEnd();
	}
	public void draw() {
		GlUtil.glDisable(GL11.GL_TEXTURE_2D);
		GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glDisable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_BLEND);
		GlUtil.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		drawRaw();

		GlUtil.glDisable(GL11.GL_BLEND);
		GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glEnable(GL11.GL_TEXTURE_2D);
	}

	public StellarPosition getSpointA() {
		return spointA;
	}

	public StellarPosition getSpointB() {
		return spointB;
	}

	public void writeToBuffer(PacketWriteBuffer buffer) throws IOException {
		if (spointA == null) {
			spointA = new StellarPosition(new Vector3i(0,0,0),pointA);
		}
		if (spointB == null) {
			spointB = new StellarPosition(new Vector3i(0,0,0),pointB);
		}
		buffer.writeVector(spointA.getSector());
		buffer.writeVector3f(spointA.getPosition());
		buffer.writeVector(spointB.getSector());
		buffer.writeVector3f(spointB.getPosition());
		buffer.writeVector4f(color);
		buffer.writeLong(LIFETIME);
	}

	/**
	 * will create spehere and generate lines (if on client) from buffer.
	 * @param buffer
	 * @throws IOException
	 */
	public void readFromBuffer(PacketReadBuffer buffer) throws IOException {
		this.spointA.setSector(buffer.readVector());
		this.spointA.setPosition(buffer.readVector3f());
		this.spointB.setSector(buffer.readVector());
		this.spointB.setPosition(buffer.readVector3f());
		this.color = buffer.readVector4f();
		this.LIFETIME = buffer.readLong();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pointA.hashCode() + pointB.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((DebugLine) obj).pointA.equals(pointA) && ((DebugLine) obj).pointB.equals(pointB);
	}


	
	public static DebugLine[] getCross(Transform where, Vector3f local, float scaleX, float scaleY, float scaleZ, boolean fullCross) {

		Transform t;
		t = where;

		Vector3f up = GlUtil.getUpVector(new Vector3f(), t);
		Vector3f right = GlUtil.getRightVector(new Vector3f(), t);
		Vector3f forward = GlUtil.getForwardVector(new Vector3f(), t);

		Vector3f up1 = new Vector3f(up);
		Vector3f right1 = new Vector3f(right);
		Vector3f forward1 = new Vector3f(forward);
		
		up.set(0, 0, 0);
		right.set(0, 0, 0);
		forward.set(0, 0, 0);

		up1.scale(scaleX);
		right1.scale(scaleY);
		forward1.scale(scaleZ);

		up.add(t.origin);
		right.add(t.origin);
		forward.add(t.origin);
		up1.add(t.origin);
		right1.add(t.origin);
		forward1.add(t.origin);
		
		if(fullCross){
			up.sub(up1);
			right.sub(right1);
			forward.sub(forward1);
		}

		Vector3f m = new Vector3f(local);

		t.basis.transform(m);

		up.add(m);
		right.add(m);
		forward.add(m);
		up1.add(m);
		right1.add(m);
		forward1.add(m);

		Vector4f uC = new Vector4f(0, 1, 0, 1);
		Vector4f rC = new Vector4f(1, 0, 0, 1);
		Vector4f fC = new Vector4f(0, 0, 1, 1);

		DebugLine uLine = new DebugLine(up, up1, uC);
		DebugLine rLine = new DebugLine(right, right1, rC);
		DebugLine fLine = new DebugLine(forward, forward1, fC);

		return new DebugLine[]{uLine, rLine, fLine};

	}

	public static DebugLine[] getArrow(Vector3f from, Vector3f to, Vector4f color, float scaleMin, float scaleMax, float minCamDist, float maxCamDist, Transform currentWT) {
		
		DebugLine base = new DebugLine(from, to, color);
		
		Vector3f d = new Vector3f();
		d.sub(from, to);
		d.normalize();
		
		Vector3f camUp = Controller.getCamera().getUp(new Vector3f());
		
		Vector3f a = new Vector3f(to);
		a.add(camUp);
		a.add(d);
		
		Vector3f b = new Vector3f(to);
		camUp.negate();
		b.add(camUp);
		b.add(d);
		
		Vector3f ad = new Vector3f();
		ad.sub(a, to);
		ad.normalize();
		
		Vector3f bd = new Vector3f();
		bd.sub(b, to);
		bd.normalize();
		
		
		Transform f = TransformTools.ident;
		if(currentWT != null){
			f = currentWT;
		}
		Vector3f ttoo = new Vector3f(to);
		f.transform(ttoo);
		float camDist = Vector3fTools.diffLength(Controller.getCamera().getPos(), ttoo);
		
		float scale = 1;
		
		assert(minCamDist <= maxCamDist);
		if(camDist <= minCamDist){
			scale = scaleMin;
		}else if(camDist >= maxCamDist){
			scale = scaleMax;
		}else{
			//linear scale within margin
			float md = maxCamDist - minCamDist;
			float cd = camDist - minCamDist;
			if(md > 0){
				float pt = cd / md;
				float st = scaleMax - scaleMin;
				scale = scaleMin + st * pt;
			}
		}
		ad.scale(scale);
		bd.scale(scale);
		
		a.add(to, ad);
		b.add(to, bd);
		
		DebugLine tipA = new DebugLine(to, a, color);
		DebugLine tipB = new DebugLine(to, b, color);
		
		
		
		return new DebugLine[]{base, tipA, tipB};
	}


}
