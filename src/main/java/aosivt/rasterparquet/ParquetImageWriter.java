package aosivt.rasterparquet;

import it.geosolutions.jaiext.range.NoDataContainer;
import java.awt.geom.AffineTransform;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.util.CoverageUtilities;
import org.geotools.data.DataSourceException;
import org.geotools.image.ImageWorker;
import org.geotools.image.io.ImageIOExt;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class ParquetImageWriter extends AbstractGridCoverageWriter implements GridCoverageWriter {
    private Format format;
    private String extension;

    public ParquetImageWriter(Object destination) {
        this(destination, (Hints) null);
    }

    public ParquetImageWriter(Object destination, Hints hints) {
        this.format = new ParquetImageFormat();
        this.extension = "png";
        this.destination = destination;
        if (this.destination instanceof String) {
            this.destination = new File((String) destination);
        } else if (this.destination instanceof URL) {
            URL url = (URL) destination;
            if (!url.getProtocol().equalsIgnoreCase("file")) {
                throw new RuntimeException(
                        "WorldImageWriter::write:It is not possible writing to an URL!");
            }

            String auth = url.getAuthority();
            String path = url.getPath();
            if (auth != null && !auth.equals("")) {
                path = "//" + auth + path;
            }

            this.destination = new File(path);
        }

        if (this.hints == null) {
            this.hints = new Hints();
        }

        if (hints != null) {
            this.hints.add(hints);
        }
    }

    public Format getFormat() {
        return this.format;
    }

    public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
            throws IllegalArgumentException, IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        if (parameters != null) {
            this.extension = ((Parameter) parameters[0]).stringValue();
        }

        if (this.destination instanceof File) {
            File imageFile = (File) this.destination;
            String path = imageFile.getAbsolutePath();
            int index = path.lastIndexOf(".");
            String baseFile = index >= 0 ? path.substring(0, index) : path;
            RenderedImage image = gc.getRenderedImage();

            try {
                createWorldFile(coverage, image, baseFile, this.extension);
            } catch (TransformException var11) {
                IOException ex = new IOException();
                ex.initCause(var11);
                throw ex;
            }

            createProjectionFile(baseFile, coverage.getCoordinateReferenceSystem());
        }

        this.outStream =
                ImageIOExt.createImageOutputStream(gc.getRenderedImage(), this.destination);
        if (this.outStream == null) {
            throw new IOException(
                    "WorldImageWriter::write:No image output stream avalaible for the provided destination");
        } else {
            encode(gc, this.outStream, this.extension);
        }
    }

    private static void createProjectionFile(
            String baseFile, CoordinateReferenceSystem coordinateReferenceSystem)
            throws IOException {
        File prjFile = new File(baseFile + ".prj");
        BufferedWriter out = new BufferedWriter(new FileWriter(prjFile));
        out.write(coordinateReferenceSystem.toWKT());
        out.close();
    }

    private static void createWorldFile(
            GridCoverage gc, RenderedImage image, String baseFile, String extension)
            throws IOException, TransformException {
        AffineTransform gridToWorld = (AffineTransform) gc.getGridGeometry().getGridToCRS();
        boolean lonFirst = XAffineTransform.getSwapXY(gridToWorld) != -1;
        double xPixelSize = lonFirst ? gridToWorld.getScaleX() : gridToWorld.getShearY();
        double rotation1 = lonFirst ? gridToWorld.getShearX() : gridToWorld.getScaleX();
        double rotation2 = lonFirst ? gridToWorld.getShearY() : gridToWorld.getScaleY();
        double yPixelSize = lonFirst ? gridToWorld.getScaleY() : gridToWorld.getShearX();
        double xLoc = gridToWorld.getTranslateX();
        double yLoc = gridToWorld.getTranslateY();
        StringBuffer buff = new StringBuffer(baseFile);
        Set ext = ParquetImageFormat.getWorldExtension(extension);
        Iterator it = ext.iterator();
        if (!it.hasNext()) {
            throw new DataSourceException("Unable to parse extension " + extension);
        } else {
            buff.append((String) it.next());
            File worldFile = new File(buff.toString());
            PrintWriter out = new PrintWriter(new FileOutputStream(worldFile));
            out.println(xPixelSize);
            out.println(rotation1);
            out.println(rotation2);
            out.println(yPixelSize);
            out.println(xLoc);
            out.println(yLoc);
            out.flush();
            out.close();
        }
    }

    private static void encode(
            GridCoverage2D sourceCoverage, ImageOutputStream outstream, String extension)
            throws IOException {
        if (sourceCoverage == null) {
            throw new IllegalArgumentException(
                    "A coverage must be provided in order for write to succeed!");
        } else {
            RenderedImage image = sourceCoverage.getRenderedImage();
            ImageWorker worker = new ImageWorker(image);
            worker.setROI(CoverageUtilities.getROIProperty(sourceCoverage));
            NoDataContainer noDataProperty = CoverageUtilities.getNoDataProperty(sourceCoverage);
            worker.setNoData(noDataProperty != null ? noDataProperty.getAsRange() : null);
            if (image.getColorModel() instanceof IndexColorModel
                    && image.getSampleModel().getNumBands() > 1) {
                worker.retainBands(1);
                image = worker.getRenderedImage();
            }

            if (image.getColorModel() instanceof DirectColorModel) {
                worker.forceComponentColorModel();
                image = worker.getRenderedImage();
            }

            if (extension.compareToIgnoreCase("gif") == 0) {
                if (image.getColorModel() instanceof IndexColorModel
                        && image.getSampleModel().getTransferType() != 0) {
                    worker.forceComponentColorModel();
                    image = worker.getRenderedImage();
                }

                if (image.getColorModel() instanceof ComponentColorModel) {
                    worker.forceIndexColorModelForGIF(true);
                    image = worker.getRenderedImage();
                } else if (image.getColorModel() instanceof IndexColorModel) {
                    worker.forceIndexColorModelForGIF(true);
                    image = worker.getRenderedImage();
                }
            }

            ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI("ImageWrite");
            pbjImageWrite.addSource(image);
            pbjImageWrite.setParameter("Output", outstream);
            pbjImageWrite.setParameter("VerifyOutput", Boolean.FALSE);
            pbjImageWrite.setParameter("Format", extension);
            JAI.create("ImageWrite", pbjImageWrite);
            outstream.flush();
            outstream.close();
        }
    }
}
