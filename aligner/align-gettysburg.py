#!/usr/bin/env python

import subprocess
import os
from os import path
import urllib
import codecs
import sys
import shutil
import json
import re
from collections import OrderedDict
from time import time as clock
import bookinfo

def align(argv):
    """ Main function for this module (see its docstring for usage) """
    
    init_start_time = clock()
    
    __dir__ = path.realpath(path.dirname(__file__))
    long_audio_aligner_path = __dir__ + '/long-audio-aligner'
    data_path = __dir__ + '/data'
    timings_file = __dir__ + '/data/gettysburg/gettysburg.json'
    mp3_file = __dir__ + '/data/gettysburg/gettysburgaddress.mp3'
    wav_file = __dir__ + '/data/gettysburg/gettysburgaddress.wav'
    
    # Build the Java project
    cwd = os.path.realpath(os.curdir)
    os.chdir(long_audio_aligner_path)
    print "Running ant"
    # retcode = subprocess.call(['ant'])
    # if retcode != 0:
    #   raise Exception("fail (have you ant?)")
    os.chdir(cwd)
  
    print "Creating WAV"
    subprocess.call(['sox', mp3_file, wav_file, 'rate', '16k'])

    print "Updating batch"
    f = open(long_audio_aligner_path + '/resource/batchFile.txt', 'w')
    f.write('../data/gettysburg/gettysburgaddress.txt ../data/gettysburg/gettysburgaddress.wav')
    f.close()	

    print "Aligning text"

    cwd = path.realpath(path.curdir)
    os.chdir(long_audio_aligner_path)
    retcode = subprocess.call(['java', '-Xmx3g', '-jar', 'bin/aligner.jar'])
    if retcode != 0:
        raise Exception("fail (haz Java?)")
    os.chdir(cwd)

    # Obtain the timed output
    fi = codecs.open(long_audio_aligner_path + '/timedOutput/1.txt', encoding='utf-8')
    raw_timings = fi.read().split()
    fi.close()

    verse_timings = OrderedDict()
    word_timings = []
    
    # Parse the timings out of the raw timings, and then pair up the
    # normalized word from Sphinx with the actual word from the text
    normalize_word_chunk = lambda s: re.sub(r'\W', '', s).lower()
    stip_punc = lambda s: re.sub(r'^\W+|\W+$', '', s)
    current_verse = None
    for raw_timing in raw_timings:
        matches = re.match(r'(.+)\((.+),(.+)\)', raw_timing)
        word = matches.group(1)

        start = float(matches.group(2))
        end = float(matches.group(3))
        
        # Keep track of verse timings
        #if verse_timings[current_verse]['start'] is None:
        #    verse_timings[current_verse]['start'] = start
        #verse_timings[current_verse]['end'] = end
        
        # Record word timings
        #word_timings.append({
        #    'word'  : word,
        #    'start' : start,
        #    'end'   : end,
        #})
        word_timings.append([word, start, end ])
    
    fo = codecs.open(timings_file, mode='w', encoding='utf-8')
    #fo.write(json.dumps({'verses': verse_timings, 'words': word_timings}, indent=2))
    fo.write(json.dumps({'words': word_timings}, indent=1))
    fo.close()

    print "Total execution time: %.02fs" % (clock() - init_start_time)

if __name__ == '__main__':
    try:
        align(sys.argv[1:])
    except Exception as e:
        print "Exception:", e
        sys.exit(1)
