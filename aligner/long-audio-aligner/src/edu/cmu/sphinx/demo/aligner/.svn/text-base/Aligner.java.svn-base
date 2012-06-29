package edu.cmu.sphinx.demo.aligner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import edu.cmu.sphinx.decoder.Decoder;
import edu.cmu.sphinx.decoder.search.AlignerSearchManager;
import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.linguist.aflat.AFlatLinguist;
import edu.cmu.sphinx.linguist.language.grammar.AlignerGrammar;
import edu.cmu.sphinx.phrasespotter.PhraseSpotterResult;
import edu.cmu.sphinx.phrasespotter.simplephrasespotter.SimplePhraseSpotter;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.recognizer.Recognizer.State;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.StringCustomise;
import edu.cmu.sphinx.util.StringErrorGenerator;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class Aligner implements AudioAlignerInterface {

	private String PROP_GRAMMAR; // which grammar to use from config
	private String PROP_RECOGNIZER; // which recognizer to use from config
	private double PROP_FORWARD_JUMP_PROB = 0.0;
	private double PROP_BACKWARD_JUMP_PROB = 0.0;
	private double PROP_SELF_LOOP_PROB = 0.0;
	private int PROP_NUM_GRAMMAR_JUMPS = 0;
	private String PROP_AUDIO_DATA_SOURCE;
	private boolean PROP_PERFORM_SPOTTING;
	private boolean PROP_MODEL_DELETIONS;
	private boolean PROP_MODEL_REPETITIONS;
	private boolean PROP_MODEL_BACKWARDJUMPS;

	private String absoluteBeamWidth;
	private String relativeBeamWidth;
	private String addOutOfGrammarBranch;
	private String outOfGrammarProbability;
	private String phoneInsertionProbability;

	private ConfigurationManager cm;
	private Recognizer recognizer;
	private AlignerGrammar grammar;
	private AudioFileDataSource datasource;

	private boolean optimize; // by default set this false

	private String config;
	private String psConfig;
	private String audioFile;
	private String textFile;
	private String txtInTranscription;
	private List<PhraseSpotterResult> phraseSpotterResult;

	public Aligner(String config, String audioFile, String textFile)
			throws IOException {
		this(config, audioFile, textFile, "recognizer", "AlignerGrammar",
				"audioFileDataSource");
	}

	public Aligner(String config, String audioFile, String textFile,
			String recognizerName, String grammarName,
			String audioDataSourceName) throws IOException {
		this(config, audioFile, textFile, recognizerName, grammarName, "",
				audioDataSourceName);
	}

	public Aligner(String config, String audioFile, String textFile,
			String recognizerName, String grammarName, String grammarType,
			String audioDataSourceName) throws IOException {
		this(config, audioFile, textFile, recognizerName, grammarName,
				audioDataSourceName, true);
	}

	public Aligner(String config, String audioFile, String textFile,
			String recognizerName, String grammarName,
			String audioDataSourceName, boolean optimize) throws IOException {
		this.config = config;
		this.audioFile = audioFile;
		this.textFile = textFile;
		this.PROP_RECOGNIZER = recognizerName;
		this.PROP_GRAMMAR = grammarName;
		this.PROP_AUDIO_DATA_SOURCE = audioDataSourceName;
		this.optimize = optimize;
		this.PROP_PERFORM_SPOTTING = false;
		this.PROP_MODEL_BACKWARDJUMPS = false;
		this.PROP_MODEL_DELETIONS = false;
		this.PROP_MODEL_REPETITIONS =  false;
		txtInTranscription = readTranscription();
		phraseSpotterResult = new LinkedList<PhraseSpotterResult>();

		cm = new ConfigurationManager(config);
		absoluteBeamWidth = cm.getGlobalProperty("absoluteBeamWidth");
		relativeBeamWidth = cm.getGlobalProperty("relativeBeamWidth");
		addOutOfGrammarBranch = cm.getGlobalProperty("addOOVBranch");
		outOfGrammarProbability = cm
				.getGlobalProperty("outOfGrammarProbability");
		phoneInsertionProbability = cm
				.getGlobalProperty("phoneInsertionProbability");
	}

	@Override
	public boolean setAudio(String path) {
		this.audioFile = path;
		return true;
	}

	@Override
	public boolean setText(String text) throws Exception {
		txtInTranscription = text;
		return true;
	}

	// Idea is to automate the process of selection and setting of
	// Global properties for alignment giving hands free experience
	// to first time users.
	@Override
	public void optimize() {

	}

	private void setGlobalProperties() {
		cm.setGlobalProperty("absoluteBeamWidth", absoluteBeamWidth);

		cm.setGlobalProperty("relativeBeamWidth", relativeBeamWidth);
		cm.setGlobalProperty("addOOVBranch", addOutOfGrammarBranch);
		cm.setGlobalProperty("outOfGrammarProbability", outOfGrammarProbability);
		cm.setGlobalProperty("phoneInsertionProbability",
				phoneInsertionProbability);
	}

	public void setPhraseSpottingConfig(String configFile) {
		psConfig = configFile;
	}

	@Override
	public String align() throws Exception {
		if (PROP_PERFORM_SPOTTING) {
			phraseSpotterResult = new LinkedList<PhraseSpotterResult>();
			collectPhraseSpottingResult();
		}

		cm = new ConfigurationManager(config);
		AlignerSearchManager sm = (AlignerSearchManager) cm
				.lookup("searchManager");
		sm.setSpotterResult(phraseSpotterResult);
		optimize();
		setGlobalProperties();
		recognizer = (Recognizer) cm.lookup(PROP_RECOGNIZER);
		grammar = (AlignerGrammar) cm.lookup(PROP_GRAMMAR);
		datasource = (AudioFileDataSource) cm.lookup(PROP_AUDIO_DATA_SOURCE);
		datasource.setAudioFile(new File(audioFile), null);
		allocate();
		return start_align();
	}

	private String start_align() throws IOException {
		// grammar.getInitialNode().dumpDot("./graph.dot");
		Result result = recognizer.recognize();
		String timedResult = result.getTimedBestResult(false, true);
		Token finalToken = result.getBestFinalToken();
		//System.out.println(result.getBestToken().getWordUnitPath());
		deallocate();
		return timedResult;
	}

	private void collectPhraseSpottingResult() throws MalformedURLException {
		StringTokenizer tok = new StringTokenizer(txtInTranscription);
		while (tok.hasMoreTokens()) {
			String phraseToSpot = "";
			int iter = 0;
			while (iter < 3 && tok.hasMoreTokens()) {
				phraseToSpot += tok.nextToken() + " ";
				iter++;
			}
			System.out.println("\n Spotting Phrase: " + phraseToSpot);
			try {

				List<PhraseSpotterResult> tmpResult = phraseSpotting(phraseToSpot);
				ListIterator<PhraseSpotterResult> iterator = tmpResult
						.listIterator();
				// System.out.println(tmpResult.size());
				while (iterator.hasNext()) {

					PhraseSpotterResult nextResult = iterator.next();

					System.out.println(nextResult);

					phraseSpotterResult.add(nextResult);
				}
			} catch (Exception e) {
				System.out
						.println("An unknown exception occured in phrase Spotter."
								+ " But Aligner will not stop");
				e.printStackTrace();
			}
			System.out
					.println("Skipping 5 words in transcription to select next phrase");
			iter = 0;
			while (iter < 5 && tok.hasMoreTokens()) {
				tok.nextToken();
				iter++;
			}
		}
	}

	private List<PhraseSpotterResult> phraseSpotting(String phrase)
			throws Exception {

		SimplePhraseSpotter phraseSpotter = new SimplePhraseSpotter(psConfig);
		phraseSpotter.setAudioDataSource(audioFile);
		phraseSpotter.setPhrase(phrase);
		long initTime = System.currentTimeMillis();
		phraseSpotter.startSpotting();
		return phraseSpotter.getTimedResult();
	}

	private void allocate() throws IOException {
		datasource.setAudioFile(new URL("file:" + audioFile), null);

		System.out.println("Transcription: " + txtInTranscription);
		grammar.setText(txtInTranscription);
		grammar.allowBackwardJumps(PROP_MODEL_BACKWARDJUMPS);
		grammar.allowDeletions(PROP_MODEL_DELETIONS);
		grammar.allowRepetions(PROP_MODEL_REPETITIONS);
		grammar.setBackWardTransitionProbability(PROP_BACKWARD_JUMP_PROB);
		grammar.setForwardJumpProbability(PROP_FORWARD_JUMP_PROB);
		grammar.setSelfLoopProbability(PROP_SELF_LOOP_PROB);
		grammar.setNumAllowedGrammarJumps(PROP_NUM_GRAMMAR_JUMPS);

		recognizer.allocate();
	}

	public void deallocate() {
		recognizer.deallocate();
	}

	private String readTranscription() throws IOException {
		BufferedReader txtReader = new BufferedReader(new FileReader(textFile));
		String line;
		String finalText = "";
		while ((line = txtReader.readLine()) != null) {
			finalText += " " + line;
		}
		StringCustomise sc = new StringCustomise();
		return sc.customise(finalText);
	}

	public void generateError(float wer) throws Exception {
		StringErrorGenerator seg = new StringErrorGenerator(wer,
				txtInTranscription);
		seg.process();
		String newText = seg.getTranscription();
		setText(newText);
	}

	public void generateError(float ir, float dr, float sr) throws Exception {
		StringErrorGenerator seg = new StringErrorGenerator(ir, dr, sr,
				txtInTranscription);
		seg.process();
		String newText = seg.getTranscription();
		setText(newText);
	}

	@Override
	public void setAbsoluteBeamWidth(String absoluteBeamWidth) {
		this.absoluteBeamWidth = absoluteBeamWidth;

	}

	@Override
	public void setRelativeBeamWidth(String relativeBeamWidth) {
		this.relativeBeamWidth = relativeBeamWidth;

	}

	@Override
	public void setOutOfGrammarProbability(String outOfGrammarProbability) {
		this.outOfGrammarProbability = outOfGrammarProbability;

	}

	@Override
	public void setPhoneInsertionProbability(String phoneInsertionProbability) {
		this.phoneInsertionProbability = phoneInsertionProbability;

	}

	@Override
	public void setForwardJumpProbability(double prob) {
		this.PROP_FORWARD_JUMP_PROB = prob;

	}

	@Override
	public void setBackwardJumpProbability(double prob) {
		this.PROP_BACKWARD_JUMP_PROB = prob;

	}

	@Override
	public void setSelfLoopProbability(double prob) {
		this.PROP_SELF_LOOP_PROB = prob;

	}

	@Override
	public void setNumGrammarJumps(int n) {
		this.PROP_NUM_GRAMMAR_JUMPS = n;
	}

	@Override
	public void performPhraseSpotting(boolean doPhraseSpotting) {
		this.PROP_PERFORM_SPOTTING = doPhraseSpotting;
	}

	@Override
	public void setAddOutOfGrammarBranchProperty(String addOutOfGrammarBranch) {
		this.addOutOfGrammarBranch = addOutOfGrammarBranch;
	}

	@Override
	public boolean allowDeletions() {
		PROP_MODEL_DELETIONS = true;
		return true;
	}

	@Override
	public boolean allowRepetions() {
		PROP_MODEL_REPETITIONS = true;
		return true;
	}

	@Override
	public boolean allowBackwardJumps() {
		PROP_MODEL_BACKWARDJUMPS = true;
		return true;
	}
}
