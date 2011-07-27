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
		assertEquals(9,synsets.length);
		Synset[] synsets_n = wn.getSynsets("pipe", SynsetType.NOUN);
		assertEquals(5, synsets_n.length);
		String sense = synsets_n[1].getWordForms()[0];
		assertEquals("pipe", sense);
		sense = synsets_n[1].getWordForms()[1];
		assertEquals("pipage", sense);
		sense = synsets_n[1].getWordForms()[2];
		assertEquals("piping", sense);
	}
	
	@Test
	public void testHyponyms() {
		Synset[] synsets = wn.getSynsets("pipe", SynsetType.NOUN);
		assertEquals(5, synsets.length);
		String sense = synsets[1].getWordForms()[0];
		assertEquals("pipe", sense);
		NounSynset noun = (NounSynset) synsets[1];
		Synset [] hyponyms = noun.getHyponyms();
		assertEquals(15, hyponyms.length);
	}
	
	@Test
	public void testHypernyms() {
		Synset[] synsets = wn.getSynsets("pipe", SynsetType.NOUN);
		assertEquals(5, synsets.length);
		String sense = synsets[1].getWordForms()[0];
		assertEquals("pipe", sense);
		NounSynset noun = (NounSynset) synsets[1];
		Synset [] hypernyms = noun.getHypernyms();
		assertEquals(1, hypernyms.length);
		sense = hypernyms[0].getWordForms()[0];
		assertEquals("tube", sense);
	}
	
	@Test
	public void testMeronyms() {
		Synset[] synsets = wn.getSynsets("london", SynsetType.NOUN);
		assertEquals(2, synsets.length);
		String sense = synsets[0].getWordForms()[0];
		assertEquals("london", sense);
		NounSynset noun = (NounSynset) synsets[0];
		Synset [] meronyms = noun.getMemberMeronyms();
		assertEquals(1, meronyms.length);
		sense = meronyms[0].getWordForms()[0];
		assertEquals("londoner", sense);
	}
	
	@Test
	public void testPartMeronyms() {
		Synset[] synsets = wn.getSynsets("london", SynsetType.NOUN);
		assertEquals(2, synsets.length);
		String sense = synsets[0].getWordForms()[0];
		assertEquals("london", sense);
		NounSynset noun = (NounSynset) synsets[0];
		Synset [] meronyms = noun.getPartMeronyms();
		assertEquals(18, meronyms.length);
	}
	
	@Test
	public void testPartHolonyms() {
		Synset[] synsets = wn.getSynsets("london", SynsetType.NOUN);
		assertEquals(2, synsets.length);
		String sense = synsets[0].getWordForms()[0];
		assertEquals("london", sense);
		NounSynset noun = (NounSynset) synsets[0];
		Synset [] holonyms = noun.getPartHolonyms();
		assertEquals(1, holonyms.length);
		sense = holonyms[0].getWordForms()[0];
		assertEquals("england", sense);
	}
	
	@Test
	public void testInstance() {
		Synset[] synsets = wn.getSynsets("london", SynsetType.NOUN);
		assertEquals(2, synsets.length);
		String sense = synsets[0].getWordForms()[0];
		assertEquals("london", sense);
		NounSynset noun = (NounSynset) synsets[0];
		Synset [] instanceOf = noun.getInstanceHypernyms();
		assertEquals(1, instanceOf.length);
		sense = instanceOf[0].getWordForms()[0];
		assertEquals("national_capital", sense);
	}
	

}
