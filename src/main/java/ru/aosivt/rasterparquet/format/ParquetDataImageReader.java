package ru.aosivt.rasterparquet.format;

import it.geosolutions.imageio.gdalframework.GDALImageReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParquetDataImageReader extends GDALImageReader {
    private static final Logger LOGGER = Logger.getLogger("ru.aosivt.rasterparquet.format");

    public ParquetDataImageReader(ParquetImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("ParquetDataImage Constructor");
        }
    }
}
