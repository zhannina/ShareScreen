# -*- coding: utf-8 -*-
"""
Created on Wed Jun 10 17:47:55 2015

@author: daniel
"""
from sklearn import svm
import numpy as np


from svmutil import *


def test():
    
    
    #'''
    X = [[0, 0], [1, 1]] # features
    y = [0, 1] # labels
    clf = svm.SVC()
    clf.fit(X, y)  
    print clf
    #'''

    '''
    # Specify training set
    prob = svm_problem([1,-1],[[1,0,1],[-1,0,-1]])
    # Train the model
    m = svm_train(prob, '-t 0 -c 1')
    # Make a prediction
    predicted_labels, _, _ = svm_predict([-1],[[1,0,1]],m)
    # Predicted label for input [1,1,1] is predicted_labels[0]
    print "Predicted value: " + str(predicted_labels[0])
    '''
    
    '''
    m = svm_train([1,-1],[[1,0,1],[-1,0,-1]], '-t 0 -c 10')
    p_labels, p_acc, p_vals = svm_predict([1,-1],[[1,0,1],[-1,0,-1]], m)
    svm_save_model("exported_model.txt", m)
    '''
    
    
if __name__ == '__main__':
        
    print "started"
    test()