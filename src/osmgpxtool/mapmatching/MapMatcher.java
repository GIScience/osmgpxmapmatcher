package osmgpxtool.mapmatching;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.geotools.geometry.jts.WKTReader2;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osmgpxtool.mapmatching.gps.GpsTrace;
import osmgpxtool.mapmatching.util.Util;
import osmgpxtool.mapmatching.writer.PGSqlWriter;
import osmgpxtool.mapmatching.util.Progress;

import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

public class MapMatcher {
	static Logger LOGGER = LoggerFactory.getLogger(MapMatcher.class);
	private Properties p;
	private Connection con;
	private PGSqlWriter writer;
	private GeometryFactory geomF;
	private WKBReader wkbReader;
	private WKBWriter wkbWriter;

	public MapMatcher(Connection con, Properties p, PGSqlWriter writer) {
		super();
		this.p = p;
		this.con = con;
		this.writer = writer;
	}

	/**
	 * This method initializes the MapMatcher. It checks whether all given
	 * database tables and their columns exist,
	 */
	public void init() {
		// 1. check if table, rows and so on of input data exists.
		checkInputdata();

		// 2. init geometryfactory
		geomF = new GeometryFactory(new PrecisionModel(), 4326);
		// 3. init WKBReader and WKBWriter
		wkbReader = new WKBReader();
		wkbWriter = new WKBWriter(2, true);

	}

	public void run() {

		List<StreetSegment> streets = queryStreets();
		// Loop through streets

		Progress pr = new Progress();
		pr.start(streets.size());
		int progressPercentPrinted = -1;

		for (StreetSegment street : streets) {
			// progress
			pr.increment();
			int currentProgressPercent = (int) (Math.round(pr.getProgressPercent()));
			if (currentProgressPercent % 5 == 0 && currentProgressPercent != progressPercentPrinted) {
				LOGGER.info(pr.getProgressMessage());
				progressPercentPrinted = currentProgressPercent;
			}

			// buffer street segment
			Polygon buffer = Util.bufferWGS84WithMeters(street.getGeom(),
					Integer.valueOf(p.getProperty("streetBuffer")));
			// retrieve all gpx points intersection with buffer
			List<GpsTrace> candidateTraces = getCandidateTraces(buffer);
			// no candidate traces are found:
			if (candidateTraces == null) {
				continue;
			}

			// get profile line from street
			street.setProfiles(Util.computesProfileLines(Double.valueOf(p.getProperty("streetProfileLength")),
					street.getGeom()));
			// loop through candidate traces
			for (GpsTrace trace : candidateTraces) {

				// check if it intersects with each profile line.
				double numberProfiles = Double.valueOf(street.getProfiles().size());
				int numberIntersects = 0;
				for (LineString profile : street.getProfiles()) {
					if (profile.intersects(trace.getGeom())) {
						numberIntersects++;
						if (numberIntersects / numberProfiles >= Double.valueOf(p
								.getProperty("streetProfileIntersectionRatio"))) {
							// more or equal than 70 % of profile line are
							// intersected, consider GPS as matched
							break;
						}
					}
				}

				if (numberIntersects / numberProfiles >= Double.valueOf(p
						.getProperty("streetProfileIntersectionRatio"))) {
					writer.write(street, trace);
				}

			}

		}

	}

