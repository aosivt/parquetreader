# parquetreader
this is plugin for geoserver wms service (read parquet file on local file system or hdfs).
template query
http://[HOST_GEOSERVER:[PORT]]/geoserver?[TYPE_FS]&[PATH_DIR_PARUQET]&[NAME_PARQUET_FILE_WITH_EXTENCION]&[OFFSET]

for LOCAL_FS_SYSTEM PATH_DIR_PARUQET = [PATH]
for HDFS PATH_DIR_PARUQET = [HOST_HDFS]:[PORT]/[PATH]
example:
for local
http://localhost:8080/geoserver?
file&
/home/alex/dev/parquetLE71310222013244EDC00&
704ec4c8-c509-44b2-a30d-112c1da987c2.parquet&
2
for hdfs
http://localhost:8080/geoserver?
hdfs&
localhost:8080/home/alex/dev/parquetLE71310222013244EDC00&
704ec4c8-c509-44b2-a30d-112c1da987c2.parquet&2
