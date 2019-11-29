package ru.aosivt.rasterparquet.format;

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

    private static final Logger LOGGER = Logger.getLogger("ru.aosivt.rasterparquet");
    static final String[] suffixes = new String[] {"parquet", "bil", "bip", "bsq", "img"};
    static final String[] formatNames = new String[] {"Parquet labeled", "PARQUETRASTER"};
    static final String[] MIMETypes = new String[] {"image/bil", "image/bip", "image/bsq"};
    static final String version = "1.0";
    static final String description = "Parquet labeled Image Reader, version 0.0.0";
    static final String readerCN = "ru.aosivt.rasterparquet.ParquetImageReader";
    static final String vendorName = "Alexander Oshchepkov";
    static final String[] wSN = new String[] {null};
    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = null;
    static final String nativeStreamMetadataFormatClassName = null;
    static final String[] extraStreamMetadataFormatNames = new String[] {null};
    static final String[] extraStreamMetadataFormatClassNames = new String[] {null};
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName = null;
    static final String nativeImageMetadataFormatClassName = null;
    static final String[] extraImageMetadataFormatNames = new String[] {null};
    static final String[] extraImageMetadataFormatClassNames = new String[] {null};

    public ParquetImageReaderSpi() {
        super(
                vendorName,
                version,
                formatNames,
                suffixes,
                MIMETypes,
                readerCN,
                new Class[] {File.class, FileImageInputStreamExt.class},
                wSN,
                false,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                false,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames,
                Collections.singletonList("ENVI"));

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("ParquetImageReaderSpi Constructor");
        }
    }

    public ImageReader createReaderInstance(Object source) throws IOException {
        return new ParquetDataImageReader(this);
    }

    public String getDescription(Locale locale) {
        return "Parquet Data Image Reader, version 0.0.0";
    }
}
