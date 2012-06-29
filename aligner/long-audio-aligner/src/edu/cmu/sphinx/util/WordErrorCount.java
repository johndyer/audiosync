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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class WordErrorCount {
	private LinkedList<Word> reference;	// contains words that are reference transcription
	private LinkedList<Word> allWordReference; // contains all words in reference
										// i.e. including the ones inserted and deleted.
	private LinkedList<Word> hypothesis;
	private LinkedList<Word> alignedList; // contains the final aligned Result
	
	private HashMap<Word, Word> alignedMap; // Maps aligned words in hypothesis with words in
											// reference
	
	private int totalNumWords;
	private int totalInsertions;
	private int totalDeletions;
	private int totalSubstitutions;
	private int totalWordErrors;
	private int artificialInsertions;	// keeps count of the number of words 
										// inserted due to corrupted transcription
	
	private int correctedDeletions;	// keeps count of the number of words
										// deleted due to corrupted transcription
	private int correctedSubstitutions; // number of words that were removed via substitution
									// and were correctly added back
	
	private int removedSubstitute;	// number of words that were added as substitute 
									// and now have been removed
	private int correctedInsertions;
	private Double wer;
	
	public WordErrorCount() {
		totalNumWords = 0;
		totalInsertions= 0;
		totalDeletions = 0;
		totalSubstitutions = 0;
		totalWordErrors = 0;
		wer = 0.0;
		artificialInsertions = 0;
		correctedDeletions = 0;
		removedSubstitute =0;
		correctedSubstitutions = 0;
		correctedInsertions = 0;
	}
	public WordErrorCount(String reference, String hypothesis) {
		this();
		this.allWordReference = textToWordList(reference);
		this.reference = new LinkedList<Word>();
		this.alignedMap = new HashMap<Word, Word>();
		generateRef();
		this.hypothesis = textToWordList(hypothesis);
		totalNumWords = this.reference.size();
	}
	
	private void generateRef() {
		Iterator<Word> iter = allWordReference.iterator();
		while(iter.hasNext()) {
			Word nextWord = iter.next();
			
			// if the word was inserted in the transcription by ErrGen
			// we don't want that in the reference
			if(!nextWord.isInserted()) {
				reference.add(nextWord);
			}
		}
	}
	
	public LinkedList<Word> textToWordList(String text) {
		LinkedList <Word> wordList = new LinkedList<Word>();
		StringTokenizer st = new StringTokenizer(text);
		while(st.hasMoreTokens()) {
			String word = st.nextToken();
			if(word.compareTo("") != 0 ) {
				//hypothesis.add(word);
				String textPart = word.substring(0,word.indexOf("("));
				String timedPart = word.substring(word.indexOf("(") + 1,
								word.indexOf(")"));
				String startTime = timedPart.substring(0,timedPart.indexOf(","));
				String endTime = timedPart.substring(timedPart.indexOf(",")+1);	
				if(textPart.compareToIgnoreCase("<unk>") != 0) {
					wordList.add(new Word(textPart, Double.valueOf(startTime),
										Double.valueOf(endTime),1.0));
				}
			}
		}
		return wordList;
	}
	
	// Aligns the reference and hypothesis strings and determines the 
	// number of insertions and deletions made in the hypothesis string.
	
	public void align() {
		
		// Create backtrace lattice
		BackTraceObj[][] lattice = createBackTraceLattice();
		
		// Now use traceback info to add all those words from ref 
		// that match and you are done.
		alignedList = traceBack(lattice);
		
		generateStats();
		printStats();
	}
	
	
	private BackTraceObj[][] createBackTraceLattice() {
		BackTraceObj [][]lattice = new BackTraceObj [reference.size() + 1][hypothesis.size()+1];
		for (int j=0; j< hypothesis.size() +1; j++) {
			for(int i= 0; i< reference.size()+ 1; i++) {
				lattice[i][j]= new BackTraceObj();
			}
		}
		
		// We will make two Forward pass of this lattice and one Trace-back pass
		// First forward pass is to intialise  match index for each BackTraceObj
		// Second pass to initialise longest Match length and traceback info
		
		// PASS 1 : A typical element of the lattice would be (i , j)
		
		for(int j= 1; j< hypothesis.size()+ 1; j++) {
			for(int i = 1; i < reference.size()+ 1; i++) {
				Word currRef =  reference.get(i-1);
				if( currRef.isEqual(hypothesis.get(j-1))) {
					lattice[i][j].match = 1;
				}
			}
		}
		
		// PASS 2 : We now move breadth first on the lattice. 
		// 			Updating longest match info. 
		// NOTE : if possible we always want to have longest match w/o using
		// currRef word since it can be used later (kind of Greedy approach)
		
		for(int j= 1; j< hypothesis.size()+ 1; j++) {
			for(int i= 1; i < reference.size()+ 1; i++){
				
				// case 1: match = 1 for this lattice object
				if (lattice[i][j].match == 1) {
					if(lattice[i-1][j].longestMatchLength >= 
						lattice[i][j-1].longestMatchLength ) {
						
						if(lattice[i-1][j].hypUsed == true) {
							lattice[i][j].longestMatchLength = 
								lattice[i-1][j].longestMatchLength;
						} else {
							lattice[i][j].longestMatchLength = 
								lattice[i-1][j].longestMatchLength + 1;
							lattice[i][j].refUsed = true;
						}
						lattice[i][j].hypUsed = true;
						lattice[i][j].prevRef = i-1;
						lattice[i][j].prefHyp = j;						
					} else {
						if(lattice[i][j-1].refUsed) {
							lattice[i][j].longestMatchLength = 
								lattice[i][j-1].longestMatchLength;
						} else {
							lattice[i][j].longestMatchLength = 
								lattice[i][j-1].longestMatchLength +1;
							lattice[i][j].hypUsed = true;
						}
						
						// No matter if ref was used the traceback info remains 
						// the same
						lattice[i][j].refUsed = true;
						lattice[i][j].prevRef = i;
						lattice[i][j].prefHyp = j-1;
					}					
				} else {
					
					// Case 2: match = 0. A bit more tricky so follow wisely.
					// Our aim is to not use currRef as far as possible
					if(lattice[i-1][j].longestMatchLength >= 
						lattice[i][j-1].longestMatchLength) {
						lattice[i][j].longestMatchLength = 
							lattice[i-1][j].longestMatchLength;
						lattice[i][j].refUsed = false;
						lattice[i][j].hypUsed = 
							lattice[i-1][j].hypUsed;
						lattice[i][j].prevRef = i-1;
						lattice[i][j].prefHyp = j;
					} else {
						lattice[i][j].longestMatchLength = 
							lattice[i][j-1].longestMatchLength;
						lattice[i][j].refUsed = 
							lattice[i][j-1].refUsed;
						lattice[i][j].hypUsed =false;
						lattice[i][j].prevRef = i;
						lattice[i][j].prefHyp = j-1;
					}
				}
			}
		}
		
		// Now since the lattice is made let's print it for once.
		//printLattice(lattice);
		return lattice;
	}
	
	public LinkedList<Word> traceBack(BackTraceObj [][]lattice) {
		
		// Start from the lower right corner and traceback.
		// Add a word from ref only when there is a change in 
		// LongestMatchlength.

		LinkedList<Word> alignedList= new LinkedList<Word>();
		int lastRefAdded = reference.size()+1;
		int lastHypAdded = hypothesis.size()+1;
		int i = reference.size();
		int j = hypothesis.size();
		while( i > 0 && j > 0){
			//System.out.println(i+ " " + j);
			int prevRef = lattice[i][j].prevRef;
			int prevHyp = lattice[i][j].prefHyp;
			if( lattice[i][j].longestMatchLength > 
				lattice[prevRef][prevHyp].longestMatchLength) {
				alignedList.add(0,reference.get(i-1));
				alignedMap.put(reference.get(i-1), hypothesis.get(j-1));
				
				// update the number of words from ref that have been skipped
				/*
				int numRefSkipped = lastRefAdded - i - 1;
				int numHypSkipped = lastHypAdded - j - 1;
				if(numRefSkipped < 0 || numHypSkipped < 0){
					throw new Error ("ERROR: THERE IS SOMETHING WRONG IN REFERENCE" 
							+"/HYPOTHESIS. ONE WORD CAN'T MATCH WITH MORE THAN ONE WORDS");
				} else {
					int numSubs = Math.min(numRefSkipped, numHypSkipped);
					int numIns = numHypSkipped - numSubs;
					int numDels = numRefSkipped - numSubs;
					totalSubstitutions += numSubs;
					totalInsertions += numIns;
					totalDeletions += numDels;
					System.out.println("("+numIns+","+numDels+","+numSubs+")");
				}
				*/
				lastRefAdded = i;
				lastHypAdded = j;				
			}
			i = prevRef;
			j = prevHyp;
		}
		return alignedList;
	}
	
	// Prints a pretty lattice 
	private void printLattice(BackTraceObj[][] lattice) {
		for(int j=1; j< hypothesis.size()+ 1; j++) {
			for(int i=1; i< reference.size()+ 1; i++) {
				//System.out.print(lattice[i][j].match);
				System.out.print(" ( "+lattice[i][j].longestMatchLength+" ) ");
				//System.out.print("("+lattice[i][j].prevRef+", "+
				//		lattice[i][j].prefHyp+") ");
			}
			System.out.println("");
		}
	}
	
	private void generateStats() {
		// Add one final word to allow paritioning of all lists by tokens from
		// aligned list
		Word finalWord = new Word("FINAL", 10000000, 10000001, 1);
		alignedList.addLast(finalWord);
		allWordReference.addLast(finalWord);
		hypothesis.addLast(finalWord);
		ListIterator<Word> alignedIter = alignedList.listIterator();
		ListIterator<Word> referenceIter = allWordReference.listIterator();
		ListIterator<Word> hypIter = hypothesis.listIterator();		
		while(alignedIter.hasNext()){
			int numUnkWordsInRef = 0;
			int numUnkWordsInHyp = 0;
			
			int numInsertedWordsInRef = 0;
			int numDeletedWordsInRef = 0;
			int numSubstitutedWordsInRef = 0;
			int numWordSubstitutesInRef = 0;
			boolean hypContainsUnkWord = false;
			
			Word nextAlignedWord = alignedIter.next();
			Set<Word> allWordSet = new TreeSet<Word>();
			Set<Word> hypSet = new TreeSet<Word>();
			Word refWord = referenceIter.next();
			while(!refWord.isEqual(nextAlignedWord) && referenceIter.hasNext()){
				allWordSet.add(refWord);				
				if(refWord.isUnknownWord()){
					numUnkWordsInRef++;
				}
				if(refWord.isDeleted()){
					numDeletedWordsInRef++;
				} else if(refWord.isSubstituted()){
					numSubstitutedWordsInRef++;
				} else if(refWord.isInserted()){
					numInsertedWordsInRef++;
				} else if(refWord.isAddedAsSubstitute()){
					numWordSubstitutesInRef ++;
				}
				refWord = referenceIter.next();			
			}
			
			Word hypWord = hypIter.next();
			while(!hypWord.isEqual(nextAlignedWord) && hypIter.hasNext()){
				hypSet.add(hypWord);
				if(hypWord.isUnknownWord()){
					numUnkWordsInHyp++;
					hypContainsUnkWord=true;
				}
				hypWord = hypIter.next();				
			}
			if(hypContainsUnkWord) {
				correctedDeletions += numDeletedWordsInRef;
				correctedSubstitutions += numSubstitutedWordsInRef;
			}
			correctedInsertions += numInsertedWordsInRef;
			removedSubstitute += numWordSubstitutesInRef;
			
			int numRefSkipped = allWordSet.size()-numUnkWordsInRef;
			int numHypSkipped = hypSet.size() - numUnkWordsInHyp;
			int numSubs = Math.min(numRefSkipped, numHypSkipped);
			int numIns = numHypSkipped - numSubs;
			int numDels = numRefSkipped - numSubs;
			totalSubstitutions += numSubs;
			totalInsertions += numIns;
			totalDeletions += numDels;
			//System.out.println("("+numIns+","+numDels+","+numSubs+")");
			
		}
		
		// remove the Final word that we added to regain the list
		alignedList.removeLast();
		allWordReference.removeLast();
		hypothesis.removeLast();
		
		totalWordErrors= totalDeletions+totalInsertions+totalSubstitutions;
		wer = (double)totalWordErrors/(double)totalNumWords;		
	}
	
	public void printStats() {
		System.out.println("Total Number Of Errors Present:         "+totalWordErrors);
		System.out.println("Total Insertions Present:               "+totalInsertions);
		System.out.println("Total Deletions Present:                "+totalDeletions);
		System.out.println("Total Substitutions Present:            "+totalSubstitutions);
		System.out.println("Total Corrected Deletions:              "+correctedDeletions);
		System.out.println("Total Corrected Insertions:             "+correctedInsertions);
		System.out.println("Total Num. Of Words Removed Via");
		System.out.println("Substitution And Now Added:             "+correctedSubstitutions);
		System.out.println("Total Number Of Words Added Via");
		System.out.println("Substitution And Now Removed:           "+removedSubstitute);
		String WER = wer.toString();
		if(WER.length() > 6)
			WER = WER.substring(0, WER.indexOf(".")+ 4);
		System.out.println("WER:                                    "+WER);
	}
	
	public int totalDeletionsAndSubstitutions(){
		return totalDeletions;
	}
	
	public int totalInsertions() {
		return totalInsertions;
	}
	
	public int totalCorrectedDeletions() {
		return correctedDeletions;
	}
	
	public class BackTraceObj {
		public int match;	// 0 if no match, 1 if doesn't match
		public int longestMatchLength; 	
		public boolean refUsed;		// true if currRef was used to 
									// get longest match
		public boolean hypUsed;
		
		// TraceBack info
		public int prevRef;
		public int prefHyp;
		
		
		public BackTraceObj() {
			this.match = 0;
			this.longestMatchLength = 0;
			this.refUsed = false;
			this.hypUsed = false;
		}
	}	
	
}
