package ru.aosivt.rasterparquet.format;

import it.geosolutions.imageio.plugins.envihdr.ENVIHdrImageReaderSpi;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverageio.BaseGridFormatFactorySPI;
import org.geotools.util.logging.Logging;

public class ParquetImageFormatFactory extends BaseGridFormatFactorySPI
        implements GridFormatFactorySpi {
    private static final Logger LOGGER = Logging.getLogger(ParquetImageFormatFactory.class);

    public ParquetImageFormatFactory() {}

    public boolean isAvailable() {
        boolean available = true;

        try {
            Class.forName("ru.aosivt.rasterparquet.format.ParquetImageReaderSpi");
            available = (new ENVIHdrImageReaderSpi()).isAvailable();
            if (LOGGER.isLoggable(Level.FINE)) {
                if (available) {
                    LOGGER.fine("ParquetImageFormatFactory is available.");
                } else {
                    LOGGER.fine("ParquetImageFormatFactory is not available.");
                }
            }
        } catch (ClassNotFoundException var3) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ParquetImageFormatFactory is not available.");
            }

            available = false;
        }

        return available;
    }

    public ParquetImageFormat createFormat() {
        return new ParquetImageFormat();
    }
}
