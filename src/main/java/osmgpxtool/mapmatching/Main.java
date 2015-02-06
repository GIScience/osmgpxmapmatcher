package osmgpxtool.mapmatching;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	/*
	 * INPUT: database, host, port, user, pw, streettable, street_id_column,
	 * street_linestring_column, gpxtable, gpx_id_column, gpx_point_col,
	 * outputtable_name, csv_output
	 */
	private static Options cmdOptions;
	private static CommandLine cmd = null;
	static Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private static String dbUser;
	private static String dbPassword;
	private static String dbName;
	private static String dbHost = "localhost";
	private static String dbPort = "5432";



	public static void main(String[] args) {
		Properties props = new Properties();
		try {
			props.load(Main.class.getResourceAsStream("/matching.properties"));
		} catch (IOException e) {
			LOGGER.error("could not read Properties file");
			System.exit(-1);
		}
		parseArguments(args, props);

		//init writer
		
		
		
		//init MapMatcher
		
		//MapMatcher.run
		
		
		
		
		LOGGER.info("done");
	}

	private static void parseArguments(String[] args, Properties props) {
		// read command line arguments
		HelpFormatter helpFormater = new HelpFormatter();
		helpFormater.setWidth(Integer.MAX_VALUE);
		CommandLineParser cmdParser = new BasicParser();
		cmdOptions = new Options();
		setupArgumentOptions();
		// parse arguments
		try {
			cmd = cmdParser.parse(cmdOptions, args);
			if (cmd.hasOption('h')) {
				helpFormater.printHelp("OSM GPX MAP MATCHER", cmdOptions, true);
				System.exit(0);
			}
			assignArguments(props);
		} catch (ParseException parseException) {
			LOGGER.info(parseException.getMessage());
			helpFormater.printHelp("OSM GPX MAP MATCHER", cmdOptions);
			System.exit(1);
		}
	}

	private static void assignArguments(Properties props) {
		// database properties
		props.put("dbUser", cmd.getOptionValue("U"));
		props.put("dbPassword", cmd.getOptionValue("PW"));
		props.put("dbName", cmd.getOptionValue("D"));
		if (cmd.getOptionValue("H") != null) {
			props.setProperty("dbHost", cmd.getOptionValue("H"));
		}
		if (cmd.getOptionValue("P") != null) {
			props.setProperty("dbPort", cmd.getOptionValue("P"));
		}
		if (cmd.getOptionValue("wpg") != null) {
			props.setProperty("dbOutputTable", cmd.getOptionValue("wpg"));
		}else if (cmd.getOptionValue("wcsv") != null) {
			props.put("dbOutputTable", cmd.getOptionValue("wcsv"));
		}else{
			LOGGER.info("No output parameter given. output table will be writen to database.");
		}
	}

	private static void setupArgumentOptions() {
		// parse command line arguments
		cmdOptions.addOption(new Option("h", "help", false, "displays help"));
		// database properties
		cmdOptions.addOption(OptionBuilder.isRequired().withLongOpt("database")
				.withDescription("Name of database").hasArg().create("D"));
		cmdOptions.addOption(OptionBuilder.isRequired().withLongOpt("user")
				.withDescription("Name of DB-Username").hasArg().create("U"));
		cmdOptions.addOption(OptionBuilder.isRequired().withLongOpt("password")
				.withDescription("Password of DB-User").hasArg().create("PW"));
		cmdOptions.addOption(OptionBuilder.withLongOpt("host")
				.withDescription("Database host <default:localhost>").hasArg()
				.create("H"));
		cmdOptions.addOption(OptionBuilder.withLongOpt("port")
				.withDescription("Database port <default:5432>").hasArg()
				.create("P"));
		// output
		cmdOptions
				.addOption(OptionBuilder
						.withDescription(
								"Name of output table in database. <default:street_gpx>")
						.hasArg().create("wpg"));
		cmdOptions
				.addOption(OptionBuilder
						.withDescription(
								"Write result to given csv file. provide path and filename")
						.hasArg().create("wcsv"));

	}
}
