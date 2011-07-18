package edu.smu.tspell.wordnet;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WordNetDatabaseTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("wordnet.database.dir", "/WordNet-3.0/dict");
		System.setProperty("wordnet.cache.synsets", "5096");
		System.setProperty("wordnet.cache.words", "5096");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private WordNetDatabase wn = null;
	
	@Before
	public void setUp() throws Exception {
		wn = WordNetDatabase.getFileInstance();
	}

	@After
	public void tearDown() throws Exception {
		wn = null;
	}

	@Test
	public void testGetWn() {
		Synset[] synsets = wn.getSynsets("pipe");
		assertEquals(synsets.length, 9);
		Synset[] synsets_n = wn.getSynsets("pipe", SynsetType.NOUN);
		assertEquals(synsets_n.length, 5);
		String sense = synsets_n[1].getWordForms()[0];
		assertEquals("pipe", sense);
	}
	

}
