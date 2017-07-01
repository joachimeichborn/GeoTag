/*
GeoTag

Copyright (C) 2015  Joachim von Eichborn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package joachimeichborn.geotag.io.writer.kml;

import java.util.LinkedList;
import java.util.List;

//The MIT License
//
// Copyright (c) 2007 Nick Galbreath, (c) 2014 Joachim von Eichborn
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
//
// Version 3 - 31-Aug-2014  Ported to Java, limited to spherical circles
// Version 2 - 12-Sept-2007 Simplified XML output
//                          Added commandline interface
// Version 1 - 10-Sept-2007 Initial release
//

/**
 * Compute a circle-shaped polygon with a specified radius around a given
 * position.
 * 
 * @author Joachim von Eichborn
 */
public class CirclePolygon {
	static final double RAD = Math.PI / 180;

	/**
	 * Mean radius of the earth
	 */
	 static final double EARTH_RADIUS = 6_371_000;

	/**
	 * The number of corners that should be used to draw the circle polygon
	 */
	private static final double CORNER_COUNT = 15;

	/**
	 * Represents a position as cartesian coordinates
	 * 
	 * @author Joachim von Eichborn
	 */
	final static class CartesianPoint {
		final private double x;
		final private double y;
		final private double z;

		/**
		 * Constuctor
		 * 
		 * @param aX
		 * 
		 *            The X-axis value
		 * @param aY
		 *            The Y-axis value
		 * @param aZ
		 *            The Z-axis value
		 */
		private CartesianPoint(final double aX, final double aY, final double aZ) {
			x = aX;
			y = aY;
			z = aZ;
		}

		/**
		 * Factory to create a cartesian point on the unit circle form a radian
		 * 
		 * @param aRadian
		 * @return The corresponding cartesian point
		 */
		static CartesianPoint fromRadian(final Radian aRadian) {
			double phi = Math.PI / 2 - (aRadian.getLatitude() * RAD);
			double theta = (aRadian.getLongitude() * RAD);
			return new CartesianPoint(Math.cos(theta) * Math.sin(phi), Math.sin(theta) * Math.sin(phi), Math.cos(phi));
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getZ() {
			return z;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(z);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CartesianPoint other = (CartesianPoint) obj;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CartesianPoint [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
	}

	/**
	 * Represents a position as radian
	 * 
	 * @author Joachim von Eichborn
	 */
	final public static class Radian {
		private static final double DEG = 180 / Math.PI;
		
		final private double latitude;
		final private double longitude;

		/**
		 * Constuctor
		 * 
		 * @param aLatitude
		 *            The latitude value
		 * @param aLongitude
		 *            The longitude value
		 */
		public Radian(final double aLatitude, final double aLongitude) {
			latitude = aLatitude;
			longitude = aLongitude;
		}

		/**
		 * Factory to get radian values from a cartesian point
		 * 
		 * @param aCartesianPoint
		 *            The cartesian point
		 * @return The corresponding radian values
		 */
		static Radian fromCartesian(final CartesianPoint aCartesianPoint) {
			double longitude = Math.PI / 2;
			if (aCartesianPoint.x != 0) {
				longitude = Math.atan(aCartesianPoint.y / aCartesianPoint.x);
			}
			double latitude = (Math.PI / 2 - Math.acos(aCartesianPoint.z));

			if (aCartesianPoint.x < 0.0) {
				if (aCartesianPoint.y < 0.0) {
					longitude = -(Math.PI - longitude);
				} else {
					longitude = Math.PI + longitude;
				}
			}

			return new Radian(latitude * DEG, longitude * DEG);
		}

		/**
		 * Get the longitude value
		 * 
		 * @return The longitude value
		 */
		public double getLongitude() {
			return longitude;
		}

		/**
		 * Get the latitude value
		 * 
		 * @return The latitude value
		 */
		public double getLatitude() {
			return latitude;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(latitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(longitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Radian other = (Radian) obj;
			if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
				return false;
			if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Radian [latitude=" + latitude + ", longitude=" + longitude + "]";
		}
	}

	/**
	 * Calculate points that form a circle with the given radius around the
	 * given position on the surface of the earth
	 * 
	 * @param aLatitude
	 *            The position's latitude
	 * @param aLongitude
	 *            The position's longitude
	 * @param aRadius
	 *            The radius in meters
	 * @return A list of radian values describing the points on the surface of
	 *         the earth that form the desired circle.
	 */
	public static List<Radian> calculateCirclePoints(final double aLatitude, final double aLongitude,
			final double aRadius) {
		double r = (aRadius / (EARTH_RADIUS * Math.cos(aLatitude * RAD))) / RAD;
		final CartesianPoint vec = CartesianPoint.fromRadian(new Radian(aLatitude, aLongitude));
		final CartesianPoint pt = CartesianPoint.fromRadian(new Radian(aLatitude, aLongitude + r));
		final List<Radian> pts = new LinkedList<Radian>();
		for (int i = 0; i < CORNER_COUNT; i++) {
			pts.add(Radian.fromCartesian(rotatePoint(vec, pt, (2 * Math.PI / CORNER_COUNT) * i)));
		}
		pts.add(pts.get(0));
		return pts;
	}

	private static CartesianPoint rotatePoint(final CartesianPoint aVector, final CartesianPoint aPoint,
			final double aPhi) {
		double a = aVector.x * aPoint.x + aVector.y * aPoint.y + aVector.z * aPoint.z;
		double d = Math.cos(aPhi);
		double e = Math.sin(aPhi);

		double x = a * aVector.x + (aPoint.x - a * aVector.x) * d + (aVector.y * aPoint.z - aVector.z * aPoint.y) * e;
		double y = a * aVector.y + (aPoint.y - a * aVector.y) * d + (aVector.z * aPoint.x - aVector.x * aPoint.z) * e;
		double z = a * aVector.z + (aPoint.z - a * aVector.z) * d + (aVector.x * aPoint.y - aVector.y * aPoint.x) * e;

		return new CartesianPoint(x, y, z);
	}

}