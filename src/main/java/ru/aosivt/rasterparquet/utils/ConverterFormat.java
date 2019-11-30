package ru.aosivt.rasterparquet.utils;

import java.util.List;
import java.util.stream.IntStream;
import org.apache.avro.generic.GenericRecord;
import org.kitesdk.data.DatasetReader;
import org.kitesdk.data.Datasets;
import ru.aosivt.rasterparquet.CreateImage;
import ru.aosivt.rasterparquet.errors.InitConrterFormat;

public class ConverterFormat {

    public static final String EXTENSION_CONVERT = ".bil";

    public static final String NAME_SYSTEM_TEMP_DIR = System.getProperty("java.io.tmpdir");

    private static final String FORMAT_PATH_STRING = "%s/%s.%s";

    private ConverterFormat() {
        throw new InitConrterFormat("this is not for implements");
    }

    public static void initConvert(final String path, final String nameImage) {

        org.kitesdk.data.Dataset satelliteImageSet =
                Datasets.load(String.format("dataset:%s", path));

        DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();

        CreateImage ci = new CreateImage();
        int rowId = 0;
        int countColValue = 0;
        int countRowValue = 0;
        while (reader.hasNext()) {
            GenericRecord record = reader.next();
            rowId = (int) record.get(0);
            if (!ci.isInitDataset()) {
                countColValue = (int) record.get(1);
                countRowValue = (int) record.get(2);
                ci.initDataset(
                        String.format(
                                FORMAT_PATH_STRING,
                                NAME_SYSTEM_TEMP_DIR,
                                nameImage,
                                EXTENSION_CONVERT),
                        countColValue,
                        countRowValue);
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
}
