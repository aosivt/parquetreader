package ru.aosivt.rasterparquet.format;

import it.geosolutions.imageio.gdalframework.GDALImageReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        super.setInput(input, seekForwardOnly, ignoreMetadata);
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
