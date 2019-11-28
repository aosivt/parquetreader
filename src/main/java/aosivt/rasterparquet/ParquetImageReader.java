package aosivt.rasterparquet;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import org.apache.avro.generic.GenericRecord;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.FileGroupProvider;
import org.geotools.data.PrjFileReader;
import org.geotools.data.WorldFileReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.io.ImageIOExt;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.util.factory.Hints;
import org.geotools.util.logging.Logging;
import org.kitesdk.data.*;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.TransformException;

public class ParquetImageReader extends AbstractGridCoverage2DReader
        implements GridCoverage2DReader {
    private Logger LOGGER;
    private boolean wmsRequest;
    private boolean metaFile;
    private String parentPath;
    private String extension;
    private ImageReaderSpi readerSPI;

    public ParquetImageReader(Object input) throws DataSourceException {
        this(input, (Hints) null);
    }

    public ParquetImageReader(Object input, Hints hints) throws DataSourceException {
        this.LOGGER = Logging.getLogger(ParquetImageReader.class);
        if (input == null) {
            IOException ex = new IOException("WorldImage:No source set to read this coverage.");
            this.LOGGER.logp(
                    Level.SEVERE,
                    ParquetImageReader.class.toString(),
                    "ParquetImageReader",
                    ex.getLocalizedMessage(),
                    ex);
            throw new DataSourceException(ex);
        } else {

            String resourcePath = ((File) input).getParentFile().getAbsolutePath();

            org.kitesdk.data.Dataset<GenericRecord> satelliteImageSet =
                    Datasets.load(String.format("dataset:file://%s", resourcePath));

            DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();
            Iterator<GenericRecord> ds = reader.iterator();

            CreateImageImageIO ci = new CreateImageImageIO(7899, 8010);
            int rowId = 0;
            while (ds.hasNext()) {
                GenericRecord record = ds.next();
                rowId = (int) record.get(0);
                List<Float> templateData = ((List<Float>) record.get(1));
                int countColValue = templateData.size();
                final float[] data = new float[countColValue];
                IntStream.range(0, countColValue - 1)
                        .boxed()
                        .parallel()
                        .forEach(index -> data[index] = templateData.get(index));
                ci.setData(rowId, data);
            }

            try {
                this.source = input = ci.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (this.hints == null) {
                this.hints = new Hints();
            }

            if (hints != null) {
                this.hints.add(hints);
            }

            if (this.hints.containsKey(Hints.GRID_COVERAGE_FACTORY)) {
                Object factory = this.hints.get(Hints.GRID_COVERAGE_FACTORY);
                if (factory != null && factory instanceof GridCoverageFactory) {
                    this.coverageFactory = (GridCoverageFactory) factory;
                }
            }

            if (this.coverageFactory == null) {
                this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);
            }

            this.coverageName = "image_coverage";

            try {
                boolean closeMe = true;
                String filename;
                if (input instanceof URL) {
                    URL sourceURL = (URL) input;
                    if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
                        filename = sourceURL.getAuthority();
                        String path = sourceURL.getPath();
                        if (filename != null && !filename.equals("")) {
                            path = "//" + filename + path;
                        }

                        this.source = input = new File(URLDecoder.decode(path, "UTF-8"));
                    } else if (sourceURL.getProtocol().equalsIgnoreCase("http")) {
                        this.wmsRequest = this.WMSRequest(input);
                    }
                }

                if (input instanceof File) {
                    File sourceFile = (File) input;
                    filename = sourceFile.getName();

                    int i = filename.lastIndexOf(46);
                    int length = filename.length();
                    if (i > 0 && i < length - 1) {
                        this.extension = filename.substring(i + 1).toLowerCase();
                    }

                    this.parentPath = sourceFile.getParent();
                    this.coverageName = filename;
                    int dotIndex = this.coverageName.lastIndexOf(".");
                    this.coverageName =
                            dotIndex == -1
                                    ? this.coverageName
                                    : this.coverageName.substring(0, dotIndex);
                } else if (input instanceof URL) {
                    input = ((URL) input).openStream();
                }

                if (input instanceof ImageInputStream) {
                    closeMe = false;
                } else {
                    this.inStreamSPI = ImageIOExt.getImageInputStreamSPI(this.source);
                    if (this.inStreamSPI == null) {
                        throw new DataSourceException("No input stream for the provided source");
                    }

                    this.inStream =
                            this.inStreamSPI.createInputStreamInstance(
                                    this.source,
                                    ImageIO.getUseCache(),
                                    ImageIO.getCacheDirectory());
                }

                if (this.inStream == null) {
                    throw new IllegalArgumentException("No input stream for the provided source");
                } else {
                    if (!this.wmsRequest) {
                        Object tempCRS = this.hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
                        if (tempCRS != null) {
                            this.crs = (CoordinateReferenceSystem) tempCRS;
                            this.LOGGER.log(
                                    Level.WARNING, "Using forced coordinate reference system ");
                        } else {
                            this.readCRS();
                        }
                    }

                    this.getHRInfo();
                    if (closeMe) {
                        this.inStream.close();
                    }
                }
            } catch (IOException var9) {
                this.LOGGER.log(Level.SEVERE, var9.getLocalizedMessage(), var9);
                throw new DataSourceException(var9);
            } catch (TransformException var10) {
                this.LOGGER.log(Level.SEVERE, var10.getLocalizedMessage(), var10);
                throw new DataSourceException(var10);
            }
        }
    }

    private void getHRInfo() throws IOException, TransformException {
        Iterator<ImageReader> it = ImageIO.getImageReaders(this.inStream);
        if (!it.hasNext()) {
            throw new DataSourceException("No reader avalaible for this source");
        } else {
            ImageReader reader = (ImageReader) it.next();
            this.readerSPI = reader.getOriginatingProvider();
            reader.setInput(this.inStream);
            this.setLayout(reader);
            this.numOverviews = this.wmsRequest ? 0 : reader.getNumImages(true) - 1;
            int hrWidth = reader.getWidth(0);
            int hrHeight = reader.getHeight(0);
            Rectangle actualDim = new Rectangle(0, 0, hrWidth, hrHeight);
            this.originalGridRange = new GridEnvelope2D(actualDim);
            if (this.source instanceof File) {
                this.prepareWorldImageGridToWorldTransform();
                if (!this.metaFile) {
                    AffineTransform tempTransform =
                            new AffineTransform((AffineTransform) this.raster2Model);
                    tempTransform.translate(-0.5D, -0.5D);
                    this.originalEnvelope =
                            CRS.transform(
                                    ProjectiveTransform.create(tempTransform),
                                    new GeneralEnvelope(actualDim));
                    this.originalEnvelope.setCoordinateReferenceSystem(this.crs);
                    this.highestRes = new double[2];
                    this.highestRes[0] = XAffineTransform.getScaleX0(tempTransform);
                    this.highestRes[1] = XAffineTransform.getScaleY0(tempTransform);
                } else {
                    this.highestRes = getResolution(this.originalEnvelope, actualDim, this.crs);
                    GridToEnvelopeMapper mapper =
                            new GridToEnvelopeMapper(this.originalGridRange, this.originalEnvelope);
                    mapper.setPixelAnchor(PixelInCell.CELL_CENTER);
                    this.raster2Model = mapper.createTransform();
                }
            }

            if (this.numOverviews >= 1) {
                this.overViewResolutions = new double[this.numOverviews][2];

                for (int i = 0; i < this.numOverviews; ++i) {
                    this.overViewResolutions[i][0] =
                            this.highestRes[0]
                                    * (double) this.originalGridRange.getSpan(0)
                                    / (double) reader.getWidth(i + 1);
                    this.overViewResolutions[i][1] =
                            this.highestRes[1]
                                    * (double) this.originalGridRange.getSpan(1)
                                    / (double) reader.getHeight(i + 1);
                }
            } else {
                this.overViewResolutions = (double[][]) null;
            }
        }
    }

    public Format getFormat() {
        return new ParquetImageFormat();
    }

    public GridCoverage2D read(GeneralParameterValue[] params)
            throws IllegalArgumentException, IOException {
        GeneralEnvelope requestedEnvelope = null;
        Rectangle dim = null;
        OverviewPolicy overviewPolicy = null;
        if (params != null) {
            for (int i = 0; i < params.length; ++i) {
                ParameterValue param = (ParameterValue) params[i];
                String name = param.getDescriptor().getName().getCode();
                if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = new GeneralEnvelope((Rectangle2D) gg.getEnvelope2D());
                    dim = gg.getGridRange2D().getBounds();
                } else if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName().toString())) {
                    overviewPolicy = (OverviewPolicy) param.getValue();
                }
            }
        }

        Integer imageChoice = 0;
        ImageReadParam readP = new ImageReadParam();
        if (!this.wmsRequest) {
            try {
                imageChoice = this.setReadParams(overviewPolicy, readP, requestedEnvelope, dim);
            } catch (TransformException var11) {
                throw new DataSourceException(var11);
            }
        }

        Hints newHints = this.hints.clone();
        ParameterBlock pbjRead = new ParameterBlock();
        pbjRead.add(
                this.inStreamSPI != null
                        ? this.inStreamSPI.createInputStreamInstance(
                                this.source, ImageIO.getUseCache(), ImageIO.getCacheDirectory())
                        : ImageIO.createImageInputStream(this.source));
        pbjRead.add(imageChoice);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add((Object) null);
        pbjRead.add((Object) null);
        pbjRead.add(readP);
        pbjRead.add(this.readerSPI.createReaderInstance());
        RenderedOp coverageRaster = JAI.create("ImageRead", pbjRead, newHints);
        AffineTransform rasterToModel = this.getRescaledRasterToModel(coverageRaster);
        return this.createImageCoverage(coverageRaster, ProjectiveTransform.create(rasterToModel));
    }

    private boolean WMSRequest(Object input) {
        if (input instanceof URL && ((URL) input).getProtocol().equalsIgnoreCase("http")) {
            try {
                String query = URLDecoder.decode(((URL) input).getQuery().intern(), "UTF-8");
                if (query.intern().indexOf("GetMap") == -1) {
                    return false;
                } else {
                    String[] pairs = query.split("&");
                    int numPairs = pairs.length;
                    String[] kvp = null;

                    for (int i = 0; i < numPairs; ++i) {
                        kvp = pairs[i].split("=");
                        if (kvp[0].equalsIgnoreCase("BBOX")) {
                            kvp = kvp[1].split(",");
                            this.originalEnvelope =
                                    new GeneralEnvelope(
                                            new double[] {
                                                Double.parseDouble(kvp[0]),
                                                Double.parseDouble(kvp[1])
                                            },
                                            new double[] {
                                                Double.parseDouble(kvp[2]),
                                                Double.parseDouble(kvp[3])
                                            });
                        }

                        if (kvp[0].equalsIgnoreCase("SRS")) {
                            this.crs = CRS.decode(kvp[1], true);
                        }

                        if (kvp[0].equalsIgnoreCase("layers")) {
                            this.coverageName = kvp[1].replaceAll(",", "_");
                        }
                    }

                    return true;
                }
            } catch (IOException var7) {
                return false;
            } catch (NoSuchAuthorityCodeException var8) {
                return false;
            } catch (MismatchedDimensionException var9) {
                return false;
            } catch (IndexOutOfBoundsException var10) {
                return false;
            } catch (FactoryException var11) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void readCRS() throws IOException {
        if (this.source instanceof File
                || this.source instanceof URL && ((URL) this.source).getProtocol() == "file") {
            String sourceAsString;
            if (this.source instanceof File) {
                sourceAsString = ((File) this.source).getAbsolutePath();
            } else {
                String auth = ((URL) this.source).getAuthority();
                String path = ((URL) this.source).getPath();
                if (auth != null && !auth.equals("")) {
                    sourceAsString = "//" + auth + path;
                } else {
                    sourceAsString = path;
                }
            }

            int index = sourceAsString.lastIndexOf(".");
            StringBuffer base =
                    (new StringBuffer(sourceAsString.substring(0, index))).append(".prj");
            File prjFile = new File(base.toString());
            if (prjFile.exists()) {
                PrjFileReader projReader = null;

                try {
                    FileChannel channel = (new FileInputStream(prjFile)).getChannel();
                    projReader = new PrjFileReader(channel);
                    this.crs = projReader.getCoordinateReferenceSystem();
                } catch (FileNotFoundException var19) {
                    this.LOGGER.log(Level.INFO, var19.getLocalizedMessage(), var19);
                } catch (IOException var20) {
                    this.LOGGER.log(Level.INFO, var20.getLocalizedMessage(), var20);
                } catch (FactoryException var21) {
                    this.LOGGER.log(Level.INFO, var21.getLocalizedMessage(), var21);
                } finally {
                    if (projReader != null) {
                        try {
                            projReader.close();
                        } catch (IOException var18) {
                            this.LOGGER.log(Level.SEVERE, var18.getLocalizedMessage(), var18);
                        }
                    }
                }
            }
        }

        if (this.crs == null) {
            this.crs = AbstractGridFormat.getDefaultCRS();
            this.LOGGER.fine("Unable to find crs, continuing with default CRS");
        }
    }

    private void prepareWorldImageGridToWorldTransform() throws IOException {
        String base =
                this.parentPath != null
                        ? this.parentPath + File.separator + this.coverageName
                        : this.coverageName;
        File file2Parse = new File(base + ".wld");
        if (file2Parse.exists()) {
            WorldFileReader reader = new WorldFileReader(file2Parse);
            this.raster2Model = reader.getTransform();
        } else {
            Set<String> ext = ParquetImageFormat.getWorldExtension(this.extension);
            Iterator<String> it = ext.iterator();
            if (!it.hasNext()) {
                throw new DataSourceException("Unable to parse extension " + this.extension);
            }

            do {
                file2Parse = new File(base + (String) it.next());
            } while (!file2Parse.exists() && it.hasNext());

            if (file2Parse.exists()) {
                WorldFileReader reader = new WorldFileReader(file2Parse);
                this.raster2Model = reader.getTransform();
                this.metaFile = false;
            } else {
                file2Parse = new File(base + ".meta");
                if (file2Parse.exists()) {
                    this.parseMetaFile(file2Parse);
                    this.metaFile = true;
                } else {
                    this.LOGGER.warning(
                            "Could not find a world transform file for "
                                    + this.coverageName
                                    + ", assuming the identity transform");
                    this.raster2Model = ProjectiveTransform.create(new AffineTransform());
                }
            }
        }
    }

    private void parseMetaFile(File file2Parse) throws NumberFormatException, IOException {
        double xMin = 0.0D;
        double yMax = 0.0D;
        double xMax = 0.0D;
        double yMin = 0.0D;
        BufferedReader in = new BufferedReader(new FileReader(file2Parse));
        String str = null;
        int index = 0;

        for (double value = 0.0D; (str = in.readLine()) != null; ++index) {
            switch (index) {
                case 1:
                    value =
                            Double.parseDouble(
                                    str.substring("Origin Longitude = ".intern().length()));
                    xMin = value;
                    break;
                case 2:
                    value =
                            Double.parseDouble(
                                    str.substring("Origin Latitude = ".intern().length()));
                    yMin = value;
                    break;
                case 3:
                    value =
                            Double.parseDouble(
                                    str.substring("Corner Longitude = ".intern().length()));
                    xMax = value;
                    break;
                case 4:
                    value =
                            Double.parseDouble(
                                    str.substring("Corner Latitude = ".intern().length()));
                    yMax = value;
            }
        }

        in.close();
        this.originalEnvelope =
                new GeneralEnvelope(new double[] {xMin, yMin}, new double[] {xMax, yMax});
        this.originalEnvelope.setCoordinateReferenceSystem(this.crs);
    }

    public int getGridCoverageCount() {
        return 1;
    }

    public String getExtension() {
        return this.extension;
    }

    protected java.util.List<FileGroupProvider.FileGroup> getFiles() {
        File file = this.getSourceAsFile();
        if (file == null) {
            return null;
        } else {
            java.util.List<File> files = new ArrayList();
            List<String> extensions = new ArrayList();
            extensions.add(".prj");
            Set<String> worldExtensions = ParquetImageFormat.getWorldExtension(this.getExtension());
            extensions.addAll(worldExtensions);
            String[] siblingExtensions =
                    (String[]) extensions.toArray(new String[extensions.size()]);
            this.addAllSiblings(file, files, siblingExtensions);
            return Collections.singletonList(
                    new FileGroupProvider.FileGroup(file, files, (Map) null));
        }
    }
}
