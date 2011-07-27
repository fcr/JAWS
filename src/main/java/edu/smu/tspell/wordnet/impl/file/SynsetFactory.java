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
import java.util.HashMap;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetException;

/**
 * Provides a central location from which synset instances can be retrieved.
 * <br><p>
 * Synsets may be cached to improve performance and minimize memory usage
 * and when a synset is requested, the cache is first checked and the cached
 * instance will be returned if one exists. If the synset isn't found in the
 * cache, however, it will be read from disk and possibly added to the cache.
 * 
 * @author Brett Spell
 * @see <a href="http://java.sun.com/developer/technicalArticles/ALT/RefObj/">
 *      Reference Objects and Garbage Collection</a>
 */
public class SynsetFactory
{

	/**
	 * Contains references to the instances of this file that have already
	 * been created.
	 */
	private HashMap<SynsetType,SynsetReader>  readers = new HashMap<SynsetType,SynsetReader>();

	/**
	 * Singleton instance of this class.
	 */
	private static final SynsetFactory instance = new SynsetFactory();

	/**
	 * Maps pointers to their corresponding synsets.
	 * <br><p>
	 * For each entry in the map, the key is an instance of
	 * {@link SynsetPointer} and the corresponding value is an instance of
	 * {@link Synset}.
	 * 
	 * @see #synsetPointers
	 * @see #getCachedSynset(SynsetPointer)
	 */
	private HashMap<SynsetPointer,Synset> pointerSynsets = new HashMap<SynsetPointer,Synset>();

	/**
	 * Returns a reference to the singleton instance of this class.
	 * 
	 * @return Reference to the singleton instance of this class.
	 */
	public static SynsetFactory getInstance()
	{
		return instance;
	}

	/**
	 * This constructor ensures that instances of this class can't be
	 * constructed by other classes.
	 * 
	 * @throws RetrievalException An error occurred reading the frame text file.
	 */
	private SynsetFactory()
	{
	}
	
	/**
	 * Get the reader
	 *  for this type.
	 * @param type
	 * @return
	 */
	private SynsetReader getReader(SynsetType type) {
		SynsetReader reader = readers.get(type);
		if (reader == null) {
			reader = SynsetReader.getInstance(type);
			readers.put(type, reader);
		}
		return reader;
	}

	/**
	 * Returns a synset that's referenced by a pointer, reading it from disk
	 * if necessary.
	 * 
	 * @param  pointer Pointer that identifies the location of the synset in
	 *         the database.
	 * @return Synset that was read from the database either as a result of
	 *         this call or a previous one that resulted in it being cached.
	 * @throws WordNetException An error occurred reading or parsing the
	 *         synset.
	 */
	public Synset getSynset(SynsetPointer pointer)
			throws WordNetException
	{
		Synset synset = pointerSynsets.get(pointer);
		if (synset == null)
		{
			synset = readSynset(pointer);
			pointerSynsets.put(pointer, synset);
		}
		return synset;
	}

	/**
	 * Reads and returns a synset from the WordNet database.
	 * 
	 * @param  pointer Identifies the location from which to read the synset.
	 * @return Newly created synset instance.
	 * @throws RetrievalException An error occurred reading the data.
	 * @throws ParseException An error occurred parsing the data.
	 */
	private Synset readSynset(SynsetPointer pointer)
			throws RetrievalException, ParseException
	{
		Synset synset;
		String data = null;
		try
		{
			SynsetReader reader = getReader(pointer.getType());
			data = reader.readData(pointer);
			SynsetParser parser = new SynsetParser();
			synset = parser.createSynset(data);
		}
		catch (ParseException pe)
		{
			throw pe;
		}
		catch (IOException ioe)
		{
			throw new RetrievalException(
					"An error occurred reading the synset data", ioe);
		}
		catch (Exception e)
		{
			throw new ParseException(
					"An error occurred parsing the synset data: " + data, e);
		}
		return synset;
	}
	
	public void closeReaders() {
		for (SynsetReader reader: readers.values()) {
			reader.close();
		}
		readers = null;
	}

}