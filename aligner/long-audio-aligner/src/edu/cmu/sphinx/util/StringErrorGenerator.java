package edu.cmu.sphinx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class StringErrorGenerator {

	private String text = null;
	private double wer = 0.03;
	private double ir = 0.01; // insertion rate (default= 1%)
	private double dr = 0.01; // deletion rate (default= 1%)
	private double sr = 0.01; // substitution rate (default= 1%)
	private int numSubstitutions;
	private int numInsertions;
	private int numDeletions;
	private Random rand;
	private List<String> wordsToInsert; // Contains words that will be inserted
	// or substituted.

	private int numWords = 0; // total number of words in the text
	private LinkedList<Word> words;
	private URL pathToWordFile ;

	public StringErrorGenerator() throws MalformedURLException {
		this.pathToWordFile = new URL("file:./resource/models/wordFile.txt");
	}

	/*
	 * Divides the input word error rate equally into insertions, deletions and
	 * substitution rates.
	 */
	public StringErrorGenerator(double wer, String text) throws MalformedURLException {
		this();
		this.wer = wer;
		this.ir = wer / 3;
		this.dr = wer / 3;
		this.sr = wer / 3;
		this.text = text;
	}

	// intialise un-equal error rates
	public StringErrorGenerator(double ir, double dr, double sr,
			String text) throws MalformedURLException {
		this();
		this.wer = ir + dr + sr;
		this.ir = ir;
		this.dr = dr;
		this.sr = sr;
		this.text = text;
	}
	
	// set Text to be corrupted
	public void setText(String text) {
		this.text = text;
	}

	/*
	 * Allocates Error Generator by assigning text dependent variables. Throws
	 * error when text is not set
	 */
	public void process() throws IOException {
		rand = new Random();
		textToWordList();
		
		// Load words to inserted from word file
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				pathToWordFile.openStream()));
		String line;
		wordsToInsert = new LinkedList<String>();
		while ((line = reader.readLine()) != null) {
			wordsToInsert.add(line);
		}
		// Check for compatible word error rates
		check_compatible();
		
		// make errors
		processDeletions();
		processInsertions();
		processSubstitution();
		printErrorStats();
	}
	
	// Throws error if error rates exceed acceptable bounds
	private void check_compatible() {
		if( wer > 1.0 || wer < 0) {
			throw new Error("Error: wer should be between 0 and 1.0");
		} else if (ir > 1.0 || ir < 0 ||
				   dr >1.0  || dr < 0 ||
				   sr >1.0  || sr < 0) {
			throw new Error("Error: insertion/deletion/substitution rates must be b/w 0 and 1.0");
		}
		
	}

	private void processSubstitution() {
		Double numSubs = sr * numWords;
		numSubstitutions = numSubs.intValue();
		int substitutionCount = 0;
		int currIndex = 0;
		Iterator<Word> iter = words.listIterator(0);
		
		// while number of substitution is less than total number of required
		// substitutioniterate over the list and substitute word at random
		// locations with another one.
		while (substitutionCount < numSubstitutions) {
			if (currIndex < words.size()) {
				double random = rand.nextGaussian();
				if (random <= sr && random >= -sr && 
						words.get(currIndex).getFlag().compareTo("")== 0) {
					// Substitute a word here
					Word currWord = words.get(currIndex);
					words.get(currIndex).substitute();					
					String wordToInsert= wordsToInsert.get(rand
							.nextInt(wordsToInsert.size()));
					Word word = new Word(wordToInsert,currWord.getStartTime() , 
							currWord.getEndTime(), 0.01);
					word.substituteWord();
					words.add(currIndex,word);
					iter = words.listIterator(currIndex);
					substitutionCount++;
					currIndex--;
				}
				currIndex++;
			} else {
				// if current index has exceeded the total number of words,
				// start over again
				iter = words.listIterator(0);
				currIndex = 0;
			}
		}
	}

	/*
	 * Deletes words from random locations such that the total number of
	 * deletions equals the specified number.
	 */
	private void processDeletions() {
		Double numDel = dr * numWords;
		numDeletions = numDel.intValue();
		int deletionCount = 0;
		int currIndex = 0;
		Iterator<Word> iter = words.listIterator(0);
		// while number of deletions is less than total number of required
		// deletions
		// iterate over the list and delete word from random locations.
		while (deletionCount < numDeletions) {
			if (currIndex < words.size()) {
				double random = rand.nextGaussian();
				if (random <= dr && random >= -dr &&
						words.get(currIndex).getFlag().compareTo("")== 0) {
					
					// Delete word from here
					
					words.get(currIndex).delete();
					iter = words.listIterator(currIndex);
					deletionCount++;
					currIndex--;
				}
				currIndex++;
			} else {
				// if current index has exceeded the total number of words,
				// start over again
				iter = words.listIterator(0);
				currIndex = 0;
			}
		}
	}

	/*
	 * Inserts new words at random locations such that the total number of
	 * insertions equals the specified number.
	 */
	private void processInsertions() {
		Double numIns = ir * numWords;
		numInsertions = numIns.intValue();
		int insertionCount = 0;
		int currIndex = 0;
		Iterator<Word> iter = words.iterator();
		// while number of insertions is less than total number of required
		// insertions iterate over the list and insert random word at random
		// locations.
		while (insertionCount < numInsertions) {
			if (currIndex < words.size()) {
				double random = rand.nextGaussian();
				if (random <= ir && random >= -ir &&
						words.get(currIndex).getFlag().compareTo("")==0) {
					// Insert a new word here
					String wordToInsert= wordsToInsert.get(rand
							.nextInt(wordsToInsert.size()));
					Word word = new Word(wordToInsert);
					word.insert();
					words.add(currIndex, word );
					iter = words.listIterator(currIndex);
					insertionCount = insertionCount + 1;
				}
				iter.next();
				currIndex++;
			} else {
				// if current index has exceeded the total number of words,
				// start over again
				iter = words.listIterator(0);
				currIndex = 0;
			}
		}
	}
	public String getTranscription() {
		return wordListToString();
	}
	
	public LinkedList<Word> getWordList() {
		return words;
	}
	
	private void textToWordList() throws IOException{
		if (text != null) {
			String[] wordTokens = text.split(" ");
			words = new LinkedList<Word>();
			for (int i = 0; i < wordTokens.length; i++) {
				if (wordTokens[i].compareTo("") != 0) {
					//words.add(new Word(wordTokens[i]));			
					words.add(new Word(wordTokens[i], 0.0, 0.0, 0.0));
				}
			}
			numWords = words.size();
		} else {
			throw new Error("ERROR: Can not allocate on a <null> text. ");
		}
	}
	
	private String wordListToString() {
		ListIterator<Word> iter = words.listIterator();
		String result="";
		while(iter.hasNext()){
			Word nextTok = iter.next();
			if(!(nextTok.isDeleted() || nextTok.isSubstituted()))
			result = result.concat(nextTok.getWord()+" ");
		}
		return result;
	}
	public void printErrorStats() {
		System.out.println("================== ERROR GENERATOR STATS ======================");
		System.out.println("Total Number Of Insertions Made:        "+ numInsertions);
		System.out.println("Total Number Of Deletions Made:         "+ numDeletions);
		System.out.println("Total Number Of Substitutions Made:     "+ numSubstitutions);
		Double totalErr = (double)(numDeletions+numSubstitutions+numInsertions)/(double)numWords;
		String WER = totalErr.toString();
		if(WER.length() > 6)
			WER = WER.substring(0, WER.indexOf(".")+4);
		System.out.println("WER Introduced:                         "+WER);
		//System.out.println("--------ERROR GENERATOR STATS END------");
	}
	
}
