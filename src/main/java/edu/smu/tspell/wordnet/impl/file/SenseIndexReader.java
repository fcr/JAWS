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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import edu.smu.tspell.wordnet.SynsetType;

/**
 * Locates entries in the <code>index.sense</code> file.
 * 
 * @author Brett Spell
 */
public class SenseIndexReader
{

	/**
	 * Name of the sense index file.
	 */
	private final static String SENSE_INDEX_FILE = "index.sense";

	/**
	 * Reference to the singleton instance of this class.
	 */
	private static WeakReference<SenseIndexReader> reference;

	/**
	 * Used to parse lines read from the sense index file.
	 */
	private SenseIndexParser parser = new SenseIndexParser();
	
	/**
	 * Full cache of parsed Sense Index Entries.
	 * Only needed until the Wordnet is fully loaded.
	 */
	private HashMap<String, SenseIndexEntry> entries = new HashMap<String, SenseIndexEntry> ();
	
	private HashMap<String, ArrayList<SenseIndexEntry>> satelliteEntries = new HashMap<String, ArrayList<SenseIndexEntry>> ();

	/**
	 * Returns a reference to the singleton instance of this class.
	 * 
	 * @return Singleton instance of this class.
	 * @throws RetrievalException An error occurred opening the index file.
	 */
	public static SenseIndexReader getInstance()
			throws RetrievalException
	{
		SenseIndexReader instance = null;
		//  See if there's one we can get to through the weak reference
		if (reference != null)
		{
			instance = (SenseIndexReader)(reference.get());
		}
		//  It was either garbage collected or never created in the first place
		if (instance == null)
		{
			//  Create a new one and create a weak reference to it
			try
			{
				instance = new SenseIndexReader();
				reference = new WeakReference<SenseIndexReader>(instance);
			}
			catch (IOException ioe)
			{
				throw new RetrievalException(
						"Error opening index file: " + ioe.getMessage(), ioe);
			}
		}
		return instance;
	}

	/**
	 * Reads the exceptions from a single file that correspond to the
	 * exceptions for a particular synset type.
	 * 
	 * @param  fileName Name of the file to read.
	 * @param  type Syntactic type associated with the file.
	 * @throws RetrievalException An error occurred reading the exception data.
	 */
	private void loadSenseIndexEntries(String fileName) throws IOException
	{
		String dir = PropertyNames.databaseDirectory;
		InputStream file = getClass().getResourceAsStream(dir + fileName);
		LineIterator iterator = IOUtils.lineIterator(file, null);
		//  Loop through all lines in the file
		while (iterator.hasNext())
		{
			String line = (String)iterator.next();
			//  Parse the index line
			SenseIndexEntry entry = parser.parse(line);
			String key = entry.getSenseKey().getFullSenseKeyText();
			entries.put(key, entry);
			
			// Deal with any adjective satellites
			if (entry.getSenseKey().getType() == SynsetType.ADJECTIVE_SATELLITE) {
				key = entry.getSenseKey().getPartialSenseKeyText();
				ArrayList<SenseIndexEntry> list = satelliteEntries.get(key);
				if (list == null) {
					// this is our first one for this key.
					list = new ArrayList<SenseIndexEntry>();
					satelliteEntries.put(key,  list);
				}
				list.add(entry);
			}
		}
		file.close();
	}

	/**
	 * This constructor ensures that instances of this class can't be
	 * constructed by other classes.
	 */
	private SenseIndexReader() throws IOException
	{
		loadSenseIndexEntries(SENSE_INDEX_FILE);
	}

	/**
	 * Returns the entry that contains the specified prefix. This should be
	 * used when it's expected that there will be exactly one (or no) matching
	 * entry and this method will throw an exception if it doesn't find exactly
	 * one matching occurrence.
	 * 
	 * @param  prefix Prefix of the line to return.
	 * @return Entry from the sense index file that contains the prefix.
	 * @throws RetrievalException An error occurred reading the index file or
	 *         more than one entry was found that matched the specified prefix.
	 */
	public SenseIndexEntry getEntry(String prefix) throws RetrievalException
	{
		return entries.get(prefix);
	}
	
	private class SenseIndexEntryIterator implements Iterator<SenseIndexEntry> {
		
		Iterator<SenseIndexEntry> it = entries.values().iterator();

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public SenseIndexEntry next() {
			return it.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
			
		}
		
	}
	
	/**
	 * Given a word form, returns all entries from the index file that begin
	 * with the specified text. No modifications or additions are made to the
	 * specified prefix before it is compared with the index file lines, so
	 * only those that match it exactly will be returned.
	 * 
	 * @param  prefix Prefix for which to return index file lines.
	 * @return All entries that begin with the specified text.
	 * @throws RetrievalException An error occurred reading the index file.
	 */
	public ArrayList<SenseIndexEntry> getAllEntries(String prefix)
			throws RetrievalException
	{
		ArrayList<SenseIndexEntry> list = satelliteEntries.get(prefix);
		return list;
	}

	/**
	 * Iterate over all entries in the SenseIndex.
	 * 
	 * @return Iterator
	 */
	public Iterator<SenseIndexEntry> getSenseIndexEntryIterator() {
		return new SenseIndexEntryIterator();
	}

}