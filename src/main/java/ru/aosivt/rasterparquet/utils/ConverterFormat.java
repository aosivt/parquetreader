package ru.aosivt.rasterparquet.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.geoserver.catalog.CatalogRepository;
import org.geotools.util.factory.Hints;
import org.kitesdk.data.*;
import org.kitesdk.data.spi.DefaultConfiguration;
import ru.aosivt.rasterparquet.CreateImage;
import ru.aosivt.rasterparquet.errors.CountParameterQuery;
import ru.aosivt.rasterparquet.errors.InitConrterFormat;

public class ConverterFormat {

    private static final Configuration conf = new Configuration(true);

    public static final String EXTENSION_CONVERT = "bil";

    public static final String LOCAL_TYPE_FS = "file";
    public static final String HDFS_TYPE_FS = "hdfs";

    private static final Integer INDEX_ROW_ID = 0;
    private static final Integer INDEX_COUNT_COL = 1;
    private static final Integer INDEX_COUNT_ROW = 2;
    private static final Integer INDEX_PROJECTION = 3;
    private static final Integer INDEX_GEOTRANSFORM = 4;
    private static final Integer INDEX_ROW_READING_VALUES = 5;

    public static final String NAME_SYSTEM_TEMP_DIR = System.getProperty("java.io.tmpdir");

    private static final String FORMAT_PATH_STRING = "%s/%s.%s";

    static {
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        DefaultConfiguration.set(conf);
    }

    private ConverterFormat() {
        throw new InitConrterFormat("this is not for implements");
    }

    public static void initConvert(String[] parameterQuery) {

        final String path = getPath(parameterQuery);
        final String nameImage = getNameFileImage(parameterQuery);
        final Integer offsetCol = getOffsetColl(parameterQuery);

        DatasetDescriptor descriptor =
                new DatasetDescriptor.Builder()
                        //                .schemaUri()
                        .compressionType(CompressionType.Uncompressed)
                        .property("parquet.block.size", "50MB")
                        .schema(Object.class)
                        //                .schemaUri(resourcePath)
                        .format(Formats.PARQUET)
                        .build();
        org.kitesdk.data.Dataset satelliteImageSet = null;
        try {
            satelliteImageSet = Datasets.load(String.format("dataset:%s", path));
        } catch (org.kitesdk.data.DatasetNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        if (Objects.isNull(satelliteImageSet)) {
            satelliteImageSet = Datasets.create(String.format("dataset:%s", path), descriptor);
        }

        DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();

        CreateImage ci = new CreateImage();
        int rowId = 0;
        int countColValue = 0;
        int countRowValue = 0;
        while (reader.hasNext()) {
            GenericRecord record = reader.next();
            rowId = (int) record.get(INDEX_ROW_ID);
            if (!ci.isInitDataset()) {
                countColValue = (int) record.get(INDEX_COUNT_COL);
                countRowValue = (int) record.get(INDEX_COUNT_ROW);
                ci.initDataset(getConvertedPathString(nameImage), countColValue, countRowValue);
                ci.addProjection((String) record.get(INDEX_PROJECTION));
                ci.addGeoTransform((List<Double>) record.get(INDEX_GEOTRANSFORM));
            }

            List<Float> templateData =
                    ((List<Float>) record.get(INDEX_ROW_READING_VALUES + offsetCol));
            final float[] data = new float[countColValue];
            IntStream.range(0, countColValue)
                    .boxed()
                    .forEach(index -> data[index] = templateData.get(index));
            ci.addRow(rowId, data);
        }
        ci.close();
        reader.close();
    }

    public static String getPath(final String[] parameterQuery) {
        return String.format(
                "%s://%s",
                FORMAT_QUERY.TYPE_FS.get(parameterQuery), FORMAT_QUERY.PATH.get(parameterQuery));
    }

    public static String[] getParameterQuery(Hints hints) {
        String[] pathParameter =
                ((CatalogRepository) hints.get(Hints.REPOSITORY))
                        .getCatalog()
                        .getCoverageStores()
                        .get(0)
                        .getURL()
                        .split("[?]")[1]
                        .split("&");
        if (pathParameter.length < 3) {
            throw new CountParameterQuery("count parameter don`t не достаточно)))");
        }
        return pathParameter;
    }

    public static String getNameFileImage(String[] parameterQuery) {
        return String.format(
                "%s-%s",
                FORMAT_QUERY.OFFSET_COL.get(parameterQuery),
                FORMAT_QUERY.NAME_FILE.get(parameterQuery));
    }

    public static Integer getOffsetColl(String[] parameterQuery) {
        return (Integer) FORMAT_QUERY.OFFSET_COL.get(parameterQuery);
    }

    public static String getConvertedPathString(String nameImage) {
        return String.format(
                FORMAT_PATH_STRING, NAME_SYSTEM_TEMP_DIR, nameImage, EXTENSION_CONVERT);
    }
}
