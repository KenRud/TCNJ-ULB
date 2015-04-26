package edu.tcnj.ulb.dsp;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import mikera.vectorz.Vector3;

/**
 *  ====> Movement direction (theta = 0)
 *  ___ ___ ___   
 * |_6_|_3_|_0_| 
 * |_7_|_4_|_1_|
 * |_8_|_5_|_2_|
 * 
 * @author kruddick
 *
 */
public class PhasedArray {
	private static final double NODE_SEPARATION = 2;
	private static final double SPEED_OF_SOUND = 1500;
	private static final Vector3[] NODES = new Vector3[9];
	
	private final Vector3[] phasedNodes = new Vector3[9];
	
	static {
		int idx = 0;
		for (int x = 1; x > -1; x--) {
			for (int y = 1; y > -1; y--) {
				Vector3 v = NODES[idx++] = Vector3.of(x, y);
				v.scale(NODE_SEPARATION);
			}
		}
	}

	public PhasedArray(double phi, double theta) {
		Vector3 plane = Vector3.of(cos(theta) * cos(phi), sin(theta) * cos(phi), sin(phi));
		for (int i = 0; i < NODES.length; i++) {
			phasedNodes[i] = NODES[i].copy();
			phasedNodes[i].projectToPlane(plane, 1);
		}
	}
	
	public double getTimeDelay(int channel) {
		return phasedNodes[channel].getZ() / SPEED_OF_SOUND;
	}
}
