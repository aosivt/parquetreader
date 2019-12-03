package ru.aosivt.rasterparquet.format;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverageio.gdal.BaseGDALGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.MismatchedDimensionException;
import ru.aosivt.rasterparquet.utils.ConverterFormat;

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

        if (source instanceof File) {
            String[] parameterQuery =
                    ConverterFormat.getParameterQuery(ConverterFormat.getUrl(hints));
            source = getSource(parameterQuery);
        } else if (source instanceof String) {
            String[] parameterQuery = ConverterFormat.getParameterQuery((String) source);
            return getParquetMetaDataReader(getSource(parameterQuery), hints);
        }

        return getParquetMetaDataReader(source, hints);
    }

    private ParquetMetaDataReader getParquetMetaDataReader(Object source, Hints hints) {
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

    private File getSource(String[] parameterQuery) {
        if (!Files.exists(
                Paths.get(
                        ConverterFormat.getConvertedPathString(
                                ConverterFormat.getNameFileImage(parameterQuery))))) {
            ConverterFormat.initConvert(parameterQuery);
        }
        return new File(
                ConverterFormat.getConvertedPathString(
                        ConverterFormat.getNameFileImage(parameterQuery)));
    }
}
