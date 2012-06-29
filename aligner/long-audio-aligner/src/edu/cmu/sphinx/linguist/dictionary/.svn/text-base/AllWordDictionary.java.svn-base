/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package edu.cmu.sphinx.linguist.dictionary;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.sun.speech.freetts.lexicon.LetterToSoundImpl;

import edu.cmu.sphinx.linguist.acoustic.Unit;
import edu.cmu.sphinx.linguist.acoustic.UnitManager;
import edu.cmu.sphinx.util.PronunciationGenerator;
import edu.cmu.sphinx.util.props.ConfigurationManagerUtils;

public class AllWordDictionary extends FastDictionary {

	// take input from ConfigurationManager later
	/** Path to G2P model file */
	public static final String G2P_MODEL = "file:./resource/models/cmudict04_lts.bin";
	private boolean addSilEndingPronunciation;

	public AllWordDictionary() {

	}

	public AllWordDictionary(String wordDictionaryFile,
			String fillerDictionaryFile, List<URL> addendaUrlList,
			boolean addSilEndingPronunciation, String wordReplacement,
			boolean allowMissingWords, boolean createMissingWords,
			UnitManager unitManager) throws MalformedURLException,
			ClassNotFoundException {
		this(ConfigurationManagerUtils.resourceToURL(wordDictionaryFile),
				ConfigurationManagerUtils.resourceToURL(fillerDictionaryFile),
				addendaUrlList, addSilEndingPronunciation, wordReplacement,
				allowMissingWords, createMissingWords, unitManager);
	}

	public AllWordDictionary(URL wordDictionaryFile, URL fillerDictionaryFile,
			List<URL> addendaUrlList, boolean addSilEndingPronunciation,
			String wordReplacement, boolean allowMissingWords,
			boolean createMissingWords, UnitManager unitManager) {
		this.logger = Logger.getLogger(getClass().getName());

		this.wordDictionaryFile = wordDictionaryFile;
		this.fillerDictionaryFile = fillerDictionaryFile;
		this.addendaUrlList = addendaUrlList;

		this.addSilEndingPronunciation = addSilEndingPronunciation;

		this.unitManager = unitManager;
	}

	/**
	 * Returns a Word object based on the spelling and its classification. The
	 * behavior of this method is also affected by the properties
	 * wordReplacement, allowMissingWords, and createMissingWords.
	 * 
	 * @param text
	 *            the spelling of the word of interest.
	 * @return a Word object
	 * @see edu.cmu.sphinx.linguist.dictionary.Word
	 */
	@Override
	public Word getWord(String text) {		
		text = text.toLowerCase();		
		Word wordObject = wordDictionary.get(text);

		if (wordObject != null) {
			return wordObject;
		}

		String word = dictionary.get(text);
		if (word == null) {
			
			// String line = text + "\t";
			PronunciationGenerator pg;
			try {
				pg = new PronunciationGenerator(new URL(
						"file:./resource/models/abbrev.txt"), new URL(
						"file:./resource/models/num.txt"), G2P_MODEL);
				pg.loadModels();
				LinkedList<String> pronunciations = pg.toPhone(text);
				Iterator<String> iter = pronunciations.iterator();
				int count = 0;
				while (iter.hasNext()) {
					String onePronunciation = iter.next();
					String entry = text + "\t";
					if (count > 1) {
						entry = text + '(' + count + ')' + "\t";
					}
					String line = entry + onePronunciation;
					dictionary.put(text.toLowerCase(), line.toUpperCase());
					count++;
				}

			} catch (Exception e) {

				System.out.println(e.getMessage());
			}

		}
		wordObject = processEntry(text);
		return wordObject;
	}

	/**
	 * Processes a dictionary entry. When loaded the dictionary just loads each
	 * line of the dictionary into the hash table, assuming that most words are
	 * not going to be used. Only when a word is actually used is its
	 * pronunciations massaged into an array of pronunciations.
	 */
	private Word processEntry(String word) {
		List<Pronunciation> pList = new LinkedList<Pronunciation>();
		String line;
		int count = 0;
		boolean isFiller = false;

		do {
			count++;
			String lookupWord = word;
			if (count > 1) {
				lookupWord = lookupWord + '(' + count + ')';
			}
			line = (String) dictionary.get(lookupWord);
			if (line != null) {
				StringTokenizer st = new StringTokenizer(line);

				String tag = st.nextToken();
				isFiller = tag.startsWith(FILLER_TAG);
				int unitCount = st.countTokens();

				Unit[] units = new Unit[unitCount];
				for (int i = 0; i < units.length; i++) {
					String unitName = st.nextToken();
					units[i] = getCIUnit(unitName, isFiller);
				}

				if (!isFiller && addSilEndingPronunciation) {
					units = Arrays.copyOf(units, unitCount + 1);
					units[unitCount] = UnitManager.SILENCE;
				}
				pList.add(new Pronunciation(units, null, null, 1.f));
			}
		} while (line != null);

		Pronunciation[] pronunciations = new Pronunciation[pList.size()];
		pList.toArray(pronunciations);
		Word wordObject = createWord(word, pronunciations, isFiller);

		for (Pronunciation pronunciation : pronunciations) {
			pronunciation.setWord(wordObject);
		}
		wordDictionary.put(word, wordObject);

		return wordObject;
	}

	/**
	 * Create a Word object with the given spelling and pronunciations, and
	 * insert it into the dictionary.
	 * 
	 * @param text
	 *            the spelling of the word
	 * @param pronunciation
	 *            the pronunciation of the word
	 * @param isFiller
	 *            if <code>true</code> this is a filler word
	 * @return the word
	 */
	private Word createWord(String text, Pronunciation[] pronunciation,
			boolean isFiller) {
		Word word = new Word(text, pronunciation, isFiller);
		dictionary.put(text, word.toString());
		return word;
	}

}