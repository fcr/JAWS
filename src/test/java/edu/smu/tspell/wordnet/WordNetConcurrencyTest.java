/*
 *
 * SpringSense Meaning Recognition Engine 1.0
 * Copyright (c) 2011 by SpringSense Ltd.
 *
 */
package edu.smu.tspell.wordnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author fcr
 *
 */
public class WordNetConcurrencyTest {
	
    private static Logger logger = Logger.getLogger("com.springsense.disambig.tagger.montyLinguaConcurrencyTest");
	protected static WordNetDatabase wn = null;
	protected static List<String> wordList = null;
	protected static List<Synset[]> synsetsList = null;
	/**
	 * Load the stop words needed by the tagger. Stop words are held in the
	 * data/stoplist.txt
	 */
	@SuppressWarnings({ "deprecation" })
	private  static List<String> loadWordList(String path) {
		List<String> words = new ArrayList<String>();
		DataInputStream br = null;
		try {
			InputStream file = WordNetConcurrencyTest.class.getResourceAsStream(path);
			if (file == null) {
				throw new IOException("Cannot open file: " + path);
			}
			br = new DataInputStream(file);

			String eachLine = br.readLine();

			while (eachLine != null) {
				words.add(eachLine);
				eachLine = br.readLine();
			}
			return words;
		} catch (FileNotFoundException e) {
			String msg = "Cannot open the file: " + path;
			fail(msg);
		} catch (IOException e) {
			String msg = "Error reading data from the file: " + path;
			fail(msg);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// Do nothing
			}
		}
		return words;
	}
	
	/**
	 * Tag all the sentences sequentially in order to compare with the 
	 * concurrently tagged results.
	 */
	private static void getSynsets() {
		logger.info("Building expected results.");
		long startTime = System.currentTimeMillis();
		synsetsList = new ArrayList<Synset[]>();
		for (String word: wordList) {
			Synset[] synsets = wn.getSynsets(word, SynsetType.NOUN) ;
			synsetsList.add(synsets);
		}
		synsetsList = Collections.unmodifiableList(synsetsList);
		long endTime = System.currentTimeMillis();
		logger.info("Finished building expected results in " + (endTime - startTime) + " milliseconds");
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		wn = WordNetDatabase.getFileInstance();
		wordList = loadWordList("/nouns.txt");
		wordList = Collections.unmodifiableList(wordList);
		getSynsets();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		wn = null;
		wordList = null;
		synsetsList = null;
	}
	
	protected static Synset[][] results = null;
	protected static ArrayList<Integer> scrambledIndices = null;

	
	@Before
	public void setUp() throws Exception {
		results = new Synset [wordList.size()] [];
		scrambledIndices = new ArrayList<Integer>();
		for (int i = 0; i < results.length; i++) {
			scrambledIndices.add(i);
		}
		Collections.shuffle(scrambledIndices);
		
	}

	@After
	public void tearDown() throws Exception {
		results = null;
		scrambledIndices = null;
	}

	protected class Tagger implements Runnable {
		
		private int index = 0;

		/**
		 * @param index
		 */
		public Tagger(int index) {
			super();
			this.index = index;
		}

		@Override
		public void run() {
			int i = scrambledIndices.get(index);
			Synset[] synsets = wn.getSynsets(wordList.get(i), SynsetType.NOUN) ;
			results[i] = synsets;
			
		}
		
	}

	@Test
	public void testConcurrentTag() {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		// Start a thread for each sentence to be tagged.
		logger.info("Starting " + results.length + " threads for test.");
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < results.length; i++) {
			Thread t = new Thread(new Tagger(i));
			threads.add(t);
			t.start();
		}
		
		// Wait for all threads to finish
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (Thread thread: threads) {
				allDone = allDone && !thread.isAlive();
			}
		}
		long endTime = System.currentTimeMillis();
		logger.info("All threads done in " + (endTime - startTime) + " milliseconds");
		
		// Compare results
		for (int i = 0; i < results.length; i++) {
			assertEquals(synsetsList.get(i).length, results[i].length);
		}
		
	}


}
