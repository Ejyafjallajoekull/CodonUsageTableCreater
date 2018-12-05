package eyja.codon.usage.table.core;

/**
 * The CodonInfo class represents one specific codon and its usage information.
 * 
 * @author Planters
 *
 */
public class CodonInfo implements Comparable<CodonInfo> {

	private final String baseTriplet;
	private final String aminoAcid;
	private final String count;
	
	/**
	 * Create a new codon info from a given base triplet, an according amino acid and a count value.
	 * 
	 * @param baseTriplet - the base triplet defining the codon
	 * @param aminoAcid - the amino acid the base triplet is translated into
	 * @param count - the number of times a coden appeared in a specified set of codons
	 */
	public CodonInfo(String baseTriplet, String aminoAcid, String count) {
		this.baseTriplet = baseTriplet;
		this.aminoAcid = aminoAcid;
		this.count = count;
	}
	

	/**
	 * Get the base triplet defining this codon.
	 * 
	 * @return the base triplet
	 */
	public String getBaseTriplet() {
		return this.baseTriplet;
	}


	/**
	 * Get the amino acid the base triplet is translated to.
	 * 
	 * @return the amino acid
	 */
	public String getAminoAcid() {
		return this.aminoAcid;
	}


	/**
	 * Get the number of time the codon has been used in a predefined set of codons.
	 * 
	 * @return the codon count
	 */
	public String getCount() {
		return this.count;
	}
	
	/**
	 * Get the number of time the codon has been used in a predefined set of codons.
	 * 
	 * @return the codon count
	 */
	public float getCountAsFloat() {
		return Float.parseFloat(this.getCount());
	}


	/**
	 * Return an integer representation of a base triplet. This is used for 
	 * sorting codons.
	 * 
	 * @param base - the base to create the integer from
	 * @return the integer representation of the base
	 */
	private static int baseToInt(String base) {
		StringBuilder sb = new StringBuilder(base.length());
		for (int i = 0; i < base.length(); i++) {
			switch(base.charAt(i)) {
			
			case 'G':
				sb.append('4');
				break;
				
			case 'A':
				sb.append('3');
				break;
				
			case 'T':
				sb.append('2');
				break;
				
			case 'C':
				sb.append('1');
				break;
				
			default:
				sb.append('0');
				break;
			}
			
		}
		return Integer.parseInt(sb.toString());
	}
	
	@Override
	public int compareTo(CodonInfo comparison) {
		int firstComparison = this.getAminoAcid().compareTo(comparison.getAminoAcid());
		if (firstComparison == 0) {
			return CodonInfo.baseToInt(this.getAminoAcid()) - CodonInfo.baseToInt(comparison.getAminoAcid());
		} else {
			return firstComparison;
		}
	}
	
}
