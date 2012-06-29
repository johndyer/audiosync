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

public class Word implements Comparable<Word>{
	private  String word;
	private double startTime;
	private double endTime;
	private double tolerance;
	private String FLAG; // contains information of whether the word is inserted
						 // or deleted. 
	
	
	public Word () {
		this(null,"",0.0, 0.0,0.0);
	}
	public Word(String word) {
		this (word,"",0.0, 0.0, 0.0);
	}
	public Word (String word, double startTime, double endTime, double tolerance) {
		this(word,"", startTime, endTime, tolerance);
	}
	public Word(String word, String FLAG, double startTime, double endTime, double tolerance ) {
		this.word = word;
		this.FLAG = FLAG;
		this.startTime = startTime;
		this.endTime = endTime;
		this.tolerance = tolerance;
		//System.out.println(word+"("+startTime+","+endTime+") ");
	}
	
	public void insert() {
		//System.out.println("inserting:"+word);
		setFlag("insert");
	}
	
	public void delete() {
		//System.out.println("deleting:"+word);
		setFlag("delete");
	}
	
	public void substitute() {
		setFlag("del+substitute");
	}
	public void substituteWord() {
		setFlag("ins+substitute");
	}
	//get functions
	public String getWord() {
		return word;
	}
	public String getFlag() {
		return FLAG;
	}
	
	//set functions
	public void setFlag(String flag) {
		this.FLAG= flag;
	}
	
	public boolean isInserted() {
		if(FLAG.compareTo("insert")==0) {
			return true;
		}else
			return false;
	}
	public boolean isDeleted() {
		if(FLAG.compareTo("delete")== 0) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isSubstituted() {
		if(FLAG.compareToIgnoreCase("del+substitute")==0){
			return true;
		}else {
			return false;
		}
	}
	public boolean isAddedAsSubstitute() {
		if(FLAG.compareTo("ins+substitute")== 0){
			return true;
		} else {
			return false;
		}
	}
	
	double getStartTime() {
		return startTime;
	}
	
	double getEndTime() {
		return endTime;
	}
	
	public boolean isEqual(Word e) {
		if(e.getWord().compareToIgnoreCase("<unk>")==0 || 
				this.getWord().compareToIgnoreCase("<unk>")== 0){
			
			return false;
		}
		if ( e.getWord().compareToIgnoreCase(this.getWord())== 0 &&
				Math.abs(e.getStartTime() - this.getStartTime())<=tolerance &&
				Math.abs(e.getEndTime()-this.getEndTime())<=tolerance) {
			return true;
		} else {
			return false;
		}
	}
	public boolean isEqualNoTolerance(Word e) {
		if(e.getWord().compareToIgnoreCase(this.getWord())== 0){
			return true;
		} else {
			return false;
		}
	}
	public boolean isUnknownWord(){
		if(this.getWord().compareToIgnoreCase("<unk>")== 0){
			return true;
		} else {
			return false;
		}
	}
	
	// Returns 1 if not equal 
	@Override
	public int compareTo(Word arg0) {
		if(this.isEqual(arg0)){
			return 0;
		} else if(this.startTime < arg0.startTime) {
			return -1;
		} else{
			return 1;
		}
	}
}
