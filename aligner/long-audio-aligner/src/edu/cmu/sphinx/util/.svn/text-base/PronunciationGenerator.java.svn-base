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
package edu.cmu.sphinx.util;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.logging.Logger;

import com.sun.speech.freetts.lexicon.LetterToSoundImpl;

/*
 * Normalises string tokens which are abbreviations or numbers represented in base 10
 */
public class PronunciationGenerator {
	private URL abbrevFile;
	private URL numberFile;
	protected HashMap<String, LinkedList<String>> abbrevMap;
	protected HashMap<String, LinkedList<String>> numberMap;
	protected boolean abb_model_loaded;
	protected boolean num_model_loaded;
	protected String G2P_MODEL;
	protected Logger logger;

	public PronunciationGenerator() {

	}

	public PronunciationGenerator(URL abbrevFile, URL numberFile, String g2p_model) {
		this.abbrevFile = abbrevFile;
		this.numberFile = numberFile;
		this.G2P_MODEL = g2p_model;
		abbrevMap = new HashMap<String, LinkedList<String>>();
		numberMap = new HashMap<String, LinkedList<String>>();
		abb_model_loaded = false;
		num_model_loaded = false;
	}

	public void loadModels() throws IOException {
		loadAbrrevModel();
		loadNumModel();
	}

	/*
	 * Load Hash Map for Abbreviations
	 */
	protected void loadAbrrevModel() throws IOException {
		InputStreamReader isr = new InputStreamReader(abbrevFile.openStream());
		BufferedReader abbrevFileReader = new BufferedReader(isr);
		String line = null;
		while ((line = abbrevFileReader.readLine()) != null) {
			if (!line.isEmpty()) {
				int spaceIndex = line.indexOf(' ');
				int spaceIndexTab = line.indexOf('\t');
				if (spaceIndex == -1) {
					// Case where there's no blank character
					spaceIndex = spaceIndexTab;
				} else if ((spaceIndexTab >= 0) && (spaceIndexTab < spaceIndex)) {
					// Case where there's a blank and a tab, but the tab
					// precedes the blank
					spaceIndex = spaceIndexTab;
				}
				if (spaceIndex == -1) {
					throw new Error("Error loading : " + line);
				}
				String word = line.substring(0, spaceIndex);
				line = line.substring(spaceIndex + 2);
				word = word.toLowerCase();
				LinkedList<String> resultMap = abbrevMap
						.get(word.toLowerCase());
				if (resultMap != null) {
					resultMap.add(line.toLowerCase());
					abbrevMap.put(word, resultMap);
				} else {
					resultMap = new LinkedList<String>();
					resultMap.add(line.toLowerCase());
					abbrevMap.put(word, resultMap);
				}
			}
		}
		abb_model_loaded = true;
	}

	/*
	 * Loads hash map for Numbers
	 */
	protected void loadNumModel() throws IOException {
		InputStreamReader isr = new InputStreamReader(numberFile.openStream());
		BufferedReader abbrevFileReader = new BufferedReader(isr);
		String line = null;
		while ((line = abbrevFileReader.readLine()) != null) {
			if (!line.isEmpty()) {
				int spaceIndex = line.indexOf(' ');
				int spaceIndexTab = line.indexOf('\t');
				if (spaceIndex == -1) {
					// Case where there's no blank character
					spaceIndex = spaceIndexTab;
				} else if ((spaceIndexTab >= 0) && (spaceIndexTab < spaceIndex)) {
					// Case where there's a blank and a tab, but the tab
					// precedes the blank
					spaceIndex = spaceIndexTab;
				}
				if (spaceIndex == -1) {
					throw new Error("Error loading : " + line);
				}
				String word = line.substring(0, spaceIndex);
				line = line.substring(spaceIndex + 2);
				word = word.toLowerCase();
				LinkedList<String> resultMap = numberMap.get(word);
				if (resultMap != null) {
					resultMap.add(line.toLowerCase());
					numberMap.put(word, resultMap);
				} else {
					resultMap = new LinkedList<String>();
					resultMap.add(line.toLowerCase());
					numberMap.put(word, resultMap);
				}
			}
		}
		num_model_loaded = true;
	}

	/*
	 * If there exists a abbreviation in hash map, word is probably an
	 * abbreviation
	 */
	public boolean isAbbrev(String word) {
		if (word != null) {
			LinkedList<String> result = abbrevMap.get(word.toLowerCase());
			if (result != null)
				return true; // abbreviation exists!
			else
				return false; // abbreviation doesn't exist in the model
		} else {
			return false;
		}
	}

