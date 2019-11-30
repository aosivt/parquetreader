package ru.aosivt.rasterparquet.format;

import java.util.logging.Logger;
import org.geotools.coverageio.gdal.BaseGDALGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;

public class ParquetMetaDataReader extends BaseGDALGridCoverage2DReader
        implements GridCoverageReader {
    private static final String worldFileExt = ".wld";
    private static final Logger LOGGER = Logging.getLogger(ParquetDataImageReader.class);

    public ParquetMetaDataReader(Object input) throws DataSourceException {
        this(input, (Hints) null);
    }

    public ParquetMetaDataReader(Object input, Hints hints) throws DataSourceException {
        //

        super(input, hints, ".wld", new ParquetImageReaderSpi());
    }

    public Format getFormat() {
        return new ParquetImageFormat();
    }
}
