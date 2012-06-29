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

package edu.cmu.sphinx.demo.aligner;

public interface AudioAlignerInterface {

	// Allow dynamic audio change
	// returns true if change succeeded
	public boolean setAudio(String pathToAudio);

	// Allow dynamic transcription change
	// returns false if change failed
	public boolean setText(String text) throws Exception;
	
	// Allow Deletions
	public boolean allowDeletions();
	
	// Allow Repetions
	public boolean allowRepetions();
	
	// Allow BackwardJumps
	public boolean allowBackwardJumps();

	// optimize values for aligner configuration
	public void optimize();

	// align audio and return alignment result
	public String align() throws Exception;

	public void setAbsoluteBeamWidth(String absoluteBeamWidth);

	public void setRelativeBeamWidth(String relativeBeamWidth);
	
	public void setAddOutOfGrammarBranchProperty(String addOutOfGrammarBranch);

	public void setOutOfGrammarProbability(String outOfGrammarProbability);

	public void setPhoneInsertionProbability(String phoneInsertionProbability);

	public void setForwardJumpProbability(double prob);

	public void setBackwardJumpProbability(double prob);

	public void setSelfLoopProbability(double prob);
	
	public void setNumGrammarJumps(int n);
	
	public void performPhraseSpotting(boolean doPhraseSpotting);

}
