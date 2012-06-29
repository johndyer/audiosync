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

package edu.cmu.sphinx.phrasespotter.simplephrasespotter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import edu.cmu.sphinx.phrasespotter.PhraseSpotter;
import edu.cmu.sphinx.phrasespotter.PhraseSpotterResult;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.linguist.phrasespottingflatlinguist.PhraseSpottingFlatLinguist;
import edu.cmu.sphinx.linguist.language.grammar.NoSkipGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;

public class SimplePhraseSpotter implements PhraseSpotter {

	private List<PhraseSpotterResult> result;
	private List<String> phrase = null;
	private String phraseText;
	private List<TimedData> timedData;
	private boolean isPhraseSet = false;

	private Recognizer recognizer;
	private PhraseSpottingFlatLinguist linguist;
	private NoSkipGrammar grammar;
	private AudioFileDataSource dataSource;

	public SimplePhraseSpotter() {

	}

	public SimplePhraseSpotter(String phraseSpotterConfig) {
		ConfigurationManager cm = new ConfigurationManager(phraseSpotterConfig);
		recognizer = (Recognizer) cm.lookup(PROP_RECOGNIZER);
		linguist = (PhraseSpottingFlatLinguist) cm.lookup(PROP_LINGUIST);
		grammar = (NoSkipGrammar) cm.lookup(PROP_GRAMMAR);
		dataSource = (AudioFileDataSource) cm.lookup(PROP_AUDIO_DATA_SOURCE);
	}

	@Override
	public void setPhrase(String phraseText) {
		this.phraseText = phraseText;
		this.phrase = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(phraseText);
		while (st.hasMoreTokens()) {
			phrase.add(st.nextToken());
		}
		grammar.setText(phraseText);
		this.isPhraseSet = true;
	}

	@Override
	public void setAudioDataSource(URL audioFile) {
		dataSource.setAudioFile(audioFile, null);
	}

	public void setAudioDataSource(String audioFile)
			throws MalformedURLException {
		setAudioDataSource(new URL("file:" + audioFile));
	}

	
	private void allocate() {
		if (!isPhraseSet) {
			throw new Error("Phrase to search can't be null");
		}
		result = new LinkedList<PhraseSpotterResult>();
		recognizer.allocate();
	}

	@Override
	public void deallocate() {
		recognizer.deallocate();
	}

	@Override
	public List<PhraseSpotterResult> getTimedResult() {
		return result;
	}

	private boolean isPhraseSet() {
		if (phrase != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void startSpotting() throws Exception {
		allocate();
		edu.cmu.sphinx.result.Result recognizedResult = recognizer.recognize();
		String timedResult = recognizedResult.getTimedBestResult(false, true);

		// Break the result into tokens and extract all time info from it.
		// I guess there should be better implementations for this using the
		// tokens
		// used to generate this result in the first place. Guess that's why I
		// call this a simple Phrase Spotter

		StringTokenizer st = new StringTokenizer(timedResult);
		//System.out.println(timedResult);
		timedData = new LinkedList<TimedData>();

		while (st.hasMoreTokens()) {
			String currentToken = st.nextToken();

			// typically this token will be like word(startTime,endTime)
			String word = currentToken.substring(0, currentToken.indexOf("("));
			String timedPart = currentToken.substring(
					currentToken.indexOf("(") + 1, currentToken.indexOf(")"));
			String startTime = timedPart.substring(0, timedPart.indexOf(","));
			String endTime = timedPart.substring(timedPart.indexOf(",") + 1);
			if (word.compareTo("<unk>") != 0) {
				timedData.add(new TimedData(word, Float.valueOf(startTime),
						Float.valueOf(endTime)));
			}
		}

		// Now since we have eliminated <unk> from the result in TimedData
		// the list should look like Phrase - Phrase - Phrase ....
		// If this is not the case, raise error.
		Iterator<TimedData> resultIter = timedData.iterator();
		while (resultIter.hasNext()) {
			Iterator<String> phraseIter = phrase.iterator();
			boolean startOfPhrase = true;
			float startTime = 0;
			float endTime = 0;
			while (phraseIter.hasNext()) {
				String word = phraseIter.next();
				if (resultIter.hasNext()) {
					TimedData data = resultIter.next();
					// if phrase is begining store the start time
					if (startOfPhrase) {
						startTime = data.getStartTime();
						startOfPhrase = false;
					}
					//System.out.println(data.getText());
					if (!(word.compareToIgnoreCase(data.getText()) == 0)) {
						grammar.getInitialNode().dumpDot("./PSGraph.dot");
						throw new Exception("Words in result don't match phrase ("
								+ word + "," + data.getText() + ")");
					}
					endTime = data.getEndTime();
				} else {
					grammar.getInitialNode().dumpDot("./PSGraph.dot");
					throw new Exception(
							"The recognizer for phrase spotting didn't exit gracefully.");
				}
			}
			result.add(new PhraseSpotterResult(phraseText, startTime, endTime));
		}

	}

	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		// Configure whatever property needs to be reset
	}

	public class TimedData {
		public String text;
		public float startTime;
		public float endTime;

		public TimedData(String text, float startTime, float endTime) {
			this.text = text;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public float getStartTime() {
			return startTime;
		}

		public float getEndTime() {
			return endTime;
		}

		public String getText() {
			return text;
		}
	}

}