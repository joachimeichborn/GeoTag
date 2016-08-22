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

package joachimeichborn.geotag.io.kml;

import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.xml.sax.ContentHandler;

import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * {@link Kml} throws a {@link PropertyException} during marshaling when trying
 * to set property "com.sun.xml.bind.namespacePrefixMapper". This class wraps
 * {@link Kml} to provide a working marshaling mechanism
 * 
 * @author Joachim von Eichborn
 */
public class GeoTagKml extends Kml {
	private static final Logger logger = Logger.getLogger(GeoTagKml.class.getSimpleName());

	private Marshaller marshaller;
	private JAXBContext jaxbContext;

	@Override
	public boolean marshal(final Writer aWriter) {
		try {
			marshaller = this.createMarshaller();
			marshaller.marshal((Kml) this, aWriter);
			return true;
		} catch (final JAXBException e) {
			logger.log(Level.SEVERE, "Exception while marshalling KML: " + e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean marshal() {
		throw new UnsupportedOperationException("Not supported, use marshal(Writer) instead");
	}

	@Override
	public boolean marshal(final ContentHandler aContenthandler) {
		throw new UnsupportedOperationException("Not supported, use marshal(Writer) instead");
	}

	@Override
	public boolean marshal(final OutputStream aOutputstream) {
		throw new UnsupportedOperationException("Not supported, use marshal(Writer) instead");
	}

	private Marshaller createMarshaller() throws JAXBException {
		if (marshaller == null) {
			marshaller = this.getJaxbContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		return marshaller;
	}

	private JAXBContext getJaxbContext() throws JAXBException {
		if (jaxbContext == null) {
			jaxbContext = JAXBContext.newInstance((Kml.class));
		}
		return jaxbContext;
	}
}
