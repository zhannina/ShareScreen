# -*- coding: utf-8 -*-
"""
Created on Thu Jun 11 16:02:45 2015

@author: daniel
"""
import numpy as np



   
class SVMParams(object):      
    def __init__(self, c1, c2, svs_c1, svs_c2, coefs_c1, coefs_c2, intercept):
        self.c1 = c1
        self.c2 = c2
        self.svs_c1 = svs_c1
        self.svs_c2 = svs_c2
        self.coefs_c1 = coefs_c1
        self.coefs_c2 = coefs_c2
        self.intercept = intercept
        
        
        
def getSVsAndCoefs(support_indices, svs, coefs, c):
    
    coefs_class = None
    svs_class = None
    if c == 0:
        coefs_class = coefs[0:support_indices[c]] 
        svs_class = svs[0:support_indices[c]]
    else:
        coefs_class = coefs[support_indices[c-1]:support_indices[c]] 
        svs_class = svs[support_indices[c-1]:support_indices[c]]   
        
    return svs_class, coefs_class
    
    

def getSVMParams(support_indices, svs, coefs, intercept, c1, c2):
    
    svs_c1, coefs_c1 = getSVsAndCoefs(support_indices, svs, coefs, c1)
    svs_c2, coefs_c2 = getSVsAndCoefs(support_indices, svs, coefs, c2) 
    
    idx_in_c1 = c2 if c2 < c1 else c2-1    
    idx_in_c2 = c1 if c1 < c2 else c1-1  
    coefs_c1 = coefs_c1[:, idx_in_c1]
    coefs_c2 = coefs_c2[:, idx_in_c2]
    
    svm_params = SVMParams(c1, c2, svs_c1, svs_c2, coefs_c1, coefs_c2, intercept)
    return svm_params
    
    
'''
Export the given trained svm object.
It is assumed to have used the createRBFKernel function as its "kernel" paramter,
with the given gamma. 
The training set (X) used to train this svm needs to be given as well,
to be able to extract the support vectors.
'''
def exportTrainedSVM(svc, X, gamma):
       
    support_indices = np.cumsum(svc.n_support_)
    classes = svc.classes_
    n_classes = len(classes)

    support_vectors =  X[svc.support_,:]  
   
    
    svms = []
    combination_count = 0
    for c1 in xrange(n_classes):     
        for c2 in xrange(n_classes):
            if c1<c2:
                svm_params = getSVMParams(support_indices, support_vectors, \
                svc.dual_coef_.T, svc.intercept_[combination_count], c1, c2)
                svms.append(svm_params)
                combination_count += 1
      
    
    'TEST export:'
    output_str = '{\n"n_classes" : ' \
    + str(n_classes) + ',\n"gamma" : ' + str(gamma) + ',\n"svms" : [\n'
    for i in xrange(len(svms)):
         svm = svms[i]
         output_str += '{\n'
         
         'encode class labels:'
         output_str += '"c1" : ' + str(svm.c1) + ',\n'
         output_str += '"c2" : ' + str(svm.c2) + ',\n'
         
         'encode support vectors class 1:'
         output_str += '"svs_c1" : ['
         for j in xrange(len(svm.svs_c1)):
             output_str += '[' 
             for k in xrange(len(svm.svs_c1[j])):
                 output_str += str(svm.svs_c1[j,k])
                 if k < len(svm.svs_c1[j])-1: output_str += ','
             output_str += ']'
             if j < len(svm.svs_c1)-1: output_str += ','
         output_str += '],\n'
         
         'encode support vectors class 2:'
         output_str += '"svs_c2" : ['
         for j in xrange(len(svm.svs_c2)):
             output_str += '[' 
             for k in xrange(len(svm.svs_c2[j])):
                 output_str += str(svm.svs_c2[j,k])
                 if k < len(svm.svs_c2[j])-1: output_str += ','
             output_str += ']'
             if j < len(svm.svs_c2)-1: output_str += ','
         output_str += '],\n'
         
         'encode dual coefs class 1:'
         output_str += '"coefs_c1" : ['
         for j in xrange(len(svm.coefs_c1)):
             output_str += str(svm.coefs_c1[j])
             if j < len(svm.coefs_c1)-1: output_str += ','
         output_str += '],\n'
         
         'encode dual coefs class 2:'
         output_str += '"coefs_c2" : ['
         for j in xrange(len(svm.coefs_c2)):
             output_str += str(svm.coefs_c2[j])
             if j < len(svm.coefs_c2)-1: output_str += ','
         output_str += '],\n'
         
         'encode intercept:'
         output_str += '"intercept" : ' + str(svm.intercept) + '\n'

         
         output_str += '}'
         
         if i < len(svms)-1: output_str += ','
         output_str += "\n"
         
    output_str += ']}'
    #print output_str
    f = open("export_svm.txt", "w+")
    f.write(output_str)
    f.close()
    
 