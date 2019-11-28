package aosivt.rasterparquet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.avro.Schema;

public class RedNirSatellite implements Serializable, org.apache.avro.generic.IndexedRecord {
    public static final Schema SCHEMA$ =
            new Schema.Parser()
                    .parse(
                            ""
                                    + "{\"type\":\"record\",\"name\":\"RedNirSatellite\",\"namespace\":\"aosivt.rasterparquet\",\""
                                    + "fields\":"
                                    + "[{\"name\":\"rowId\",\"type\":\"int\"}"
                                    + ",{\"name\":\"dataRed\",\"type\":{\"type\":\"array\",\"items\":\"float\"},\"default\":[0]}"
                                    + ",{\"name\":\"dataNIR\",\"type\":{\"type\":\"array\",\"items\":\"float\"},\"default\":[0]}]}");

    public static Schema getClassSchema() {
        return SCHEMA$;
    }

    public Integer rowId;
    public List<Float> dataRed;
    public List<Float> dataNIR;

    public RedNirSatellite() {}

    public RedNirSatellite(Integer rowId, List<Float> dataRed, List<Float> dataNIR) {
        this.rowId = rowId;
        this.dataRed = dataRed;
        this.dataNIR = dataNIR;
    }

    public RedNirSatellite(Integer rowId, Collection<Float> dataRed, Collection<Float> dataNIR) {
        this.rowId = rowId;
        this.dataRed = new ArrayList<>(dataRed);
        this.dataNIR = new ArrayList<>(dataNIR);
    }

    @Override
    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                rowId = (Integer) value$;
                break;
            case 1:
                dataRed = (List<Float>) value$;
                break;
            case 2:
                dataNIR = (List<Float>) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    @Override
    public Object get(int field$) {
        switch (field$) {
            case 0:
                return rowId;
            case 1:
                return dataRed;
            case 2:
                return dataNIR;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    @Override
    public Schema getSchema() {
        return SCHEMA$;
    }
}
