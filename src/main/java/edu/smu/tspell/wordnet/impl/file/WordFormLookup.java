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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetException;

/**
 * This is the main class that's used to perform lookups based upon a word
 * form.
 * <br><p>
 * A cache is maintained that allows for fast retrieval of synsets for words
 * on which a retrieval has already been performed. Specifically the caching
 * is done with a weak hash map using the word form as the key, which means
 * that as long as the caller maintains a strong reference to the string the
 * corresponding synsets will contain to be quickly accessible via the cache.
 * However, when no more (strong) references exist to the word form, its data
 * becomes eligible for garbage collection and may not be available as quickly
 * upon subsequent calls to this class. In that case the data will still be
 * returned, but it will be necessary to again read it from the database
 * instead of from the cache.
 *
 * @author Brett Spell
 * @see <a href="http://java.sun.com/developer/technicalArticles/ALT/RefObj/">
 *      Reference Objects and Garbage Collection</a>
 */
public class WordFormLookup
{

    private static Logger logger = Logger.getLogger("edu.smu.tspell.wordnet");

    /**
	 * Singleton instance of this class.
	 */
	private static final WordFormLookup instance = new WordFormLookup();;

	/**
	 * Map in which the retrieved data is cached.
	 */
	private HashMap<String, TreeMap<SynsetType, ArrayList<Synset>>> wordCategories = new HashMap<String, TreeMap<SynsetType, ArrayList<Synset>>>();

	/**
	 * Returns a reference to the singleton instance of this class.
	 *
	 * @return Singleton instance of this class.
	 */
	public static WordFormLookup getInstance()
	{
		return instance;
	}

	/**
	 * This constructor ensures that instances of this class can't be
	 * constructed by other classes.
	 */
	private WordFormLookup()
	{
		logger.info("Starting to load WordNet data to memory.");
		long startTime = System.currentTimeMillis();
		loadAllSynsets();
		Morphology.getInstance();
		long endTime = System.currentTimeMillis();
		logger.info("Finished loading WordNet data to memory in " + (endTime - startTime)/1000 + " seconds");
	}

	private SenseIndexReader reader;
	
	
	/**
	 * Check if each candidate form exists in Wordnet and if not already added to synsetList.
	 * 
	 * @param type
	 * @param candidates
	 * @param synsetList
	 * @return if a candidate was added
	 */
	private void filterCandidates(SynsetType type, List<String> candidates, List<Synset> synsetList) {
		Synset[] synsetArray;
		for (String wordForm : candidates) {
			// Get synsets for the candidate and loop through them
			synsetArray = getSynsets(wordForm, type);
			for (int k = 0; k < synsetArray.length; k++) {
				// Add (non-duplicate) synsets to the list
				if (!synsetList.contains(synsetArray[k])) {
					synsetList.add(synsetArray[k]);
				}
			}
		}
	}

