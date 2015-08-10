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

/**
 * Database writer for MapMatching results.
 *
 */
public class PGSqlWriter {

	private Connection con;
	private Properties p;
	private PreparedStatement insert;

	int batchSize = 0;

	public PGSqlWriter(Connection con, Properties props) {
		this.con = con;
		this.p = props;
	}

	public void init() {

		// drop table if exists and create new one

		Statement create = null;

		try {

			// for creation of databases
			create = con.createStatement();
			create.addBatch("DROP TABLE IF EXISTS " + p.getProperty("dbMatchingOutputTable") + ";");
			create.addBatch("CREATE TABLE "
					+ p.getProperty("dbMatchingOutputTable")
					+ " (\"street_id\" integer NOT NULL, \"gpx_id\" integer NOT NULL, \"trk_id\" integer NOT NULL, CONSTRAINT \""
					+ p.getProperty("dbMatchingOutputTable") + "_PK\" PRIMARY KEY (street_id, gpx_id, trk_id));");


			create.executeBatch();

			insert = con.prepareStatement("INSERT INTO " + p.getProperty("dbMatchingOutputTable")
					+ " (\"street_id\",\"gpx_id\", \"trk_id\") VALUES(?,?,?);");


		} catch (SQLException e) {
			e.printStackTrace();
			SQLException e2 = e.getNextException();
			e2.printStackTrace();
			System.exit(1);
		}

	}

	public void write(StreetSegment street, GpsTrace trace) {

		try {
			insert.setInt(1, street.getId());
			insert.setInt(2, trace.getId());
			insert.setInt(3, trace.getTrkId());
			insert.addBatch();
			batchSize++;



			if (batchSize == 10000) {
				batchSize = 0;
				insert.executeBatch();
				insert.clearBatch();

				 
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
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// don't close connection, this is done, where is was established
	}

}
