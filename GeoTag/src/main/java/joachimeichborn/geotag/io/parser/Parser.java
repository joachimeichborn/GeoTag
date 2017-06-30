package joachimeichborn.geotag.io.parser;

import java.nio.file.Path;

import joachimeichborn.geotag.model.Track;

public interface Parser {
	Track read(final Path aFile);
}
