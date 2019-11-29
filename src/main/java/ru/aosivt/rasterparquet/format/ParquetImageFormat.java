package ru.aosivt.rasterparquet.format;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverageio.gdal.BaseGDALGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.MismatchedDimensionException;

public class ParquetImageFormat extends BaseGDALGridFormat implements Format {
    private static final Logger LOGGER = Logging.getLogger(ParquetImageFormat.class);
    private static InfoWrapper INFO = new InfoWrapper("RasterParquet with GeoTrans", "GGRP");

    public ParquetImageFormat() {
        super(new ParquetImageReaderSpi());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Creating a new EnviHdrFormat.");
        }

        this.setInfo();
    }

    protected void setInfo() {
        this.setInfo(INFO);
    }

    public ParquetMetaDataReader getReader(Object source, Hints hints) {
        RuntimeException re;
        try {
            return new ParquetMetaDataReader(source, hints);
        } catch (MismatchedDimensionException var5) {
            re = new RuntimeException();
            re.initCause(var5);
            throw re;
        } catch (DataSourceException var6) {
            re = new RuntimeException();
            re.initCause(var6);
            throw re;
        }
    }
}
