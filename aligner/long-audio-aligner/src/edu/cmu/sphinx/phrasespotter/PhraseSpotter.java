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

import java.net.URL;
import java.util.List;
import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.phrasespotter.PhraseSpotterResult;;

public interface PhraseSpotter extends Configurable {
	
	/**
	 * Hopefully there will be things here that will need configuration
	 */
	
	public final static String PROP_RECOGNIZER = "recognizer";
	public final static String PROP_GRAMMAR = "grammar";
	public final static String PROP_LINGUIST = "linguist";
	public final static String PROP_AUDIO_DATA_SOURCE = "audioFileDataSource";
	
	public void deallocate();
	
	public void startSpotting() throws Exception;
	
	public void setPhrase(String phrase);
	
	public List<PhraseSpotterResult> getTimedResult();
	
	public void setAudioDataSource(URL audioFile);

}