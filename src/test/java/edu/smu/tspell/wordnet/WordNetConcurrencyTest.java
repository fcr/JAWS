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
	protected static TaggerType monty = new MontyLingua();
	protected static List<String> sentences = null;
	protected static List<String> taggedSentences = null;
	/**
	 * Load the stop words needed by the tagger. Stop words are held in the
	 * data/stoplist.txt
	 */
	@SuppressWarnings({ "deprecation" })
	private  static List<String> loadWordList(String path) {
		List<String> sents = new ArrayList<String>();
		DataInputStream br = null;
		try {
			InputStream file = MontyLinguaConcurrencyTest.class.getResourceAsStream(path);
			if (file == null) {
				throw new IOException("Cannot open file: " + path);
			}
			br = new DataInputStream(file);

			String eachLine = br.readLine();

			while (eachLine != null) {
				sents.add(eachLine);
				eachLine = br.readLine();
			}
			return sents;
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
		return sents;
	}
	
	protected static String tagAndLemma(String text) {
		String t_text = monty.tokenize(text, 1);
		String ml_tags_str = monty.tag_tokenized(t_text);
		String ml_lemmitized_str = monty.lemmatise_tagged(ml_tags_str);
		return ml_lemmitized_str;
	}
	
	/**
	 * Tag all the sentences sequentially in order to compare with the 
	 * concurrently tagged results.
	 */
	private static void tagSentences() {
		logger.info("Tagging expected results.");
		taggedSentences = new ArrayList<String>();
		for (String sentence: sentences) {
			String tagged = tagAndLemma(sentence);
			taggedSentences.add(tagged);
		}
		taggedSentences = Collections.unmodifiableList(taggedSentences);
		logger.info("Finished tagging expected results.");
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		monty = new MontyLingua();
		sentences = loadWordList("/testData/randomSentences.txt");
		sentences = Collections.unmodifiableList(sentences);
		tagSentences();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		monty = null;
		sentences = null;
		taggedSentences = null;
	}
	
	protected static String [] results = null;
	protected static ArrayList<Integer> scrambledIndices = null;

	
	@Before
	public void setUp() throws Exception {
		results = new String [sentences.size()];
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
			String tagged = tagAndLemma(sentences.get(i));
			results[i] = tagged;
			
		}
		
	}

	@Test
	public void testConcurrentTag() {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		// Start a thread for each sentence to be tagged.
		logger.info("Staring " + results.length + " threads for tagging.");
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
		logger.info("All threads done");
		
		// Compare results
		for (int i = 0; i < results.length; i++) {
			assertEquals(taggedSentences.get(i), results[i]);
		}
		
	}


}
