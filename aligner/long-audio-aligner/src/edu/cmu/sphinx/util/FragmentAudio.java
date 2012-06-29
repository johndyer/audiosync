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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author apurv This is audio fragmenting tool designed as a supplement to S4
 *         utils. Out of a number of uses one can think of you may want to use
 *         this to create Training corpus easily with help of this.
 * 
 *         NOTE: Right now it only takes WAV audio as input.
 * 
 * @param input
 *            audio file , output audio file, start time and end time.
 */

public class FragmentAudio {

	private float startTime;
	private float endtime;
	private AudioInputStream inAudio;
	private String outFile;

	public FragmentAudio(String inFile) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		this.inAudio = AudioSystem.getAudioInputStream(new URL("file:"+inFile));
		inAudio.skip(100000);	// I believe it removes some header info
	}
	public FragmentAudio(AudioInputStream inAudio) throws IOException{
		this.inAudio = inAudio;
		//inAudio.skip(10000);
	}

	public void fragment(String outFile, float startTime, float endTime)
			throws Exception {
		
		if(startTime > endTime){
			throw new Exception("Start Time should be less than end time" +
					" for fragmenting audio");
		}
		
		//System.out.println("sampling from " + startTime + " to " + endTime );
		float sampleRate = inAudio.getFormat().getSampleRate();
		int sampleSize = inAudio.getFormat().getSampleSizeInBits();
		float numOfBitsBeforeStart = startTime * sampleRate * sampleSize;
		float numOfBits = (endTime - startTime) * sampleRate * sampleSize;
		//System.out.println(numOfBitsBeforeStart/sampleSize + "   " + numOfBits/sampleSize);
		inAudio.skip((int)numOfBitsBeforeStart/8);
		File output = new File(outFile);
		output.createNewFile();
		AudioSystem.write(new AudioInputStream(inAudio, inAudio.getFormat(),
				(int) numOfBits / sampleSize), AudioFileFormat.Type.WAVE,
				new FileOutputStream(outFile));
	}
}
