# -*- coding: utf-8 -*-
"""
Created on Tue Jun 09 23:34:06 2015

@author: daniel
"""

import sqlite3
import os
import numpy as np
from sklearn.hmm import GaussianHMM
import cPickle as pickle


class SimpleDBHandler(object):
    
    
    def __init__(self, path_database):

        self.dbFile = os.path.join(os.path.dirname(__file__), path_database)
        
        'Create or connect to local db (just a file):'
        self.connection = sqlite3.connect(self.dbFile)   
            
        'Fetch cursor to work with db:'
        self.cursor = self.connection.cursor()
   



def get_sequences_by_label(label, path_database):
    
    db = SimpleDBHandler(path_database)
    db.cursor.execute("""SELECT seqs.id, s.accX, s.accY, s.accZ
            FROM sequences seqs, sensor_data s WHERE seqs.id=s.seq_id AND
            seqs.label = '%s' ORDER BY s.seq_id ASC, s.id ASC""" % (label))
    sequences = []
    current_sequence = []
    last_id = -1
    for row in db.cursor:
        current_id = row[0]
        current_sequence.append(row[1:])
        if last_id != -1 and last_id != current_id :
            sequences.append(np.array(current_sequence))
            current_sequence = []
        last_id = current_id
        
    if len(current_sequence) > 0:
        sequences.append(np.array(current_sequence))

    sequences = np.array(sequences)
    return sequences
    
    
def get_sequences_by_label_multi_dbs(label, dbs):
    
    seqs_all = []
    for db in dbs:
        seqs = get_sequences_by_label(label, db)
        seqs_all.extend(seqs)
    
    seqs_all = np.array(seqs_all)
    return seqs_all

    
def create_hmm_by_label(label):
    
    seqs = get_sequences_by_label(label)
    
    n_states = 3
    hmm = GaussianHMM(n_states, covariance_type="diag", n_iter=1000)
    hmm.fit([seqs])
    
    return hmm
    
    
def create_hmm_by_labels(labels, dbs):
    
    seqs_all= []
    for label in labels:
        seqs = get_sequences_by_label_multi_dbs(label, dbs)
        seqs_all.append(seqs)
    
    seqs_all = np.array(seqs_all)[0]
    
    #print seqs_all
    #print np.shape(seqs_all)

    n_states = 3
    hmm = GaussianHMM(n_states, covariance_type="full", n_iter=1000)
    hmm.fit(seqs_all)
    
    return hmm
    
    
    
def export_hmm(hmm, filename):
    
    n_states = len(hmm.startprob_)
    num_d = len(hmm.means_[0])
    
    export_str = '<?xml version="1.0" encoding="utf-8"?>\n<hmm num_d="%s" num_states="%s">\n\n' % (num_d, n_states)
    export_str += "<states>\n\n"
    for i in xrange(n_states):
        export_str += "<state>\n"
        export_str += "<pi>" + str(hmm.startprob_[i]) + "</pi>\n" 
        export_str += "<mean>" + ",".join([str(v) for v in hmm.means_[i]]) + "</mean>\n"
        export_str += "<covar>" + ",".join([str(v) for v in hmm.covars_[i].ravel()]) + "</covar>\n"
        #export_str += "<covar>" + str(hmm.covars_) + "<covar>"
       
        export_str += "</state>\n\n"
    export_str += "</states>\n\n"
    
    export_str += "<transitions>\n"
    for i in xrange(n_states):
        export_str += "<row>" + ",".join([str(v) for v in hmm.transmat_[i]]) + "</row>\n"
    export_str += "</transitions>\n\n"

    export_str += "</hmm>"
    
    f = open(filename, 'w+')
    f.write(export_str)
    f.close()
    
    with open(filename.split(".")[0] + ".pkl", 'w+') as output:
            pickle.dump(hmm, output, pickle.HIGHEST_PROTOCOL)
    
    
if __name__ == '__main__':
    
    
    path_database = "C:/Users/daniel/Desktop/test_db.db"   
    
    
    hmm = create_hmm_by_labels(["label"], [path_database])
    export_hmm(hmm, "models/model_1.xml")
    
    hmm = create_hmm_by_labels(["label2"], [path_database])
    export_hmm(hmm, "models/model_2.xml")
    
    
  
    