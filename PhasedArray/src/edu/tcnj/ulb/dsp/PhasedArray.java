package edu.tcnj.ulb.dsp;

import static edu.tcnj.ulb.dsp.DataProcessor.SAMPLE_FREQUENCY;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import mikera.vectorz.Vector3;

/**
 * Simulates phasing an array of audio channels so that the structure points in a particular
 * direction. Phi represents the angle of inclination where 0 degrees represents a vector pointing
 * straight down. Theta represents the horizontal angle which corresponds to cardinal directions.
 * The diagram below establishes the required geometry of the audio channels.
 * 
 *  ====> Movement direction (theta = 0)
 *  ___ ___ ___   
 * |_6_|_3_|_0_| 
 * |_7_|_4_|_1_|
 * |_8_|_5_|_2_|
 * 
 * @author Kenneth Ruddick
 *
 */
public class PhasedArray {
	private static final double NODE_SEPARATION = 2;
	private static final double SPEED_OF_SOUND = 1500;
	private static final Vector3[] NODES = new Vector3[9];
	
	private int[] delays;
	
	private short[][] window;
	private int maxAdvance;
	private int maxDelay;
	
	static {
		int idx = 0;
		for (int x = 1; x >= -1; x--) {
			for (int y = 1; y >= -1; y--) {
				Vector3 v = NODES[idx++] = Vector3.of(x, y, 0);
				v.scale(NODE_SEPARATION);
			}
		}
	}

	public PhasedArray(double phi, double theta, short[][] window) {
		this.window = window;
		calculateChannelDelays(phi, theta);
		calculateMaxAdvance();
		calculateMaxDelay();
	}

	/**
	 * Combines all of the channels from the window with the appropriate phasing that corresponds
	 * to the direction of sensitivity. The combined signal is not normalized.
	 * @return A signal signal created by combining the phased channels. This signal is not
	 * normalized. The signal may be longer than each of the individual channels.
	 */
	public int[] combineChannels() {
		int sequenceLength = maxAdvance + maxDelay + window[0].length;
		int[] result = new int[sequenceLength];
		for (int channel = 0; channel < window.length; channel++) {
			short[] sequence = window[channel];
			int advance = delays[channel] + maxAdvance;
			for (int i = 0; i < sequence.length; i++) {
				result[i + advance] += sequence[i];
			}
		}
		return result;
	}
	
	private void calculateChannelDelays(double phi, double theta) {
		delays = new int[window.length];
		Vector3 plane = Vector3.of(cos(theta) * sin(phi), sin(theta) * sin(phi), cos(phi));
		for (int i = 0; i < NODES.length; i++) {
			Vector3 phasedNode = NODES[i].copy();
			// TODO Find out what the "distance" parameter means in Vector#projectToPlane()
			phasedNode.projectToPlane(plane, 0);
			delays[i] = (int) (SAMPLE_FREQUENCY * phasedNode.getZ() / SPEED_OF_SOUND);
		}
	}

	private void calculateMaxDelay() {
		maxDelay = delays[0];
		for (int i = 1; i < delays.length; i++) {
			maxDelay = Math.max(maxDelay, delays[i]);
		}
	}
	
	private void calculateMaxAdvance() {
		int min = delays[0];
		for (int i = 1; i < delays.length; i++) {
			min = Math.min(min, delays[i]);
		}
		maxAdvance = Math.abs(min);
	}
}
