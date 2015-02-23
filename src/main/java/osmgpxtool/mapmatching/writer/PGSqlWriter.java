/**
 * 
 */
package osmgpxtool.mapmatching.writer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import osmgpxtool.mapmatching.StreetSegment;
import osmgpxtool.mapmatching.gps.GpsTrace;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Database writer for MapMatching results.
 *
 */
public class PGSqlWriter {

	private Connection con;
	private Properties p;
	private PreparedStatement insert;
	private PreparedStatement insert_profiles;
	private Statement inserts;
	private WKBWriter wkbWriter;
	int batchSize = 0;
	private int lastStreetWritten = 0;

	public PGSqlWriter(Connection con, Properties props) {
		this.con = con;
		this.p = props;
	}

	public void init() {
		inserts = null;

		// drop table if exists and create new one

		Statement create = null;

		try {
			// for insert batch
			inserts = con.createStatement();
			// for creation of databases
			create = con.createStatement();
			create.addBatch("DROP TABLE IF EXISTS " + p.getProperty("dbOutputTable") + ";");
			create.addBatch("CREATE TABLE " + p.getProperty("dbOutputTable")
					+ " (\"street_id\" integer NOT NULL, \"gpx_id\" integer NOT NULL, CONSTRAINT \""
					+ p.getProperty("dbOutputTable") + "_PK\" PRIMARY KEY (street_id, gpx_id));");

			// TODO: uncomment if you want to write the profile line to database
			/*
			 * create.addBatch("DROP TABLE IF EXISTS " +
			 * p.getProperty("dbOutputTable") + "_profiles;");
			 * create.addBatch("CREATE TABLE " + p.getProperty("dbOutputTable")
			 * +
			 * "_profiles (\"street_id\" integer NOT NULL, \"street_profiles\" geometry, CONSTRAINT \""
			 * + p.getProperty("dbOutputTable") +
			 * "_profiles_PK\" PRIMARY KEY (street_id));");
			 */
			create.executeBatch();

			insert = con.prepareStatement("INSERT INTO " + p.getProperty("dbOutputTable")
					+ " (\"street_id\",\"gpx_id\") VALUES(?,?);");

			// TODO: uncomment if you want to write the profile line to database
			/*
			 * insert_profiles = con.prepareStatement("INSERT INTO " +
			 * p.getProperty("dbOutputTable") +
			 * "_profiles (\"street_id\",\"street_profiles\") VALUES(?, ST_GeomFromEWKB(?));"
			 * );
			 */
		} catch (SQLException e) {
			e.printStackTrace();
			SQLException e2 = e.getNextException();
			e2.printStackTrace();
			System.exit(1);
		}

		wkbWriter = new WKBWriter(3, true);
	}

	public void write(StreetSegment street, GpsTrace trace) {

		try {
			insert.setInt(1, street.getId());
			insert.setInt(2, trace.getId());
			insert.addBatch();
			batchSize++;
			// TODO: uncomment if you want to write the profile line to database
			/*
			 * if (lastStreetWritten != street.getId()) { GeometryFactory geomF
			 * = new GeometryFactory(); MultiLineString profiles =
			 * geomF.createMultiLineString(street.getProfiles().toArray(new
			 * LineString[0])); insert_profiles.setInt(1, street.getId());
			 * insert_profiles.setBytes(2, wkbWriter.write(profiles));
			 * insert_profiles.addBatch(); }
			 * 
			 * lastStreetWritten = street.getId();
			 */
			if (batchSize == 10000) {
				batchSize = 0;
				insert.executeBatch();
				insert.clearBatch();
				// TODO: uncomment if you want to write the profile line to
				// database
				/*
				 * insert_profiles.executeBatch(); insert_profiles.clearBatch();
				 */
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void close() {

		// close insert statement
		try {
			if (insert != null) {
				insert.executeBatch();
				insert.close();
			}
			// TODO: uncomment if you want to write the profile line to
			// database
			/*
			 * if (insert_profiles != null) { insert_profiles.executeBatch();
			 * insert_profiles.close(); }
			 */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// don't close connection, this is done, where is was established
	}

}
