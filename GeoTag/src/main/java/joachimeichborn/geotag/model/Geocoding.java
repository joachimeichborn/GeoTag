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

package joachimeichborn.geotag.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable representation of geo coding information
 * 
 * @author Joachim von Eichborn
 */
public class Geocoding implements PropertyChangeListener {
	public static class Builder {
		private String builderLocationName;
		private String builderCity;
		private String builderSublocation;
		private String builderProvinceState;
		private String builderCountryCode;
		private String builderCountryName;

		public final Builder setLocationName(final String aLocationName) {
			if (!StringUtils.isEmpty(aLocationName)) {
				builderLocationName = aLocationName;
			}
			return this;
		}

		public final Builder setCity(final String aCity) {

			if (!StringUtils.isEmpty(aCity)) {
				builderCity = aCity;
			}
			return this;
		}

		public final Builder setSublocation(final String aSublocation) {
			if (!StringUtils.isEmpty(aSublocation)) {
				builderSublocation = aSublocation;
			}
			return this;
		}

		public final Builder setProvinceState(final String aProvinceState) {
			if (!StringUtils.isEmpty(aProvinceState)) {
				builderProvinceState = aProvinceState;
			}
			return this;
		}

		public final Builder setCountryCode(final String aCountryCode) {
			if (!StringUtils.isEmpty(aCountryCode)) {
				builderCountryCode = aCountryCode.toUpperCase();
			}
			return this;
		}

		public final Builder setCountryName(final String aCountryName) {
			if (!StringUtils.isEmpty(aCountryName)) {
				builderCountryName = aCountryName;
			}
			return this;
		}

		/**
		 * Builds a {@link Geocoding} object if at least one property is set.
		 * 
		 * If some properties are <code>null</code>, they are converted to the
		 * empty string in the built object.
		 * 
		 * @return the built object or <code>null</code> if all provided
		 *         properties were <code>null</code>
		 */
		public Geocoding build() {
			if (builderLocationName == null && builderCity == null && builderSublocation == null
					&& builderProvinceState == null && builderCountryCode == null && builderCountryName == null) {
				return null;
			}

			if (builderLocationName == null) {
				builderLocationName = "";
			}
			if (builderCity == null) {
				builderCity = "";
			}
			if (builderSublocation == null) {
				builderSublocation = "";
			}
			if (builderProvinceState == null) {
				builderProvinceState = "";
			}
			if (builderCountryCode == null) {
				builderCountryCode = "";
			}
			if (builderCountryName == null) {
				builderCountryName = "";
			}

			return new Geocoding(this);
		}
	}

	public static final String LOCATION_NAME_PROPERTY = "locationName";
	public static final String CITY_PROPERTY = "city";
	public static final String SUBLOCATION_PROPERTY = "sublocation";
	public static final String PROVINCE_STATE_PROPERTY = "provinceState";
	public static final String COUNTRY_CODE_PROPERTY = "countryCode";
	public static final String COUNTRY_NAME_PROPERTY = "countryName";
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	private String locationName;
	private String city;
	private String sublocation;
	private String provinceState;
	private String countryCode;
	private String countryName;

	private Geocoding(final Builder aBuilder) {
		locationName = aBuilder.builderLocationName;
		city = aBuilder.builderCity;
		sublocation = aBuilder.builderSublocation;
		provinceState = aBuilder.builderProvinceState;
		countryCode = aBuilder.builderCountryCode;
		countryName = aBuilder.builderCountryName;
	}

	public final String getLocationName() {
		return locationName;
	}

	public final String getCity() {
		return city;
	}

	public final String getSublocation() {
		return sublocation;
	}

	public final String getProvinceState() {
		return provinceState;
	}

	public final String getCountryCode() {
		return countryCode;
	}

	public final String getCountryName() {
		return countryName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
		result = prime * result + ((countryName == null) ? 0 : countryName.hashCode());
		result = prime * result + ((locationName == null) ? 0 : locationName.hashCode());
		result = prime * result + ((provinceState == null) ? 0 : provinceState.hashCode());
		result = prime * result + ((sublocation == null) ? 0 : sublocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object aObj) {
		if (this == aObj)
			return true;
		if (aObj == null)
			return false;
		if (getClass() != aObj.getClass())
			return false;
		Geocoding other = (Geocoding) aObj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (countryCode == null) {
			if (other.countryCode != null)
				return false;
		} else if (!countryCode.equals(other.countryCode))
			return false;
		if (countryName == null) {
			if (other.countryName != null)
				return false;
		} else if (!countryName.equals(other.countryName))
			return false;
		if (locationName == null) {
			if (other.locationName != null)
				return false;
		} else if (!locationName.equals(other.locationName))
			return false;
		if (provinceState == null) {
			if (other.provinceState != null)
				return false;
		} else if (!provinceState.equals(other.provinceState))
			return false;
		if (sublocation == null) {
			if (other.sublocation != null)
				return false;
		} else if (!sublocation.equals(other.sublocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GeoCoding [mLocationName=" + locationName + ", mCity=" + city + ", mSublocation=" + sublocation
				+ ", mProvinceState=" + provinceState + ", mCountryCode=" + countryCode + ", mCountryName="
				+ countryName + "]";
	}

	public void addPropertyChangeListener(final String aPropertyName, final PropertyChangeListener aListener) {
		propertyChangeSupport.addPropertyChangeListener(aPropertyName, aListener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener aListener) {
		propertyChangeSupport.removePropertyChangeListener(aListener);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent aEvent) {
		propertyChangeSupport.firePropertyChange(aEvent.getPropertyName(), aEvent.getOldValue(), aEvent.getNewValue());
	}
}
