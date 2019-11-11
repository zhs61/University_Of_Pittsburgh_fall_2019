1. Runtime Environment:
	Python 3.7.
2. File:
	HW3.py					# main program
	GoogleNews-vectors-negative300.bin	# pretrained word2vecmodel
	SFUcorpus.xlsx				# input dataset
# All three files need to put in the same folder to make the program executable


3. Package:

import re 					# manipulate String
import gensim 					# handle word2vec
import nltk 					# Handle stop words
import numpy as np 				# handle calculation
from nltk.stem.porter import PorterStemmer	# handle warning
import warnings
warnings.filterwarnings('ignore')
stemmer = PorterStemmer()
import pandas as pd				# handle read file
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split, KFold
from warnings import simplefilter
simplefilter(action='ignore', category=FutureWarning)
from sklearn import metrics
import timeit					# handle timer


4. The Google Word2Vec model saves in the GoogleNews-vectors-negative300.bin file
Due to the size of the file, The course web might not accept it. Here is the link to download just in case:

https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit

5. REference:
##https://medium.com/analytics-vidhya/a-guide-to-machine-learning-in-r-for-beginners-part-5-4c00f2366b90
# https://www.youtube.com/watch?v=zM4VZR0px8E
# https://www.datacamp.com/community/tutorials/understanding-logistic-regression-python
# bag of word:
#    https://medium.com/greyatom/an-introduction-to-bag-of-words-in-nlp-ac967d43b428
# precission and recall
#https://simonhessner.de/why-are-precision-recall-and-f1-score-equal-when-using-micro-averaging-in-a-multi-class-problem/


