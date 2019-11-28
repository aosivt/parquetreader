package aosivt.rasterparquet;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageReader;


public class ParquetImageReaderSpi extends GDALImageReaderSpi {
    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.envihdr");
    static final String[] suffixes = new String[]{"parquet"};
    static final String[] formatNames = new String[]{"GDAL Geo Parquet Hdr", "GGPH"};
    static final String[] MIMETypes = new String[]{"image/bil", "image/bip", "image/bsq"};
    static final String version = "1.0";
    static final String description = "GDAL Geo Parquet, version 0.0.0";
    static final String readerCN = "aosivt.rasterparquet.ParquetImageReader";
    static final String vendorName = "Alexandr Oshchepkov";
    static final String[] wSN = new String[]{null};
    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = null;
    static final String nativeStreamMetadataFormatClassName = null;
    static final String[] extraStreamMetadataFormatNames = new String[]{null};
    static final String[] extraStreamMetadataFormatClassNames = new String[]{null};
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName = null;
    static final String nativeImageMetadataFormatClassName = null;
    static final String[] extraImageMetadataFormatNames = new String[]{null};
    static final String[] extraImageMetadataFormatClassNames = new String[]{null};

    public ParquetImageReaderSpi() {
        super(vendorName, version, formatNames, suffixes,
                                                MIMETypes,
                                                readerCN, new Class[] {File.class, FileImageInputStreamExt.class},
                wSN, supportsStandardImageMetadataFormat,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                supportsStandardStreamMetadataFormat,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames,
                Collections.singletonList("GGPH"));
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("ParquetImageReaderSpi Constructor");
        }

    }

    public ImageReader createReaderInstance(Object source) throws IOException {
        return new ParquetImageReader(this);
    }

    public String getDescription(Locale locale) {
        return description;
    }
}
