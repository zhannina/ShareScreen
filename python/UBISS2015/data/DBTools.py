# -*- coding: utf-8 -*-
"""
Created on Thu Jun 11 12:17:45 2015

@author: daniel
"""
import sqlite3
import os
import numpy as np



class SimpleDBHandler(object):
    
    
    def __init__(self, path_database):

        self.dbFile = os.path.join(os.path.dirname(__file__), path_database)
        
        'Create or connect to local db (just a file):'
        self.connection = sqlite3.connect(self.dbFile)   
            
        'Fetch cursor to work with db:'
        self.cursor = self.connection.cursor()
   



def load_data_by_label(label, path_database):
    
    db = SimpleDBHandler(path_database)
    db.cursor.execute("""SELECT s.fft
            FROM sequences seqs, sensor_data s WHERE seqs.id=s.seq_id AND
            seqs.label = '%s' ORDER BY s.seq_id ASC, s.id ASC""" % (label))
    
    data = []
    for row in db.cursor:
        d_dims = row[0].split(";")
        d = []
        for d_dim in d_dims:
            d += [float(v) for v in d_dim.split(",")[1:]]
        data.append(d)
      
    data = np.array(data)
    return data
    
    


if __name__ == '__main__':
        
    print "started"
    data = load_data_by_label("right", "ubiss_db.db")
    print data
    print "shape:", np.shape(data)