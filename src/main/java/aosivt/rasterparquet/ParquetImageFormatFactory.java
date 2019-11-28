package aosivt.rasterparquet;

import java.util.Collections;
import java.util.Map;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

public class ParquetImageFormatFactory implements GridFormatFactorySpi {
    public ParquetImageFormatFactory() {}

    public ParquetImageFormat createFormat() {
        return new ParquetImageFormat();
    }

    public boolean isAvailable() {
        boolean available = true;

        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
        } catch (ClassNotFoundException var3) {
            available = false;
        }

        return available;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
