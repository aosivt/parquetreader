package ru.aosivt.rasterparquet.format;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.CatalogRepository;
import org.geotools.coverageio.gdal.BaseGDALGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.MismatchedDimensionException;
import ru.aosivt.rasterparquet.errors.CountParameterQuery;
import ru.aosivt.rasterparquet.utils.ConverterFomat;

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

            String[] parameterQuery = getParameterQuery(hints);

            if (!Files.exists(
                    Paths.get(
                            ConverterFomat.NAME_SYSTEM_TEMP_DIR.concat(
                                    getNameFileImage(parameterQuery))))) {
                ConverterFomat.initConvert(
                        getPath(parameterQuery), getNameFileImage(parameterQuery));
            }

            source =
                    new File(
                            ConverterFomat.NAME_SYSTEM_TEMP_DIR.concat(
                                    getNameFileImage(parameterQuery)));
        }

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

    protected String[] getParameterQuery(Hints hints) {
        String[] pathParameter =
                ((CatalogRepository) hints.get(Hints.REPOSITORY))
                        .getCatalog()
                        .getCoverageStores()
                        .get(0)
                        .getURL()
                        .split("[?]")[1]
                        .split("&");
        if (pathParameter.length < 4) {
            throw new CountParameterQuery("count parameter don`t не достаточно)))");
        }
        return pathParameter;
    }

    protected String getPath(String[] parameterQuery) {
        return String.format(
                "%s//%s/%s/",
                FORMAT_QUERY.TYPE_FS.get(parameterQuery),
                FORMAT_QUERY.HOST.get(parameterQuery),
                FORMAT_QUERY.PATH.get(parameterQuery));
    }

    protected String getNameFileImage(String[] parameterQuery) {
        return FORMAT_QUERY.NAME_FILE.get(parameterQuery);
    }
}
