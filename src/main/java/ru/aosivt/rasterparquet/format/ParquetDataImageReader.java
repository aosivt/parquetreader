package ru.aosivt.rasterparquet.format;

import it.geosolutions.imageio.gdalframework.GDALImageReader;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.avro.generic.GenericRecord;
import org.kitesdk.data.DatasetReader;
import org.kitesdk.data.Datasets;
import ru.aosivt.rasterparquet.CreateImage;

public class ParquetDataImageReader extends GDALImageReader {
    private static final Logger LOGGER = Logger.getLogger("ru.aosivt.rasterparquet.format");
    private static final String TYPE_FS = "file";
    private static final String EXTENSION_CONVERT = ".bil";

    //    private static final String NAME_FIELD_DATASET_MAP = "datasetsMap";
    //    private static final String NAME_FIELD_NSUBDARASETS = "nSubdatasets";
    //    private static final String NAME_FIELD_DATASET_NAMES = "datasetNames";
    //    private static final String NAME_FIELD_DATASET_METADATA_MAP = "datasetMetadataMap";

    public ParquetDataImageReader(ParquetImageReaderSpi originatingProvider) {
        super(originatingProvider, 0);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("ParquetDataImage Constructor");
        }
    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {

        super.setInput(getConverted(input), seekForwardOnly, ignoreMetadata);
        //        Dataset mainDataSet = null;
        //        String mainDatasetName = "";
        //        try {
        //            addDatasetMap(mainDatasetName, mainDataSet);
        //            ((ImageReader)this).setInput(this.imageInputStream, seekForwardOnly,
        // ignoreMetadata);
        //        } catch (IllegalAccessException e) {
        //            e.printStackTrace();
        //        }
        //        AppContext.getAppContext();
    }

    private File getConverted(Object input) {
        File convertedInput = null;
        if (input instanceof File) {
            convertedInput = getConvertedFile((File) input);

        } else if (input instanceof FileImageInputStreamExt) {
            convertedInput = getConvertedFile((((FileImageInputStreamExt) input).getFile()));
        }
        return convertedInput;
    }

    private File getConvertedFile(File input) {
        String nameImage = getConvertedNameFile(input);
        if (!Files.exists(Paths.get(nameImage))) {
            initConvert(input);
        }
        return new File(nameImage);
    }

    private String getConvertedNameFile(File input) {
        String nameParentPathParquet = input.getParentFile().getAbsolutePath();
        return nameParentPathParquet.concat(EXTENSION_CONVERT);
    }

    private void initConvert(final File input) {
        String nameParentPathParquet = input.getParentFile().getAbsolutePath();
        org.kitesdk.data.Dataset satelliteImageSet =
                Datasets.load(String.format("dataset:%s://%s", TYPE_FS, nameParentPathParquet));
        DatasetReader<GenericRecord> reader = satelliteImageSet.newReader();
        String nameImage = nameParentPathParquet.concat(EXTENSION_CONVERT);
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

    //    private synchronized void addDatasetMap(String mainDatasetName, Dataset mainDataSet)
    // throws IllegalAccessException {
    //        ((ConcurrentHashMap<String, Dataset>)
    // CommonUtils.extractField(this.getClass(),NAME_FIELD_DATASET_MAP)
    //                .get(this)).put(mainDatasetName,mainDataSet);
    //    }
    //    private synchronized void initSubDatasets(String mainDatasetName) throws
    // IllegalAccessException {
    //        String[] tempMainDatasetName = new String[1];
    //        tempMainDatasetName[0] = mainDatasetName;
    //        CommonUtils.extractField(this.getClass(),NAME_FIELD_NSUBDARASETS).set(this,1);
    //        CommonUtils.extractField(this.getClass(),NAME_FIELD_NSUBDARASETS).set(this,1);
    //
    // CommonUtils.extractField(this.getClass(),NAME_FIELD_DATASET_NAMES).set(this,tempMainDatasetName[0]);
    //        ((ConcurrentHashMap<String, GDALCommonIIOImageMetadata>)
    // CommonUtils.extractField(this.getClass(),NAME_FIELD_DATASET_METADATA_MAP).get(this)).put(tempMainDatasetName[0],this.createDatasetMetadata(mainDatasetName));
    //        this.datasetNames = new String[1];
    //        this.datasetNames[0] = mainDatasetName;
    //        this.datasetMetadataMap.put(this.datasetNames[0],
    // this.createDatasetMetadata(mainDatasetName));
    //
    //        ((ConcurrentHashMap<String, Dataset>)
    // CommonUtils.extractField(this.getClass(),NAME_DATASET_MAP)
    //                .get(this)).put(mainDatasetName,mainDataSet);
    //    }

}
