package eyja.codon.usage.table.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import central.logging.functionality.Logging;
import central.logging.functionality.LoggingFailureException;

/**
 * The CodonTableMain class retrieves codon usage table information for a specified organism and 
 * creates a CLC codon usage table from it.
 * 
 * @author Planters
 *
 */
public class CodonTableMain {
	
	/**
	 * The sign specifying command line arguments.
	 */
	private static final String CLA_ARGUMENT_IDENTIFIER = "-";
	/**
	 * The command line argument specifier for setting the genetic code ID.
	 */
	private static final String CLA_CODE = "gc";
	/**
	 * The command line argument specifier for enabling logging.
	 */
	private static final String CLA_LOGGING = "logging";
	/**
	 * The command line argument specifier for defining the output file.
	 */
	private static final String CLA_OUTPUT = "o";
	/**
	 * The central logger.
	 */
	private static final Logging LOG = new Logging(new File("."), "CondonUsageTable");
	/**
	 * The file extension of CLC codon usage tables.
	 */
	private static final String FILE_EXTENSION_CLC = ".cftbl";
	
	private static String geneticCode = "1";
	private static String species = null;
	private static File output = null;
	
	public static void main(String[] args) {
		CodonTableMain.configureLogger();
		CodonTableMain.parseCommandLineArguments(args);
		try { // create the URL to the table
			URL urlToTable = CodonTableMain.createCodonTableURL(CodonTableMain.species, CodonTableMain.geneticCode);
			CodonTableMain.getLog().fine(String.format("Created URL \"%s\" from species "
					+ "identifier \"%s\" and genetic code identifier \"%s\".", 
					urlToTable, CodonTableMain.species, CodonTableMain.geneticCode));
			try { // read the table
				CodonTableMain.LOG.logAndPrint(Level.INFO, String.format("Extracting the codon usage table "
						+ "from \"%s\"...", urlToTable));
				String usageTable = CodonTableMain.parseURL(urlToTable).createUsageTable();
				CodonTableMain.getLog().fine(String.format("Extracted following codon usage table:%n"
						+ " %s", usageTable));
				File writeLoc = CodonTableMain.output;
				if (writeLoc == null) {
					writeLoc = new File(String.format("%s_%s_%s%s", System.nanoTime(), 
							CodonTableMain.species, CodonTableMain.geneticCode, 
							CodonTableMain.FILE_EXTENSION_CLC));
				}
				CodonTableMain.LOG.logAndPrint(Level.INFO, String.format("Writing the codon usage table "
						+ "to \"%s\"...", writeLoc));
				// write the table to a file
				try (BufferedWriter br = new BufferedWriter(new FileWriter(writeLoc))) {
					br.write(usageTable);
					CodonTableMain.LOG.logAndPrint(Level.INFO, "Done.");
				} catch (IOException clcWriteException) {
					CodonTableMain.LOG.logAndPrint(Level.SEVERE, String.format("The codon usage table \n"
							+ "%s \ncould not be written to file \"%s\".", 
							usageTable, writeLoc), clcWriteException);
				}
			} catch (IOException e) {
				CodonTableMain.LOG.logAndPrint(Level.SEVERE, String.format("The URL \"%s\" could not be "
						+ "read from.", 
						urlToTable), e);
			}
		} catch (MalformedURLException e1) {
			CodonTableMain.LOG.logAndPrint(Level.SEVERE, String.format("The URL created from "
					+ "species identifier \"%s\" and genetic code identifier \"%s\" is invalid.", 
					CodonTableMain.species, CodonTableMain.geneticCode), e1);	
		}
		
		try {
			CodonTableMain.LOG.stopLogWriting();
		} catch (LoggingFailureException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse the command line arguments and adjust the specified settings.
	 * 
	 * @param arguments the command line arguments supplied
	 */
	private static void parseCommandLineArguments(String[] arguments) {
		if (arguments.length > 0) {
			for (int i = 0; i < arguments.length; i++) {
				if (arguments[i].startsWith(CodonTableMain.CLA_ARGUMENT_IDENTIFIER)) {
					String cla = arguments[i].replaceFirst("-", "");
					switch (cla) {
					
					case CodonTableMain.CLA_CODE:
						CodonTableMain.geneticCode = arguments[++i];
						break;
						
					case CodonTableMain.CLA_LOGGING:
						try {
							CodonTableMain.LOG.startLogWriting();
						} catch (LoggingFailureException e) {
							CodonTableMain.LOG.getLog().log(Level.SEVERE, "Logging could not "
									+ "be started.", e);
						}
						break;
					
					case CodonTableMain.CLA_OUTPUT:
						CodonTableMain.output = new File(arguments[++i]);
						break;
						
					default:
						throw new IllegalArgumentException(String.format("The command line "
								+ "argument \"%s\" is unknown.", arguments[i]));
					}
				} else {
					CodonTableMain.species = arguments[i];
				}
			}
		} else {
			throw new IllegalArgumentException("A species identifier is required for "
					+ "retrieval of codon usage tables.");
		}
	}
	
	/**
	 * Create the URL pointing to the codon usage table information based on the species identifier 
	 * and the genetic code identifier.
	 * 
	 * @param species - the species identifier
	 * @param geneticCode - the genetic code identifier
	 * @return the URL to the codon usage table information
	 * @throws MalformedURLException if the spiecies or genetic code identifiers are invalid in 
	 * a way they would violate the URL standard
	 */
	private static URL createCodonTableURL(String species, String geneticCode) throws MalformedURLException {
		return new URL(String.format("http://www.kazusa.or.jp/codon/cgi-bin/showcodon.cgi?species=%s&aa=%s&style=GCG", species, geneticCode));
	}
	
	/**
	 * Parse the specified URL and create a codon usage table from the information found there.
	 * 
	 * @param urlToCodonTable - the URL to the codon usage table
	 * @return the codon usage table built from the information retrieved from the URL
	 * @throws IOException if the website could not be parse
	 * @throws IllegalArgumentException if the URL has been parsed properly but no codon usage 
	 * table information could be determined
	 */
	private static CodonUsageTable parseURL(URL urlToCodonTable) throws IOException {
		/*
		 * This hacky implementation should be replaced by using the more standardised
		 * request via http://www.kazusa.or.jp/codon/current/species/"species ID".
		 */
		String name = null;
		ArrayList<CodonInfo> tableContent = new ArrayList<CodonInfo>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(urlToCodonTable.openStream()))) {
			String inputLine;
			boolean insideTable = false;
			while ((inputLine = br.readLine()) != null) {
				// search for organism name
				if (name == null && inputLine.toLowerCase().startsWith("<strong><i>")) {
					name = inputLine.substring(inputLine.indexOf("<i>") + "<i>".length(), inputLine.indexOf("</i>"));
					CodonTableMain.getLog().fine(String.format("Found species name \"%s\" for URL \"%s\".", 
							name, urlToCodonTable));
				}
				// search for codon usage table
				if (inputLine.startsWith("AmAcid")) {
					insideTable = true;
					CodonTableMain.getLog().fine(String.format("Found codon usage table for URL \"%s\".", 
							urlToCodonTable));
				} else if (insideTable && inputLine.startsWith("<")) {
					insideTable = false;
					CodonTableMain.getLog().fine(String.format("Found codon usage table end for URL \"%s\".", 
							urlToCodonTable));
				} else if (insideTable) {
					String[] columns = Arrays.stream(inputLine.split(" ")).map(String::trim).filter(s -> s.length() > 0)
					.toArray(String[]::new);
					if (columns.length >= 3) {
						tableContent.add(new CodonInfo(columns[1], columns[0], columns[2]));
						CodonTableMain.getLog().fine(String.format("Found valid codon usage table "
								+ "row: \"%s\"", inputLine));
						} else {
						CodonTableMain.getLog().fine(String.format("Invalid table line detected: "
								+ "\"%s\"", inputLine));
					}
				}
			}
		}
		if (tableContent.size() > 0) {
			return new CodonUsageTable(name, CodonTableMain.geneticCode, tableContent);
		} else {
			throw new IllegalArgumentException(String.format("No valid codon usage information could "
					+ "be retrieved from the URL \"%s\".", urlToCodonTable));
		}
	}
	
	/**
	 * Set up the logger.
	 */
	private static void configureLogger() {
		CodonTableMain.LOG.setNumberLogFiles(1);
		CodonTableMain.LOG.getLog().setLevel(Level.ALL);	
	}
	
	/**
	 * Get the central logger.
	 * 
	 * @return the central logger
	 */
	public static Logger getLog() {
		return CodonTableMain.LOG.getLog();
	}
	
}
