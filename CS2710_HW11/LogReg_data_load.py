#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Nov 21 23:52:56 2019

@author: milos
Loading data
"""

import pandas as pd
import numpy as np

# Load x part of the training data

# read x train matrix from the text file
x_train_aux=pd.read_csv("digit_x.txt",sep=' ',header=None)
# convert to numpy matrix
x_train=x_train_aux.values

# Load y part of the training data
y_train_aux=pd.read_csv("digit_y.txt",sep=' ',header=None)
# convert to numpy matrix
y_train=y_train_aux.values

# Load x part of the test data
x_test_aux=pd.read_csv("digit_x_test.txt",sep=' ',header=None)
# convert to numpy matrix
x_test=x_test_aux.values

# Load y part of the test data
y_test_aux=pd.read_csv("digit_y_test.txt",sep=' ',header=None)
# convert to numpy matrix
y_test=y_test_aux.values

