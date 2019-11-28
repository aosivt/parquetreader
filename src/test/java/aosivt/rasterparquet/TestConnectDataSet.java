package aosivt.rasterparquet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.stream.ImageInputStream;

import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.kitesdk.data.*;
import org.kitesdk.shaded.com.google.common.collect.Iterators;

public class TestConnectDataSet {
    private static final String PATH_PARQUET = "file:///home/oshchepkovay/dev/parquetLC08_L1TP_142020_20190907_20190907_01_RT";

    @Test
    public void assertDataSet() throws IOException, URISyntaxException {

        org.kitesdk.data.Dataset satelliteImageSet =
                Datasets.load(String.format("dataset:%s", PATH_PARQUET));

        DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();

        String nameImage = "/home/oshchepkovay/dev/parquet".concat(".bil");
        CreateImage ci = new CreateImage();
        int rowId = 0;
        int countColValue = 0;
        int countRowValue = 0;
        while (reader.hasNext()) {
            GenericRecord record = reader.next();
            rowId = (int) record.get(0);
            if (!ci.isInitDataset()){
                countColValue = (int) record.get(1);
                countRowValue = (int) record.get(2);

                ci.initDataset(nameImage, countColValue, countRowValue);
                ci.addProjection((String) record.get(3));
                ci.addGeoTransform((List<Double>) record.get(4));

            }

            List<Float> templateData = ((List<Float>) record.get(5));
            final float[] data = new float[countColValue];
            IntStream.rangeClosed(0, countColValue - 1)
                    .boxed()
                    .forEach(index -> data[index] = templateData.get(index));

            ci.addRow(rowId, data);
        }
        ci.close();
        reader.close();
    }

    @Test
    public void testImageIO() throws IOException {
        org.kitesdk.data.Dataset satelliteImageSet =
                Datasets.load(String.format("dataset:%s", PATH_PARQUET));

        DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();
        Iterator<GenericRecord> ds = reader.iterator();

        String nameImage = "/home/oshchepkovay/dev/parquet".concat(".tif");
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
        ImageInputStream is = ci.getInputStream();
        reader.close();
    }

    void deleteDirectoryStream(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