	/**
	 * Returns only the synsets of the specified types (e.g., noun) that
	 * contain a word form matching the specified text and / or possibly
	 * synsets that contain one of that word form's variants. The caller
	 * can request that variants be by specifying that WordNet's morphology
	 * rules should be applied when determining which synsets to return. For
	 * example, if the caller requests that noun synsets be returned that
	 * contain the word form "masses" and the caller also requests that
	 * morphological processing be used, this method will return all noun
	 * synsets that contain <i>either</i> "masses" or "mass". That's due to
	 * the fact that one of WordNet's morphology rules, specifically a
	 * detachment rule, produces "mass" as a candidate form of "masses" as
	 * a result of stripping the "es" suffix.
	 * 
	 * @param  wordForm Text representing a word or collocation (phrase).
	 * @param  types Types of synsets (e.g., noun) to return.
	 * @param  useMorphology When <code>true</code>, indicates that this
	 *         method should return synsets that contain any morphological
	 *         variation of the specified word form; conversely, a value of
	 *         <code>false</code> returns in only synsets being returned that
	 *         contain the word for exactly as it is specified. In other words,
	 *         specifying <code>false</code> indicates that an exact-match-only
	 *         approach should be used to determine which synsets to return.
	 * @return Synsets that contain the specified word form.
	 *         If the category argument is specified, only synsets of that
	 *         type will be returned, otherwise all synsets containing the
	 *         form are returned.
	 * @throws WordNetException An error occurred retrieving the data.
	 */
	public Synset[] getSynsets(
			String externalWordForm, SynsetType[] types, boolean useMorphology)
			throws WordNetException
	{
		Synset[] synsetArray;
		List<String> candidates;
		
		String wordForm = TextTranslator.translateToDatabaseFormat(externalWordForm);

		//  Create the list that will hold the results
		List<Synset> synsetList = new ArrayList<Synset>();
		//  Loop through the synset types
		for (int i = 0; i < types.length; i++)
		{
			//  Get all synsets for the current type
			candidates = Arrays.asList(wordForm);
			filterCandidates(types[i], candidates, synsetList);
			//  Does caller also want synsets containing base form candidates?
			if (useMorphology)
			{
				//  Find possible base forms and loop through each one
		        // 0. Check the exception lists
				candidates = getExceptionCandidates(wordForm, types[i]);
				if (candidates.size() > 0) {
					filterCandidates(types[i], candidates, synsetList);
				}
				else {
					// No exceptions so..
			        // 1. Apply rules once to the input to get y1, y2, y3, etc.
					candidates = getBaseFormCandidates(wordForm, types[i]);
					filterCandidates(types[i], candidates, synsetList);
			        // 2. Return all that are in the database (and the original too) otherwise...
					while (candidates.size() > 0 && synsetList.size() == 0) {
				        // 3. If there are no matches, keep applying rules until we find a match
						ArrayList<String> newCandidates = new ArrayList<String>();
						for (String candidate: candidates) {
							newCandidates.addAll(getBaseFormCandidates(candidate, types[i]));
						}
						candidates = newCandidates;
						filterCandidates(types[i], candidates, synsetList);
					}
				}
			}
		}
		//  Convert the list to an array and return it
		synsetArray = new Synset[synsetList.size()];
		synsetList.toArray(synsetArray);
		return synsetArray;
	}

	/**
	 * Returns the synsets of a particular type (e.g., noun) that contain a
	 * specific word form.
	 * <br><p>
	 * This method first tries to retrieve the synsets from the cache but if
	 * they are not found there will attempt to read them from the database.
	 *
	 * @param  wordForm Word form for which to return containing synsets.
	 * @param  type Type of synsets to be returned.
	 * @return Synsets of a single type that contain the specified word form.
	 * @throws WordNetException An error occurred retrieving the synsets.
	 */
	private Synset[] getSynsets(String wordForm, SynsetType type)
	{
		int count;

		//  Create a list to hold the synsets we'll return
		ArrayList<Synset> synsetList = new ArrayList<Synset>();
		//  Get the map that contains a List per synset type
		TreeMap<SynsetType, ArrayList<Synset>> subMap = wordCategories.get(wordForm);
		
		if (subMap != null) {
			//  Get the synsets for this type
			ArrayList<Synset> typeList = subMap.get(type);
			//  If there are some, add them to the list
			if (typeList != null)
			{
				//  Find out how many there are
				count = typeList.size();
				//  Loop through list of synsets
				for (int j = 0; j < count; j++)
				{
					//  Add each one to the list if it isn't already there
					if (!synsetList.contains(typeList.get(j)))
					{
						synsetList.add(typeList.get(j));
					}
				}
			}
		}
		//  Convert the list to an array and return it
		Synset[] synsetArray = new Synset[synsetList.size()];
		synsetList.toArray(synsetArray);
		return synsetArray;
	}
	
	private class SynsetComparator implements Comparator<Synset> {
		
		String wordForm = "";
		
