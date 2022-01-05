package org.schema.schine.graphicsengine.forms.debug;

import com.bulletphysics.linearmath.Transform;
import me.iron.npccontrol.ModMain;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBoundingBox;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.graphicsengine.forms.simple.Box;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.LinkedList;
import java.util.Vector;

public class DebugDrawer {

	private static final Vector3f[][] verts = Box.init();
	public static Vector<DebugBoundingBox> boundingBoxes = new Vector<DebugBoundingBox>();
	public static Vector<DebugBox> boxes = new Vector<DebugBox>();
	public static Vector<DebugBoundingBox> boundingXses = new Vector<DebugBoundingBox>();
	public static Vector<DebugPoint> points = new Vector<DebugPoint>();
	public static Vector<DebugLine> lines = new Vector<DebugLine>();
	public static final Vector<DebugLine> myLines = new Vector<DebugLine>();
	static long lastClear = 0;
	static long lastClearPoints = 0;
	static long lastClearLines = 0;
	static long lastClearXses = 0;

	public static void debugDraw(int x, int y, int z, int halfDim, Transformable o) {
	/*	if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn()) {
			float scale = 0.51f;
			Transform t = new Transform(o.getWorldTransform());
			Vector3f p = new Vector3f();
			p.set(x, y, z);
			p.x -= halfDim;
			p.y -= halfDim;
			p.z -= halfDim;
			t.basis.transform(p);
			t.origin.add(p);
			DebugBox bo = new DebugBox(new Vector3f(-scale, -scale, -scale), new Vector3f(scale, scale, scale), t, 1, 0, 0, 1);
			bo.LIFETIME = 200;
			DebugDrawer.boxes.add(bo);
		}

	 */
	}

	public static void addArrowFromTransform(Transform t) {

	/*	Vector3f up = GlUtil.getUpVector(new Vector3f(), t);
		Vector3f right = GlUtil.getRightVector(new Vector3f(), t);
		Vector3f forward = GlUtil.getForwardVector(new Vector3f(), t);

		Vector3f up1 = new Vector3f(up);
		Vector3f right1 = new Vector3f(right);
		Vector3f forward1 = new Vector3f(forward);

		up.set(0, 0, 0);
		right.set(0, 0, 0);
		forward.set(0, 0, 0);

		up1.scale(3);
		right1.scale(3);
		forward1.scale(4);

		up.add(t.origin);
		right.add(t.origin);
		forward.add(t.origin);
		up1.add(t.origin);
		right1.add(t.origin);
		forward1.add(t.origin);

		Vector4f uC = new Vector4f(0, 0, 1, 1);
		Vector4f rC = new Vector4f(1, 0, 0, 1);
		Vector4f fC = new Vector4f(0, 1, 0, 1);

		DebugLine uLine = new DebugLine(up, up1, uC);
		DebugLine rLine = new DebugLine(right, right1, rC);
		DebugLine fLine = new DebugLine(forward, forward1, fC);
		if (!DebugDrawer.lines.contains(uLine)) {
			DebugDrawer.lines.add(uLine);
		}
		if (!DebugDrawer.lines.contains(rLine)) {
			DebugDrawer.lines.add(rLine);
		}
		if (!DebugDrawer.lines.contains(fLine)) {
			DebugDrawer.lines.add(fLine);
		}


	 */
	}

	public static void clear() {
		lines.clear();
		boundingXses.clear();
		for (int i = 0; i < boundingBoxes.size(); i++) {
			DebugGeometry g = boundingBoxes.get(i);
			if (!g.isAlive()) {
				//				System.err.println("point dead "+g.getTimeLived());
				boundingBoxes.remove(i);
				i--;
			}
		}
		for (int i = 0; i < boxes.size(); i++) {
			DebugGeometry g = boxes.get(i);
			if (!g.isAlive()) {
				//				System.err.println("point dead "+g.getTimeLived());
				boxes.remove(i);
				i--;
			}
		}

		for (int i = 0; i < points.size(); i++) {
			DebugGeometry g = points.get(i);
			if (!g.isAlive()) {
				//				System.err.println("point dead "+g.getTimeLived());
				points.remove(i);
				i--;
			}
		}

		for (int i = 0; i < myLines.size(); i++) {
			DebugGeometry g = myLines.get(i);
			if (!g.isAlive()) {
				myLines.remove(i);
				i--;
			}
		}
	}

	public static void drawBoundingBoxes() {
	/*	for (int j = 0; j < boundingBoxes.size(); j++) {
			DebugBoundingBox bb = boundingBoxes.get(j);
			assert (bb.bb.min != null && bb.bb.max != null);
			Vector3f[][] box = Box.getVertices(bb.bb.min, bb.bb.max, verts);

			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glDisable(GL12.GL_TEXTURE_3D);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL11.GL_TEXTURE_1D);
			//			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glColor4f(bb.getColor().x, bb.getColor().y, bb.getColor().z, bb.getColor().w);

			GL11.glBegin(GL11.GL_QUADS);
			for (int i = 0; i < box.length; i++) {
				for (int k = 0; k < box[i].length; k++) {
					GL11.glVertex3f(box[i][k].x, box[i][k].y, box[i][k].z);
				}
			}
			GL11.glEnd();
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			GlUtil.glDisable(GL11.GL_BLEND);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		} */
	}

	public static void drawBoundingXses() {
	/*	for (DebugBoundingBox bb : boundingXses) {

			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GlUtil.glDisable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_CULL_FACE);
			GlUtil.glEnable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glDisable(GL12.GL_TEXTURE_3D);
			GlUtil.glDisable(GL11.GL_TEXTURE_2D);
			GlUtil.glDisable(GL11.GL_TEXTURE_1D);
			//			GlUtil.glDisable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_BLEND);
			GlUtil.glColor4f(bb.getColor().x, bb.getColor().y, bb.getColor().z, bb.getColor().w);
			Vector3f startA = new Vector3f(bb.bb.min);
			startA.x = bb.bb.max.x;
			startA.z = bb.bb.max.z;
			Vector3f endA = new Vector3f(bb.bb.max);
			endA.x = bb.bb.min.x;
			endA.z = bb.bb.min.z;

			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3f(startA.x, startA.y, startA.z);
			GL11.glVertex3f(endA.x, endA.y, endA.z);

			GL11.glVertex3f(bb.bb.min.x, bb.bb.min.y, bb.bb.min.z);
			GL11.glVertex3f(bb.bb.max.x, bb.bb.max.y, bb.bb.max.z);

			GL11.glEnd();
			GlUtil.glEnable(GL11.GL_DEPTH_TEST);
			GlUtil.glEnable(GL11.GL_LIGHTING);
			GlUtil.glDisable(GL11.GL_COLOR_MATERIAL);
			GlUtil.glEnable(GL11.GL_CULL_FACE);
			GlUtil.glDisable(GL11.GL_BLEND);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}

	 */
	}

	public static void drawBoxes() {
	/*	synchronized (boxes) {
			for (int i = boxes.size() - 1; i >= 0; i--) {
				boxes.get(i).draw();
			}
		}

	 */
	}

	public static void drawLines() {
		clear();
		synchronized (myLines) {
			for (DebugLine line : myLines) {
				line.draw();
			}
		}
	}

	public static void clearLines() {
		if (GameServerState.instance!=null) {
			DebugPacket p= new DebugPacket();
			p.setClear(true);
			p.sendToAll();
		}
	}

	public static void drawPoints() {
	/*	synchronized (points) {
			for (int i = points.size() - 1; i >= 0; i--) {
				points.get(i).draw();
			}
		}

	 */
	}

}