	/**
	 * This method queries all streets segments from the given street table and
	 * stores it in an ArrayList as {@link StreetSegment} objects.
	 * 
	 * @return
	 */
	private List<StreetSegment> queryStreets() {
		Statement s;
		ResultSet rs;
		List<StreetSegment> streets = new ArrayList<StreetSegment>();

		try {
			s = con.createStatement();

			rs = s.executeQuery("SELECT " + p.getProperty("t_streetIdCol") + "," + p.getProperty("t_streetIdCol") + ","
					+ p.getProperty("t_streetTags") + ",ST_ASBINARY(" + p.getProperty("t_streetGeomCol") + ") as "
					+ p.getProperty("t_streetGeomCol") + " FROM " + p.getProperty("t_streetName") + ";");

			while (rs.next()) {

				int id = rs.getInt(p.getProperty("t_streetIdCol"));
				BigInteger osm_id = new BigInteger(rs.getBigDecimal(p.getProperty("t_streetIdCol")).toString());
				Map<String, String> tags = Util.hstoreToMap(rs.getObject(p.getProperty("t_streetTags")));
				LineString line = null;
				try {
					line = (LineString) wkbReader.read(rs.getBytes(p.getProperty("t_streetGeomCol")));
					line.setSRID(4326);
				} catch (ParseException e) {
					LOGGER.error("Could not parse LineString");
					e.printStackTrace();
				}
				if (line != null) {
					StreetSegment street = new StreetSegment(id, osm_id, tags, line);
					streets.add(street);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create Statement");
		}
		return streets;

	}

	/**
	 * This method gets all GPS traces which intersect the buffer with the given
	 * bufferDistance (in properties file) of the street segment. It return an
	 * ArrayList, containing {@link GpsTrace} objects
	 * 
	 * @param buffer
	 * @return
	 */
	private List<GpsTrace> getCandidateTraces(Polygon buffer) {
		PreparedStatement s;
		ResultSet rs;
		List<GpsTrace> traces = new ArrayList<GpsTrace>();

		try {
			/*
			 * ST_FORCE2D added, since wkbReader does not support parsing of
			 * MultiLineString Z geometries. Work around could be to fetch first
			 * all gpx_id. Then loop through gpx_id and fetch the single points
			 * per trace using ST_DUMPOINTS (as well as ST_X, ST_Y, ST_Z) and
			 * construct a new Multilinestrin out of it. EXAMPLE: SELECT gpx_id,
			 * ST_ASTEXT((st_dumppoints(geom)).geom),(st_dumppoints(geom)).path
			 * as points from gpx_data_line WHERE gpx_id=xxx ORDER BY
			 * (st_dumppoints(geom)).path[1], (st_dumppoints(geom)).path[2];
			 */

			s = con.prepareStatement("SELECT " + p.getProperty("t_gpxIdCol") + ", ST_ASBINARY(ST_Force2D("
					+ p.getProperty("t_gpxGeomCol") + ")) AS " + p.getProperty("t_gpxGeomCol") + " FROM "
					+ p.getProperty("t_gpxName") + " WHERE ST_INTERSECTS(ST_GeomFromWKB(?,4326),"
					+ p.getProperty("t_gpxGeomCol") + ");");

			s.setObject(1, wkbWriter.write(buffer), java.sql.Types.BINARY);

			rs = s.executeQuery();

			int gpxID;

			GpsTrace trace = null;

			while (rs.next()) {
				// get information from result set
				gpxID = rs.getInt(p.getProperty("t_gpxIdCol"));
				MultiLineString line = (MultiLineString) wkbReader.read(rs.getBytes(p.getProperty("t_gpxGeomCol")));
				trace = new GpsTrace(gpxID, line);
				traces.add(trace);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not create Statement");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (traces.isEmpty()) {
			return null;
		} else {
			return traces;

		}
	}

	/**
	 * This method checks, if all table and columns name, given in
	 * matching.properties exist in the database. If not all given names are
	 * found, the program will exit.
	 */
	private void checkInputdata() {

		try {
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT  * FROM " + p.getProperty("t_streetName") + " WHERE false;");
			try {
				rs.findColumn(p.getProperty("t_streetIdCol"));
				rs.findColumn(p.getProperty("t_streetOsmIdCol"));
				rs.findColumn(p.getProperty("t_streetTags"));
				rs.findColumn(p.getProperty("t_streetGeomCol"));
			} catch (SQLException e) {
				LOGGER.error("Coloumn is missing in street network table.");
				e.printStackTrace();
				System.exit(1);
			}

			rs = s.executeQuery("SELECT * FROM " + p.getProperty("t_gpxName") + " WHERE false");
			try {
				rs.findColumn(p.getProperty("t_gpxIdCol"));
				rs.findColumn(p.getProperty("t_gpxGeomCol"));
			} catch (SQLException e) {
				LOGGER.error("Coloumn is missing in gpx table.");
				e.printStackTrace();
				System.exit(1);
			}

			s.close();
		} catch (SQLException e) {
			LOGGER.error("Could not find table.");
			e.printStackTrace();
			System.exit(1);
		}

	}

}