		void setComparisonWordForm(String wordForm) {
			this.wordForm = wordForm;
		}

		@Override
		public int compare(Synset o1, Synset o2) {
			return ((ReferenceSynset)o1).compareSenseIndex(wordForm, (ReferenceSynset)o2);
		}
		
	}

	/**
	 * Loads from the database all synsets in the database.
	 */
	private void loadAllSynsets() {
		
		reader = SenseIndexReader.getInstance();
		SynsetFactory factory = SynsetFactory.getInstance();
		
		//Loop through all entries in index.
		Iterator<SenseIndexEntry> iterator = reader.getSenseIndexEntryIterator();
		SynsetComparator comparator = new SynsetComparator();
		while (iterator.hasNext()) {
			SenseIndexEntry entry = iterator.next();
			String wordForm = entry.getSenseKey().getLemma();
			
			//  Get the map that contains a List per synset type
			TreeMap<SynsetType, ArrayList<Synset>> subMap = wordCategories.get(wordForm);
			if (subMap == null) {
				//  Create a new entry for the word form in the map
				subMap = new TreeMap<SynsetType, ArrayList<Synset>>();
				wordCategories.put(wordForm, subMap);
			}
			
			// Get a synset for this entry
			Synset synset = factory.getSynset(entry.getSynsetPointer());
			SynsetType type = synset.getType();
			
			// Cache the synset with its sense.
			entry.setSynset(synset);
			
			ReferenceSynset refSynset = (ReferenceSynset) synset;
			
			refSynset.populateRelationships();
			
			// Set the tag count. Even if the synset already existed, this is a new synonym.
			// This must be set AFTER the test if isPopulated.
			refSynset.setTagCount(wordForm, entry.getTagCount());
			
			
			//  Also add the new synset to our list
			ArrayList<Synset> categoryList = subMap.get(type);
			//  If this is the first one, create a new list and store it
			if (categoryList == null)
			{
				categoryList = new ArrayList<Synset>();
				subMap.put(type, categoryList);
				categoryList.add(synset);
			}
			else {
				//  Add current synset to the list for its type if it is not there.
				categoryList.add(synset);
				comparator.setComparisonWordForm(wordForm);
				Collections.sort(categoryList, comparator);
			}
		}
		factory.closeReaders();
		
	}

	
	/**
	 * Returns lemma representing word forms that <u>might</u> be present
	 * in WordNet. For example, if "geese" is passed to this method (along
	 * with a parameter that indicates that noun forms should be returned),
	 * it will return the base form of "goose".
	 * 
	 * @param  inflection Irregular inflection for which to return root words.
	 * @param  type Syntactic type for which to perform the lookup.
	 * @return Root word(s) from which the inflection is derived.
	 * @see    Morphology
	 */
	private List<String>  getExceptionCandidates(String inflection, SynsetType type)
	{
		Morphology morphology = Morphology.getInstance();
		return Arrays.asList(morphology.getExceptionCandidates(inflection, type));
	}

	/**
	 * Returns lemma representing word forms that <u>might</u> be present
	 * in WordNet..
	 * 
	 * @param  inflection Irregular inflection for which to return root words.
	 * @param  type Syntactic type for which to perform the lookup.
	 * @return Root word(s) from which the inflection is derived.
	 * @see    Morphology
	 */
	private List<String>  getBaseFormCandidates(String inflection, SynsetType type)
	{
		Morphology morphology = Morphology.getInstance();
		return Arrays.asList(morphology.getBaseFormCandidates(inflection, type));
	}

	/**
	 * Returns a set of all word forms that are in the WordNet.
	 * 
	 * @return a set of word forms
	 */
	public Set<String> allWordForms() {
		return wordCategories.keySet();
	}
	
	/**
	 * Return the synset associated with the sense key.
	 * 
	 * @param senseKey
	 * @return
	 */
	public Synset getSynsetWithSenseKey(String senseKey) {
		return reader.getFromSenseKey(senseKey);
	}

}