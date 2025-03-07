/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core.ansi;

import org.springframework.util.Assert;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility for working with {@link AnsiColor} in the context of {@link Color AWT Colors}.
 *
 * @author Craig Burke
 * @author Ruben Dijkstra
 * @author Phillip Webb
 * @author Michael Simons
 * @since 1.4.0
 */
public final class AnsiColors {

	private static final Map<AnsiColor, LabColor> ANSI_COLOR_MAP;

	static {
		Map<AnsiColor, LabColor> colorMap = new LinkedHashMap<AnsiColor, LabColor>();
		colorMap.put(AnsiColor.BLACK, new LabColor(0x000000));
		colorMap.put(AnsiColor.RED, new LabColor(0xAA0000));
		colorMap.put(AnsiColor.GREEN, new LabColor(0x00AA00));
		colorMap.put(AnsiColor.YELLOW, new LabColor(0xAA5500));
		colorMap.put(AnsiColor.BLUE, new LabColor(0x0000AA));
		colorMap.put(AnsiColor.MAGENTA, new LabColor(0xAA00AA));
		colorMap.put(AnsiColor.CYAN, new LabColor(0x00AAAA));
		colorMap.put(AnsiColor.WHITE, new LabColor(0xAAAAAA));
		colorMap.put(AnsiColor.BRIGHT_BLACK, new LabColor(0x555555));
		colorMap.put(AnsiColor.BRIGHT_RED, new LabColor(0xFF5555));
		colorMap.put(AnsiColor.BRIGHT_GREEN, new LabColor(0x55FF00));
		colorMap.put(AnsiColor.BRIGHT_YELLOW, new LabColor(0xFFFF55));
		colorMap.put(AnsiColor.BRIGHT_BLUE, new LabColor(0x5555FF));
		colorMap.put(AnsiColor.BRIGHT_MAGENTA, new LabColor(0xFF55FF));
		colorMap.put(AnsiColor.BRIGHT_CYAN, new LabColor(0x55FFFF));
		colorMap.put(AnsiColor.BRIGHT_WHITE, new LabColor(0xFFFFFF));
		ANSI_COLOR_MAP = Collections.unmodifiableMap(colorMap);
	}

	private AnsiColors() {
	}

	public static AnsiColor getClosest(Color color) {
		return getClosest(new LabColor(color));
	}

	private static AnsiColor getClosest(LabColor color) {
		AnsiColor result = null;
		double resultDistance = Float.MAX_VALUE;
		for (Entry<AnsiColor, LabColor> entry : ANSI_COLOR_MAP.entrySet()) {
			double distance = color.getDistance(entry.getValue());
			if (result == null || distance < resultDistance) {
				resultDistance = distance;
				result = entry.getKey();
			}
		}
		return result;
	}

	/**
	 * Represents a color stored in LAB form.
	 */
	private static final class LabColor {

		private static final ColorSpace XYZ_COLOR_SPACE = ColorSpace
				.getInstance(ColorSpace.CS_CIEXYZ);

		private final double l;

		private final double a;

		private final double b;

		LabColor(Integer rgb) {
			this(rgb == null ? (Color) null : new Color(rgb));
		}

		LabColor(Color color) {
			Assert.notNull(color, "Color must not be null");
			float[] lab = fromXyz(color.getColorComponents(XYZ_COLOR_SPACE, null));
			this.l = lab[0];
			this.a = lab[1];
			this.b = lab[2];
		}

		private float[] fromXyz(float[] xyz) {
			return fromXyz(xyz[0], xyz[1], xyz[2]);
		}

		private float[] fromXyz(float x, float y, float z) {
			double l = (f(y) - 16.0) * 116.0;
			double a = (f(x) - f(y)) * 500.0;
			double b = (f(y) - f(z)) * 200.0;
			return new float[] { (float) l, (float) a, (float) b };
		}

		private double f(double t) {
			return (t > (216.0 / 24389.0) ? Math.cbrt(t)
					: (1.0 / 3.0) * Math.pow(29.0 / 6.0, 2) * t + (4.0 / 29.0));
		}

		// See http://en.wikipedia.org/wiki/Color_difference#CIE94
		public double getDistance(LabColor other) {
			double c1 = Math.sqrt(this.a * this.a + this.b * this.b);
			double deltaC = c1 - Math.sqrt(other.a * other.a + other.b * other.b);
			double deltaA = this.a - other.a;
			double deltaB = this.b - other.b;
			double deltaH = Math.sqrt(
					Math.max(0.0, deltaA * deltaA + deltaB * deltaB - deltaC * deltaC));
			return Math.sqrt(Math.max(0.0,
					Math.pow((this.l - other.l) / (1.0), 2)
							+ Math.pow(deltaC / (1 + 0.045 * c1), 2)
							+ Math.pow(deltaH / (1 + 0.015 * c1), 2.0)));
		}

	}

}
