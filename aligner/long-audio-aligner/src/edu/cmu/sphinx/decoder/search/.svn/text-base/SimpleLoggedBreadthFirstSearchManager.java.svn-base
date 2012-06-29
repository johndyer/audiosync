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

package edu.cmu.sphinx.decoder.search;

import java.util.Iterator;

import edu.cmu.sphinx.decoder.pruner.Pruner;
import edu.cmu.sphinx.decoder.scorer.AcousticScorer;
import edu.cmu.sphinx.linguist.Linguist;
import edu.cmu.sphinx.linguist.SearchState;
import edu.cmu.sphinx.linguist.aflat.AFlatLinguist;
import edu.cmu.sphinx.linguist.aflat.AFlatLinguist.GrammarState;
import edu.cmu.sphinx.util.LogMath;

public class SimpleLoggedBreadthFirstSearchManager 
extends SimpleBreadthFirstSearchManager{
	public int growSkipInterval;
	public SimpleLoggedBreadthFirstSearchManager(){
		
	}
	
	public SimpleLoggedBreadthFirstSearchManager(LogMath logMath, Linguist linguist,
			Pruner pruner,AcousticScorer scorer, ActiveListFactory activeListFactory,
            boolean showTokenCount, double relativeWordBeamWidth,
            int growSkipInterval, boolean wantEntryPruning) {
		
		super(logMath,linguist,pruner,scorer,activeListFactory,showTokenCount,
				relativeWordBeamWidth,growSkipInterval,wantEntryPruning);
		this.growSkipInterval = growSkipInterval;
	}
	
	@Override
	protected boolean recognize() {
        boolean more = scoreTokens(); // score emitting tokens
        if (more) {
            pruneBranches(); // eliminate poor branches
            currentFrameNumber++;
            if (growSkipInterval == 0
                    || (currentFrameNumber % growSkipInterval) != 0) {
                growBranches(); // extend remaining branches
            }
        }
        ActiveList actList = this.getActiveList();
        Iterator<Token> iter =actList.iterator();
        while(iter.hasNext()){
        	System.out.println("");
        	Token tok = iter.next();
        	tok.dumpTokenPath(false);
        	System.out.println("");
        	//SearchState state = tok.getSearchState();
        	//if(state instanceof GrammarState){
        	//	GrammarState gState = (GrammarState)state;
        	//	System.out.print(gState.toPrettyString()+" ");
        	//}
        }
        System.out.println("");
        return !more;
    }

}
