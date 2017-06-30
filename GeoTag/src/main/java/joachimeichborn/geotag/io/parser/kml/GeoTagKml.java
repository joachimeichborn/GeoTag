package joachimeichborn.geotag.io.parser.kml;

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
