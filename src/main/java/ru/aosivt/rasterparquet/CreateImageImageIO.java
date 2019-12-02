package ru.aosivt.rasterparquet;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/** @deprecated not use for realisation */
public class CreateImageImageIO {

    final BufferedImage img;
    WritableRaster raster;
    SampleModel sm;
    DataBuffer db;
    int w = 0;
    int h = 0;

    public CreateImageImageIO(int w, int h) {
        //        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
        this.w = w;
        this.h = h;
        sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, w, h, 1, w, new int[] {0});
        db = new DataBufferFloat(w * h);
        raster = Raster.createWritableRaster(sm, db, null);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm =
                new ComponentColorModel(
                        cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        img = new BufferedImage(cm, raster, true, null);
    }

    public void setData(int rowId, float[] data) {
        raster.setPixels(0, rowId, w, 1, data);
    }

    public void save(String nameFile) throws IOException {

        ImageIO.write(img, "tiff", new File(nameFile));
    }

    public ImageInputStream getInputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "tiff", out);
        //        return ImageIO.createImageInputStream(out);
        //        return ImageIO.createImageInputStream(img.getData().getDataBuffer().);
        return ImageIO.createImageInputStream(new ByteArrayInputStream(out.toByteArray()));
    }
}
