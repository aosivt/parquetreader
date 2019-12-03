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

704ec4c8-c509-44b2-a30d-112c1da987c2.parquet

&2

#for rest api geoserver

#Create workspace

post query

http://localhost:8080/geoserver/rest/workspaces

body (application/json)

{
	"workspace":{
		"name":"wsparquet"
	}
}

#Create coveragestores

post query

http://localhost:8080/geoserver/rest/workspaces/wsparquet/coveragestores

body (application/json)

{
	"coverageStore":{
	"workspace":"wsparquet",
	"name": "parquet",
	"enabled": true,
	"type": "GGRP",
	"url":"http://localhost:8080/geoserver?hdfs&claster@localhost:8020/user/claster/temp/parquet/ndvi/1/LC08_L1TP_142020_20190907_20190907_01_RT&LC08_L1TP_142020_20190907_20190907_01_RT.parquet"	
}
}

#Create layer

post query

http://localhost:8080/geoserver/rest/workspaces/wsparquet/coveragestores/parquet/coverages

body (application/json)

{
	"coverage":{
	"name":"parquet",
	"title":"parquet",
	"srs":"EPSG:4326"
	}
}
	








