package osmgpxtool.mapmatching;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.LineString;

public class StreetSegment {
	private int id;
	private BigInteger osm_id;
	private Map<String, String> tags;
	private LineString geom;
	private double heading;
	private List<LineString> profiles;

	public StreetSegment(int id, BigInteger osm_id, Map<String, String> tags, LineString geom) {
		super();
		this.id = id;
		this.osm_id = osm_id;
		this.tags = tags;
		this.geom = geom;
	}

	public int getId() {
		return id;
	}

	public BigInteger getOsm_id() {
		return osm_id;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public LineString getGeom() {
		return geom;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public List<LineString> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<LineString> profiles) {
		this.profiles = profiles;
		
	}




}
