package eyja.codon.usage.table.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The CodonUsageTable class represents a CLC codon usage table.
 * 
 * @author Planters
 *
 */
public class CodonUsageTable {

	private static final String NAME_IDENTIFIER = "Name: ";
	private static final String CODE_IDENTIFIER = "GeneticCode: ";
	
	private static final Comparator<CodonInfo> CODON_COMP = new Comparator<CodonInfo> () {

		@Override
		public int compare(CodonInfo o1, CodonInfo o2) {
			return o1.compareTo(o2);
		}
		
	};	
	
	private final String geneticCode;
	private final String name;
	private final ArrayList<CodonInfo> codons;
	
	/**
	 * Create a new codon usage table by supplying an organism name, a genetic code to use and a 
	 * list of codons.
	 * 
	 * @param organismName - the name of the organism
	 * @param geneticCode - the genetic code used for coding base triplets
	 * @param codons - the codon information
	 */
	public CodonUsageTable(String organismName, String geneticCode, List<CodonInfo> codons) {
		this.codons = new ArrayList<CodonInfo>(codons);
		this.name = organismName;
		this.geneticCode = geneticCode;
		this.codons.sort(CodonUsageTable.CODON_COMP);
	}
	
	/**
	 * Get the total frequency of this codon compared to all other codons used by this 
	 * organism.
	 * 
	 * @param codon - the codon to get the frequency for
	 * @return the total frequency of the codon
	 * 
	 * @throws NullPointerException if the supplied codon is null
	 */
	public float getCodonFrequency(CodonInfo codon) {
		if (codon != null) {
			float totalCount = 0.0f;
			for (CodonInfo ci : this.codons) {
				if (ci != null) {
					totalCount += ci.getCountAsFloat();
				}
			}
			CodonTableMain.getLog().finest(String.format("Found %s codons in total.", totalCount));
			return codon.getCountAsFloat() * 1000.0f / totalCount;
		} else {
			throw new NullPointerException("The frequency of null cannot be determined.");
		}
	}

	/**
	 * Get the frequency of usage for the specific base triplet in regard to other base triplets 
	 * coding for the same amino acid.
	 * 
	 * @param codon - the codon to get the frequency for
	 * @return the base triplet frequency
	 * 
	 * @throws NullPointerException if the supplied codon is null
	 */
	public float getBaseTripletFrequency(CodonInfo codon) {
		if (codon != null) {
			float totalCount = 0.0f;
			for (CodonInfo ci : this.codons) {
				if (ci != null && ci.getAminoAcid().equals(codon.getAminoAcid())) {
					totalCount += ci.getCountAsFloat();
				}
			}
			return codon.getCountAsFloat() / totalCount;
		} else {
			throw new NullPointerException("The usage frequency of null cannot be determined.");
		}
	}
	
	/**
	 * Create a codon usage table corresponding to CLC standards.
	 * 
	 * @return the CLC codon usage table
	 */
	public String createUsageTable() {
		final DecimalFormat floatFormatter = new DecimalFormat("0.00");
		floatFormatter.setRoundingMode(RoundingMode.HALF_UP);
		floatFormatter.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		StringBuilder sb = new StringBuilder();
		sb.append(CodonUsageTable.NAME_IDENTIFIER);
		sb.append(this.name);	
		sb.append("\r\n");
		sb.append(CodonUsageTable.CODE_IDENTIFIER);
		sb.append(this.geneticCode);
		sb.append("\r\n");
		for (CodonInfo ci : this.codons) {
			sb.append(String.format("%s%8s%15s%9s%7s\r\n", 
					ci.getAminoAcid(), ci.getBaseTriplet(), ci.getCount(), 
					floatFormatter.format(this.getCodonFrequency(ci)), 
					floatFormatter.format(this.getBaseTripletFrequency(ci))));
		}
		return sb.toString();
	}
	
}
