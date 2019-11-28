package aosivt.rasterparquet;

import java.awt.*;
import java.util.Objects;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

public class CreateImage {

    static {
        try {
            System.load("/usr/local/lib/gdal/libgdalconstjni.so");
            System.load("/usr/local/lib/gdal/libgdaljni.so");
            System.load("/usr/local/lib/gdal/libgnmjni.so");
            System.load("/usr/local/lib/gdal/libogrjni.so");
            System.load("/usr/local/lib/gdal/libosrjni.so");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    private static int scaleImage = 10;
    private String nameFile;
    private int width;
    private int iteratorArray;
    private int height;
    private int maxCountArray;
    private float[] dataImage;
    private Dataset dataset = null;
    private Driver driver = null;
    private Band band = null;
    private int offset = 0;

    public CreateImage() {
        gdal.AllRegister();
        driver = gdal.GetDriverByName("ENVI");
    }

    public void initDataset(String nameFile, int xsize, int ysize) {
        dataset = driver.Create(nameFile, xsize, ysize, 1, gdalconst.GDT_Float32);

        band = dataset.GetRasterBand(1);

        this.nameFile = nameFile;
        width = xsize;
        height = ysize;
    }

    public void addProjection(String projection) {
        dataset.SetProjection(projection);
    }

    public void addGeoTransform(java.util.List<java.lang.Double> geoTransform) {
        dataset.SetGeoTransform(geoTransform.stream().mapToDouble(Double::doubleValue).toArray());
    }

    public void addRow(int rowId, float[] data) {

        band.WriteRaster(0, rowId, width, 1, data);
    }

    public void close() {
        dataset.delete();
    }

    public boolean isInitDataset() {
        return Objects.nonNull(nameFile) && width != 0 && height != 0;
    }
}
