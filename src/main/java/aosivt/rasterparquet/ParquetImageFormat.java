package aosivt.rasterparquet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.URLs;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;

public class ParquetImageFormat extends AbstractGridFormat implements Format {
    private static final Set<String> TIFF_WFILE_EXT;
    private static final Set<String> PARQUET_WFILE_EXT;
    private static final Logger LOGGER;
    public static final ParameterDescriptor<String> FORMAT;

    public ParquetImageFormat() {
        this.setInfo();
    }

    private void setInfo() {
        HashMap<String, String> info = new HashMap();
        info.put("name", "ParquetImage");
        info.put("description", "A raster view from parquet file");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://joyreactor.cc");
        info.put("version", "0.0.0");
        this.mInfo = info;
        this.readParameters =
                new ParameterGroup(
                        new DefaultParameterDescriptorGroup(
                                this.mInfo,
                                new GeneralParameterDescriptor[] {READ_GRIDGEOMETRY2D}));
        this.writeParameters =
                new ParameterGroup(
                        new DefaultParameterDescriptorGroup(
                                this.mInfo, new GeneralParameterDescriptor[] {FORMAT}));
    }

    public ParquetImageReader getReader(Object source) {
        return this.getReader(source, (Hints) null);
    }

    public GridCoverageWriter getWriter(Object destination) {
        return new ParquetImageWriter(destination);
    }

    public GridCoverageWriter getWriter(Object destination, Hints hints) {
        return new ParquetImageWriter(destination, hints);
    }

    public boolean accepts(Object input, Hints hints) {
        String pathname = "";
        if (input instanceof URL) {
            URL url = (URL) input;
            String protocol = url.getProtocol();
            if (protocol.equalsIgnoreCase("file")) {
                pathname = URLs.urlToFile(url).getPath();
            } else if (protocol.equalsIgnoreCase("http")) {
                String query;
                try {
                    query = URLDecoder.decode(url.getQuery().intern(), "UTF-8");
                } catch (UnsupportedEncodingException var21) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, var21.getLocalizedMessage(), var21);
                    }

                    return false;
                }

                if (query.toLowerCase().intern().indexOf("getmap") == -1) {
                    return false;
                }

                return true;
            }
        } else if (input instanceof File) {
            File file = (File) input;
            pathname = file.getAbsolutePath();
        } else {
            if (!(input instanceof String)) {
                return false;
            }

            pathname = (String) input;
        }

        if (!pathname.toLowerCase().endsWith(".tif")
                && !pathname.toLowerCase().endsWith(".tiff")
                && !pathname.endsWith(".parquet")) {
            return false;
        } else {
            try {
                ImageInputStream is = ImageIO.createImageInputStream(new File(pathname));
                Throwable var24 = null;

                boolean var7;
                try {
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
                    var7 = readers.hasNext();
                } catch (Throwable var18) {
                    var24 = var18;
                    throw var18;
                } finally {
                    if (is != null) {
                        if (var24 != null) {
                            try {
                                is.close();
                            } catch (Throwable var17) {
                                var24.addSuppressed(var17);
                            }
                        } else {
                            is.close();
                        }
                    }
                }

                return var7;
            } catch (IOException var20) {
                return false;
            }
        }
    }

    public static Set<String> getWorldExtension(String fileExtension) {
        if (fileExtension == null) {
            throw new NullPointerException("Provided input is null");
        } else if (fileExtension.toLowerCase().equals("tif")
                || fileExtension.toLowerCase().equals("tiff")) {
            return TIFF_WFILE_EXT;
        } else if (fileExtension.toLowerCase().equals("parquet")) {
            return PARQUET_WFILE_EXT;
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }
    }

    public ParquetImageReader getReader(Object source, Hints hints) {
        try {
            return new ParquetImageReader(source, hints);
        } catch (DataSourceException var4) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, var4.getLocalizedMessage(), var4);
            }

            return null;
        }
    }

    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }

    static {
        Set<String> tempSet = new HashSet(2);
        tempSet.add(".tfw");
        tempSet.add(".tiffw");
        TIFF_WFILE_EXT = Collections.unmodifiableSet(tempSet);

        tempSet = new HashSet(1);
        tempSet.add(".parquet");
        PARQUET_WFILE_EXT = Collections.unmodifiableSet(tempSet);

        LOGGER = Logging.getLogger(ParquetImageFormat.class);
        FORMAT =
                DefaultParameterDescriptor.create(
                        "Format",
                        "Indicates the output format forthis image",
                        String.class,
                        "png",
                        true);
    }
}
