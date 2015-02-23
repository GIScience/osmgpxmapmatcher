# osmgpxmapmatcher
osmgpxmapmatcher is a tool to find corresponding GPS traces of street segments. The streets and GPS traces to be matched, need to be stored in a PostgreSQL/PostGIS database. [osmgpxfilter](https://github.com/GIScience/osmgpxfilter) can be used to import the GPS-traces into the database.
The result will be written in a new table, referencing the streets and GPS traces. 
To adjust the standard schema of the input tables, the properties file matching.properties needs to be changed. 

 Until now it uses following simple matching algorithm:

- look for candidate traces intersecting a buffer of a street segment
- create profile lines (perpendicular line) with a given length add each node of a street 
- condition: if 70% of the profile lines are intersected by the GPS Trace, the street and GPS trace are considered as matched:

### Requirements
- The input data must be in CRS WGS84 (EPSG:4326)
- Geometry type of GPS-traces: MultiLineString
- Geometry type of streets: LineString


### Getting started

1. install maven
2. install git
3. clone project `$ git clone https://github.com/GIScience/osmgpxfilter`
4. go into project directory `$ cd osmgpxfilter/h
5. run maven `$ mvn clean package`
6. start application `java -jar target/osmgpxfilter-0.1.jar <args>`

### Usage
```
Help:
 -h,--help              displays help
 
Required Arguments:
 -D,--database          Name of database
 -PW,--password <arg>   Password of DB-User
 -U,--user <arg>        Name of DB-Username
 
 Optional Arguments:
-H,--host <arg>        Database host <default:localhost>
 -o <arg>               Name of output table in database. <default:streets_gpx>
 -P,--port <arg>        Database port <default:5432>


Example:


 ```
 
 ```
 /*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
 ```
 