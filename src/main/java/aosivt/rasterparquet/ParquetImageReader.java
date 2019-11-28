package aosivt.rasterparquet;

import it.geosolutions.imageio.gdalframework.GDALImageReader;
import it.geosolutions.imageio.plugins.envihdr.ENVIHdrImageReaderSpi;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ParquetImageReader extends GDALImageReader {
    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.envihdr");

    public ParquetImageReader(ParquetImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("ENVIHdrImageReader Constructor");
        }

    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input,seekForwardOnly,ignoreMetadata);
    }

}
