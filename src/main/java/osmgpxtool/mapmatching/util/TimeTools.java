package osmgpxtool.mapmatching.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Class for getting and parsing and converting different time related stuff.
 * 
 * @see System.getTimeInMillis()
 * @author hahmann
 * 
 */
public class TimeTools {

	/**
	 * Gets a Timestamp from String <code>s</code>.
	 * <p>
	 * 
	 * @param s
	 *            the String to be parsed as a Timestamp
	 * @return the parsed Timestamp object
	 */
	public static Timestamp getTimestamp(String s) {
		// results in "1970-01-01 01:00:00.0" in case of an exception
		return getTimestamp(s, new Timestamp(0));
	}

	/**
	 * Gets a Timestamp from String <code>s</code>.
	 * <p>
	 * In case of an Exception parsing <code>s</code> Timestamp <code>alt</code>
	 * will be returned.
	 * <p>
	 * 
	 * @param s
	 *            the String to be parsed as a Timestamp
	 * @param alt
	 *            the alternative Timestamp in case of an Exeception
	 * @return the parsed Timestamp object
	 */
	public static Timestamp getTimestamp(String s, Timestamp alt) {
		try {
			return (Timestamp.valueOf(s));
		} catch (Exception e) {
			return (alt);
		}
	}

	/**
	 * Return the log time string for the current time in the default time zone.
	 * 
	 * @return formatted string: "yyyymmddhhmmss"
	 */
	public static String getModifiedTimeString() { // 16 chars!
		return (getLogTime(Calendar.getInstance(), false));
	}

	/**
	 * Return the log time string for the current time in the default time zone.
	 * 
	 * @return formatted string: "yyyy-mm-dd hh:mm:ss"
	 */
	// public static String getLogTime() { // 19 chars!
	//	return (getLogTime(Calendar.getInstance(), true));
	//}

	/**
	 * Return the log time string for the current time in the default time zone.
	 * 
	 * @param the
	 *            UTC milliseconds value
	 * @return formatted string: "yyyy-mm-dd hh:mm:ss", or "0000-00-00 00:00:00"
	 *         if time is 0
	 */
	public static String getLogTime(long time) { // 19 chars!

		if (time == 0)
			return ("0000-00-00 00:00:00");
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(time)); // avoid setTimeInMillis(), not 1.1-comp.
		return (getLogTime(gc, true));
	}
	
	/**
	 * Return the log time string for the current time in the default time zone.
	 * 
	 * @return formatted string: "dd.MM.yyyy-HH:mm:ss.SSS" (e.g. 30.09.2011-10:23:24.073)
	 */
	public static String getLogTime() {
		return new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss.SSS").format(System.currentTimeMillis());
	}

	/**
	 * Return a internal data representation of an GregorianCalendar having a
	 * special time zone.
	 * 
	 * @param gc
	 *            the gregorian calendar
	 * @return formatted string: "yyyy-mm-dd hh:mm:ss"
	 */
	public static String getLogTime(Calendar gc, boolean useSeparators) {
		// 19 chars!
		int day = gc.get(Calendar.DAY_OF_MONTH);
		int month = gc.get(Calendar.MONTH) + 1; // 0-based!
		int year = gc.get(Calendar.YEAR);
		int hour = gc.get(Calendar.HOUR_OF_DAY);
		int minute = gc.get(Calendar.MINUTE);
		int second = gc.get(Calendar.SECOND);

		StringBuffer sb = new StringBuffer(512);
		sb.append(year);
		sb.append(useSeparators ? "-" : "");
		sb.append((month < 10 ? "0" : "") + month);
		sb.append(useSeparators ? "-" : "");
		sb.append((day < 10 ? "0" : "") + day);
		sb.append(useSeparators ? "__" : "");
		sb.append((hour < 10 ? "0" : "") + hour);
		sb.append(useSeparators ? ":" : "");
		sb.append((minute < 10 ? "0" : "") + minute);
		sb.append(useSeparators ? ":" : "");
		sb.append((second < 10 ? "0" : "") + second);

		return sb.toString();
	}

	/**
	 * Converts an arbitrary time span (measured in milliseconds) to a 00:00:00
	 * like representation;
	 * 
	 * e.g.: 233683 ms are converted to "00:05:53.683";
	 * 
	 * 
	 * @param millis
	 *            time span in milliseconds
	 * @return time span in "hh:mm:ss" representation
	 */
	public static String convertMillisToHourMinuteSecond(long millis) {
				
		SimpleDateFormat sdf = new SimpleDateFormat("D HH:mm:ss.SSS");
		sdf.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
		return sdf.format(millis);
	}
}
