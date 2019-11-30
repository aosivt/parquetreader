package ru.aosivt.rasterparquet;

import java.awt.*;
import java.util.Objects;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

public class CreateImage {

    private static final String NAME_DRIVER = "ENVI";

    private String nameFile;

    private int width;

    private int height;

    private Dataset dataset = null;
    private Driver driver = null;
    private Band band = null;

    public CreateImage() {
        gdal.AllRegister();
        driver = gdal.GetDriverByName(NAME_DRIVER);
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
