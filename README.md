# parquetreader

this is plugin for geoserver wms service (read parquet file on local file system or hdfs).

template query

http://[HOST_GEOSERVER:[PORT]]/geoserver?[TYPE_FS]&[PATH_DIR_PARUQET]&[NAME_PARQUET_FILE_WITH_EXTENCION]&[OFFSET]

for LOCAL_FS_SYSTEM PATH_DIR_PARUQET = [PATH]


for HDFS PATH_DIR_PARUQET = [HOST_HDFS]:[PORT]/[PATH]


example:

# for local

```http
http://localhost:8080/geoserver?file&/home/alex/dev/parquetLE71310222013244EDC00&704ec4c8-c509-44b2-a30d-112c1da987c2.parquet&2
```

# for hdfs

```http
http://localhost:8080/geoserver?hdfs&localhost:8080/home/alex/dev/parquetLE71310222013244EDC00&704ec4c8-c509-44b2-a30d-112c1da987c2.parquet&2
```

# for rest api geoserver

# Create workspace

post query

```http
http://localhost:8080/geoserver/rest/workspaces
```


body (application/json)
```json

{
	"workspace":{
		"name":"wsparquet"
	}
}


```

# Create coveragestores

post query

```http
    http://localhost:8080/geoserver/rest/workspaces/wsparquet/coveragestores
```


body (application/json)
```json
{
	"coverageStore":{
	"workspace":"wsparquet",
	"name": "parquet",
	"enabled": true,
	"type": "GGRP",
	"url":"http://localhost:8080/geoserver?hdfs&claster@localhost:8020/user/claster/temp/parquet/ndvi/1/LC08_L1TP_142020_20190907_20190907_01_RT&LC08_L1TP_142020_20190907_20190907_01_RT.parquet"	
}
}
```


# Create layer

post query

```http

http://localhost:8080/geoserver/rest/workspaces/wsparquet/coveragestores/parquet/coverages

```

body (application/json)
```json 
{
	"coverage":{
	"name":"parquet",
	"title":"parquet",
	"srs":"EPSG:4326"
	}
}
```

	
# Set exist layer (example NDVI)	
 PUT query 
 
 http://localhost:8080/geoserver/rest/layers/wsparquet:parquet
 
 body
 
 {
	"layer":{
		"defaultStyle":{
			"name":"NDVI"
		}
	}
}

# Example HTML page for use
```html
<!doctype html>
<html lang="en">
  <head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/css/ol.css" type="text/css">
    <style>
      .map {
        height: 400px;
        width: 100%;
      }
    </style>
    <script src="https://cdn.jsdelivr.net/gh/openlayers/openlayers.github.io@master/en/v6.1.1/build/ol.js"></script>
    <title>OpenLayers example</title>
  </head>
  <body>
    <h2>My Map</h2>
    <div id="map" class="map"></div>
    <script type="text/javascript">
      
		    var layers = [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        }),
        new ol.layer.Tile({
          // extent: [-13884991, 2870341, -7455066, 6338219],
          source: new ol.source.TileWMS({
            url: 'http://localhost:8080/geoserver/wsparquet/wms',
            params: {'LAYERS':'wsparquet:parquet'},
            serverType:'geoserver',
            // crossOrigin: 'anonymous'
          })
        })
      ];

      var map = new ol.Map({
        target: 'map',
        layers: layers,
        view: new ol.View({
          center: [-8208266.719431938, 4979872.392779233],
          zoom: 1
        })
      });
    </script>
  </body>
</html>
```







