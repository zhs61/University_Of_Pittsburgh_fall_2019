import math
import random
import numpy as np

from LogReg_data_load import x_train, y_train, x_test, y_test


def LogReg_probability(w, x):
    sum = 0
    w0 = w[0]
    for i in range(len(x)):
        sum = sum + w[i+1] * x[i]
    result = 1 / (1 + math.exp(-(w0 + sum)))
    return float(result)

def LogReg_online_GD(x_train, y_train, num_iterations):
    w = [0 for i in range(len(x_train[0])+1)]
    for i in range(num_iterations):
        learning_rate = 1 / (2 * math.sqrt(i+1))
        index = random.randrange(0, len(x_train))
        row = x_train[index]
        label = y_train[index][0]
        prob = (label-LogReg_probability(w, row))
        for j in range(len(row)):
            w[j+1] = w[j+1] + learning_rate * prob * row[j]
    return w

def LogReg_classify(w, x):
    prob = LogReg_probability(w, x)
    #print(prob)
    if prob >= 0.5:
        return 1
    else:
        return 0


def mis_error(predictedlabels, truelabels):
    misclassified = 0
    for i in range(len(predictedlabels)):
        if predictedlabels[i] != truelabels[i][0]:
            misclassified += 1
    return misclassified * 1.0 / len(predictedlabels)


