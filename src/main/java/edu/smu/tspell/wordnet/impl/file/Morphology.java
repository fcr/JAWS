/*

  Java API for WordNet Searching 1.0
  Copyright (c) 2007 by Brett Spell.

  This software is being provided to you, the LICENSEE, by under the following
  license.  By obtaining, using and/or copying this software, you agree that
  you have read, understood, and will comply with these terms and conditions:
   
  Permission to use, copy, modify and distribute this software and its
  documentation for any purpose and without fee or royalty is hereby granted,
  provided that you agree to comply with the following copyright notice and
  statements, including the disclaimer, and that the same appear on ALL copies
  of the software, database and documentation, including modifications that you
  make for internal use or for distribution.

  THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" WITHOUT REPRESENTATIONS OR
  WARRANTIES, EXPRESS OR IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION,  
  LICENSOR MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANTABILITY OR FITNESS
  FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE LICENSED SOFTWARE OR
  DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS,
  TRADEMARKS OR OTHER RIGHTS.

 */
package edu.smu.tspell.wordnet.impl.file;

import edu.smu.tspell.wordnet.SynsetType;

/**
 * Provides morphology processing for lemma to be passed to WordNet.
 * 
 * @author Brett Spell
 */
public class Morphology
{

	/**
	 * Singleton instance of this class.
	 */
	private static final Morphology instance = new Morphology();

	/**
	 * Returns a reference to the singleton instance of this class.
	 * 
	 * @return Reference to the singleton instance of this class.
	 */
	public static Morphology getInstance()
	{
		return instance;
	}

	/**
	 * This constructor ensures that instances of this class can't be
	 * constructed by other classes.
	 */
	private Morphology()
	{
		// Force loading of the data
		InflectionData.getInstance();
		DetachmentRules.getInstance();
	}

	/**
	 * Returns lemma representing word forms that <u>might</u> be present
	 * in WordNet. For example, if "geese" is passed to this method (along
	 * with a parameter that indicates that noun forms should be returned),
	 * it will return the base form of "goose" which is located in WordNet
	 * ("geese", at least as of this writing, is not). It first tries to
	 * return the base forms corresponding to entries in the appropriate
	 * exception file but if none are found it attempts to apply the
	 * applicable detachment rules and returns any derivative word forms.
	 * <br><p>
	 * In addition to returning valid words that aren't stored in the
	 * database this method can also return candidates that aren't valid
	 * words at all. For example, requesting the candidate verb forms for
	 * "sing" will result in the method returning "se" and "s" because two
	 * of the detachment rules for verbs are that a suffix of "ing" should
	 * be replaced with "e" and "" (the empty string).
	 * 
	 * @param  inflection Possible irregular inflection for which to return
	 *         root words candidates.
	 * @param  type Syntactic type for which to perform the lookup.
	 * @return Root word(s) from which the inflection is derived.
	 * @see    <a href="http://wordnet.princeton.edu/man/wndb.5WN#sect5">
	 *         Format of WordNet database files ("Exception List File Format")
	 *         </a>
	 * @see    <a href="http://wordnet.princeton.edu/man/morphy.7WN">
	 *         WordNet morphological processing</a>
	 */
	public String[] getBaseFormCandidates(String inflection, SynsetType type)
	{
		DetachmentRules rules = DetachmentRules.getInstance();
		String[] detachments = rules.getCandidateForms(inflection, type);
		return detachments;
	}

	/**
	 * Provides access to morphology exception data. These represent "irregular
	 * inflections" and their corresponding root word(s), such as "geese" which
	 * is an inflected form of the root word "goose". This is useful because in
	 * some cases (as in "geese") the inflected form is not stored in WordNet but
	 * the root form is available.
	 * @param inflection
	 * @param type
	 * @return
	 */
	public String[] getExceptionCandidates(String inflection, SynsetType type)
	{
		InflectionData inflections = InflectionData.getInstance();
		String[] exceptions = inflections.getBaseForms(inflection, type);
		return exceptions;
	}

}