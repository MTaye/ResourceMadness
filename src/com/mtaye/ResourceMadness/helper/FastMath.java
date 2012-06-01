package com.mtaye.ResourceMadness.helper;

public final class FastMath {
	private static final float RAD;
	private static final float DEG;

	public static final float atan2Deg(float y, float x) {
		return FastMath.atan2(y, x) * DEG;
	}

	public static final float atan2DegStrict(float y, float x) {
		return (float) Math.atan2(y, x) * DEG;
	}

	public static final float atan2(float y, float x) {
		float add, mul;

		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;

				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}

			add = -3.141592653f;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}

			add = 0.0f;
		}

		float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);

		int xi = (int) (x * invDiv);
		int yi = (int) (y * invDiv);

		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
	}

	private static final int ATAN2_BITS = 0;

	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

	private static final float ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);

	private static final float[] atan2 = new float[ATAN2_COUNT];

	static {
		RAD = (float) Math.PI / 180.0f;
		DEG = 180.0f / (float) Math.PI;
		for (int i = 0; i < ATAN2_DIM; i++) {
			for (int j = 0; j < ATAN2_DIM; j++) {
				float x0 = (float) i / ATAN2_DIM;
				float y0 = (float) j / ATAN2_DIM;

				atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
			}
		}
	}

}
