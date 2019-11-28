package aosivt.rasterparquet;

import it.geosolutions.imageio.plugins.envihdr.ENVIHdrImageReader;
import it.geosolutions.imageio.plugins.envihdr.ENVIHdrImageReaderSpi;

public class ParquetImageReader extends ENVIHdrImageReader {
    public ParquetImageReader(ENVIHdrImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }
}
