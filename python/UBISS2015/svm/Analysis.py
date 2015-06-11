# -*- coding: utf-8 -*-
"""
Created on Thu Jun 11 15:03:28 2015

@author: daniel
"""
from sklearn import svm
from sklearn import cross_validation as cv
from sklearn.cross_validation import KFold
from sklearn.cross_validation import StratifiedKFold
from sklearn.metrics import accuracy_score

import numpy as np
from data.DBTools import *
import matplotlib.pyplot as plt

import ExportSVM


if __name__ == '__main__':
       
    data_left = load_data_by_label("left", "ubiss_db6.db")
    data_right = load_data_by_label("right", "ubiss_db6.db")
    data_nothing = load_data_by_label("nothing", "ubiss_db6.db")
    
    data_all = np.vstack((data_left, data_right, data_nothing))

    targets = np.zeros(len(data_left) + len(data_right) + len(data_nothing))
    targets[0:len(data_left)] = 0
    targets[len(data_left):len(data_left)+len(data_right)] = 1
    targets[len(data_left)+len(data_right):] = 2
    

    print "data loaded:"
    print "data table shapes:", np.shape(data_left), np.shape(data_right), np.shape(data_nothing)
      
    clf = svm.SVC(kernel="linear", C=100)#, C=1000, gamma=1./200)
    

    print "cross validation:"
    #scores = cv.cross_val_score(clf, data_all, targets, cv=5, scoring="accuracy")
    #print("Accuracy: %0.2f (+/- %0.2f)" % (scores.mean(), scores.std() * 2))
    '''
    #kf = KFold(len(data_all), n_folds=5)
    skf = StratifiedKFold(targets, 5)
    scores = []
    for train, test in skf:
        training_data = data_all[train]
        targets_train = targets[train]
        testing_data = data_all[test]
        targets_test = targets[test]
        clf.fit(training_data, targets_train)
        preds = clf.predict(testing_data)
        score = accuracy_score(targets_test, preds)
        scores.append(score)
        print score
        
    print "accuracy:", np.mean(scores)
    '''
    clf_for_export = svm.SVC(kernel="linear", C=1000)#, C=1000, gamma=1/200)
    clf_for_export.fit(data_all, targets)
    print len(clf_for_export.support_)
    ExportSVM.exportTrainedSVM(clf_for_export, data_all, 1./200)
        
    
    plt.figure()
    plt.subplot(111)
    
    plt.plot(np.arange(len(data_left[0])), np.mean(data_left, axis=0), lw=2, label="vib from left")
    plt.plot(np.arange(len(data_right[0])), np.mean(data_right, axis=0), lw=2, label="vib from right")
    plt.plot(np.arange(len(data_nothing[0])), np.mean(data_nothing, axis=0), lw=2, label="no vibration")
    
    plt.legend(loc="upper left")
    plt.tight_layout()
    plt.show()