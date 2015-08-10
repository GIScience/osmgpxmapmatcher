package osmgpxtool.mapmatching.gps;

import com.vividsolutions.jts.geom.MultiLineString;

public class GpsTrace {
	private int id;
	private int trkId;
	private double heading;
	private MultiLineString geom;

	public int getId() {
		return id;
	}
	

	public double getHeading() {
		return heading;
	}

	public MultiLineString getGeom() {
		return geom;
	}

	public GpsTrace(int id, int trkId,  MultiLineString geom) {
		super();
		this.id = id;
		this.trkId = trkId;
		this.geom=geom;
	}


	public int getTrkId() {
		return trkId;
	}
	

}
