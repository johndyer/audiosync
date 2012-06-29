/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */

package edu.cmu.sphinx.phrasespotter;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * 
 * @author apurv
 * This class kind of models a typical instance of spotted phrase in an utterance.
 * Contains the start and the end time of the entire phrase.
 * 
 */
public class PhraseSpotterResult {
	private String phraseText;
	private float startTime;
	private float endTime;
	private List<String> phrase;
		
	public PhraseSpotterResult() {
		
	}
	
	public PhraseSpotterResult(String phraseText, float startTime, float endTime) {
		this.phraseText = phraseText;
		this.startTime = startTime;
		this.endTime = endTime;
		this.phrase = new LinkedList<String>();
		processPhrase();
	}
	
	private void processPhrase() {
		StringTokenizer st = new StringTokenizer(phraseText);
		while(st.hasMoreTokens()){
			phrase.add(st.nextToken());
		}
	}
	
	public float getStartTime(){
		return startTime;
	}
	
	public float getEndTime(){
		return endTime;
	}
	
	public void setStartTime(float time){
		startTime = time;
	}
	
	public void setEndTime(float time) {
		endTime = time;
	}
	
	@Override
	public String toString(){
		return phraseText + "(" + startTime + "," + endTime + ")" ;
	}

	
	public int equals(PhraseSpotterResult obj) {
		if((Math.abs(this.getStartTime() - obj.getStartTime()) < 0.05) && 
				(Math.abs(this.getEndTime() - obj.getEndTime()) < 0.05)) {
			return 0;
		}
		
		return 1;
	}
	public String getPhraseFirstWord(){
		return phrase.get(0);
	}
	
	public String getLastWord(){
		return phrase.get(phrase.size()-1);
	}
}