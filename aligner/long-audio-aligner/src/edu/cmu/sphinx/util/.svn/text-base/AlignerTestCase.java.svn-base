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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

// An aligner test case contains:
// i) the original transcription
// ii)the corrupted transcription
// iii) positions of injected errors
public class AlignerTestCase {
	private final String originalTranscription;
	private LinkedList<Word> corruptedTranscription;
	
	public AlignerTestCase (String text) {
		originalTranscription = text;		
	}
	
	public AlignerTestCase (String text, double wer,String pathToWordFile ) 
	throws IOException {
		this(text);
		
		StringErrorGenerator seg = new StringErrorGenerator(wer, pathToWordFile);
		seg.setText(originalTranscription);
		seg.process();
		corruptedTranscription = seg.getWordList();
	}
	
	public AlignerTestCase (String text, double ir, double dr,
							double sr, String pathToWordFile) throws IOException {
		this(text);
		
		StringErrorGenerator seg = new StringErrorGenerator(ir, dr, sr, pathToWordFile);
		seg.setText(originalTranscription);
		seg.process();
		corruptedTranscription = seg.getWordList();
	}

	public String getCorruptedText() {
		return generateTranscription();
	}
	
	public String generateTranscription() {		
		Iterator<Word> iter = corruptedTranscription.iterator();
		String result = "";
		while (iter.hasNext()) {
			Word nextWord = iter.next();
			if(!nextWord.isDeleted() && !nextWord.isSubstituted()) {
				result = result.concat(nextWord.getWord() + " ");
			}
		}
		return result;
	}
	public LinkedList<Word> getWordList() {
		return corruptedTranscription;
	}
}