	/*
	 * A simple hack to see if the string is a number by typecasting
	 */
	public boolean isNum(String word) {
		try {
			float num = Float.parseFloat(word);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public LinkedList<String> decode_Abb(String word) throws IOException {
		if (!abb_model_loaded)
			loadAbrrevModel();
		return abbrevMap.get(word.toLowerCase());
	}

	/*
	 * A number of possible hypothesis which are considered here (for
	 * 123456789.1234): i) one two three four five six seven eight nine point
	 * one two three four ii) One hundred twenty three million four hundred
	 * fifty six thousand seven hundred eighty nine point one two three four
	 * iii) Twelve crore thirty four lakh fifty six thousand seven hundred
	 * eighty nine point one two three four
	 */
	public LinkedList<String> decode_num(String word) throws IOException {
		
		if (!num_model_loaded)
			loadNumModel();
		LinkedList<String> result = new LinkedList<String>();
		int decimalIndex = word.indexOf(".");
		String simpleResult = "";
		if (decimalIndex == -1) {
			simpleResult = simpleNum(word);
		} else {
			// process both fragments individually and add a decimal "point"
			// in between
			String floor = word.substring(0, decimalIndex);
			simpleResult = simpleNum(floor);
			simpleResult = simpleResult.concat(" point "
					+ simpleNum(word.substring(decimalIndex + 1)));
		}
		result.add(simpleResult);
		
		return result;
	}

	private String longNumbers(String word) {
		
		String result = "";
		int length = word.length();
		if(length % 2 != 0){
			int n = Integer.valueOf(word.substring(0, 2));
		}
		
		for(int i = 0; 2*i < word.length(); i++){
			String tmpNum = word.substring(2*i, 2*i + 2);
			if(tmpNum.compareToIgnoreCase("00") != 0) {
				result += numberMap.get(tmpNum).getFirst() + " ";
			} else {
				result += "hundred ";
			}
			
		}

		return result;
	}

	/*
	 * Returns Simplest possible pronunciation for a number (for 1234.1234): one
	 * two three four point one two three four
	 */
	private String simpleNum(String word) {
		String result = "";
		if(word.length() == 0){
			return result;
		}
		if(word.length() % 2 == 0) {
			for (int i = 0; 2*i < word.length(); i++) {
				String currNum = word.substring(2*i, 2*i + 2);			
				result = result.concat(" " + numberMap.get(currNum).getFirst());
			}
		} else {
			String tmpWord = word.substring(0, 1);
			result = numberMap.get(tmpWord) + " " + simpleNum(word.substring(1, word.length()));
		}
		return result;
	}

	/*
	 * For an input word, search if it is a abbreviation or a number, and if so
	 * find all possible pronunciations For each pronunciation, return the
	 * equivalent phone represenation. If no match is found in numbers or
	 * abbreviation, pronunciation for the word is returned as it is.
	 */
	public LinkedList<String> toPhone(String word)
			throws MalformedURLException, IOException {
		LetterToSoundImpl g2p = new LetterToSoundImpl(new URL(G2P_MODEL), true);
		LinkedList<String> textResult = null;
		LinkedList<String> phoneResult = new LinkedList<String>();		
		if (isAbbrev(word)) {
			textResult = decode_Abb(word);
			
		} else if (isNum(word)) {
			textResult = decode_num(word);
		}
		if (textResult == null) {
			phoneResult.add(wordPronunciation(word, g2p));
			return phoneResult;
		}

		Iterator iter = textResult.iterator();
		while (iter.hasNext()) {
			String text = iter.next().toString();
			String oneResult = wordPronunciation(text, g2p);
			phoneResult.add(oneResult);
		}
		return phoneResult;
	}

	/*
	 * returns one pronunciation hypothesis per input text.
	 */
	protected String wordPronunciation(String text, LetterToSoundImpl g2p) {
		String[] phones = g2p.getPhones(text, "");
		String line = "";
		// make one string out of the array for phones for a word
		for (int i = 0; i < phones.length; i++) {
			String currPhone = phones[i];

			// remove integers from the back of individual phones
			if (currPhone.substring(currPhone.length() - 1).compareTo("1") == 0
					|| currPhone.substring(currPhone.length() - 1).compareTo(
							"2") == 0)
				currPhone = currPhone.substring(0, currPhone.length() - 1);
			if (currPhone.compareToIgnoreCase("ax") == 0)
				currPhone = "ah";
			line = line.concat(" " + currPhone);
		}
		
		return line.toLowerCase();
	}
}
