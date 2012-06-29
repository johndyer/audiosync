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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

// Removes all characters from a string that are unaccounted for
// while phoneticizing words from it.
public class StringCustomise {

	TreeSet<Character> ignoreChar= new TreeSet<Character>();

	public StringCustomise() {
		ignoreChar.add(' ');
	}

	public StringCustomise(List<Character> ignoreList) {

		// always ignore a blank space
		ignoreChar.add(' ');
		for (Iterator<Character> iter = ignoreList.iterator(); iter.hasNext();) {
			ignoreChar.add(iter.next());
		}
	}

	public String customise(String text) {
		StringTokenizer st = new StringTokenizer(text);
		LinkedList<String> wordTokens = new LinkedList<String>();
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			String processedWord = process(word);
			if (processedWord.compareTo("") != 0) {
				wordTokens.add(processedWord);
			}
		}
		String result = "";
		for (Iterator<String> iter = wordTokens.iterator(); iter.hasNext();) {
			result = result.concat(iter.next() + " ");
		}
		return result;
	}

	private String process(String word) {	
		
		if(word.length() >= 4) {
			if(word.substring(0, 4).compareToIgnoreCase("SIL_") == 0) {
				
				return " ";
			}
		}
		word = word.toLowerCase();
		int length = word.length();
		String processedWord = "";
		boolean notBlank=false;
		for (int i = 0; i < length; i++) {
			Character c = word.charAt(i);
			if (Character.isLetter(c)|| Character.isDigit(c)) {
				
				// if character is in [a - z], [0 - 9]
				processedWord = processedWord.concat(c.toString());
				notBlank=true;
			} else {

				// if this is a ignored character, then add it
				if (ignoreChar.contains(c)) {
					processedWord.concat(c.toString());
				}
			}
		}
		if(notBlank) {
			return processedWord;
		}
		else {
			return "";
		}
	}

}
