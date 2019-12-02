import math
import random
import numpy as np

from LogReg_data_load import x_train, y_train, x_test, y_test


def LogReg_probability(w, x):
    sum = 0
    for i in range(len(x)):
        sum = sum + w[i] * x[i]
    result = 1 / (1.0 + math.exp(-sum))
    return result

def LogReg_online_GD(x_train, y_train, num_iterations):
    w = [0 for i in range(len(x_train[0])+1)]
    for i in range(num_iterations):
        learning_rate = 1 / (2 * math.sqrt(i+1))
        index = random.randrange(0, len(x_train))
        row = x_train[index]
        row = np.insert(row, 0, 1)
        row = row.tolist()
        label = y_train[index]
        prob = (label[0]-LogReg_probability(w, row))
        for j in range(len(row)):
            w[j] = w[j] + learning_rate * prob * row[j]
    return w

def LogReg_classify(w, x):
    prob = LogReg_probability(w, x)
    if prob >= 0.5:
        return 1
    else:
        return 0


def mis_error(predictedlabels, truelabels):
    misclassified = 0
    for i in range(len(predictedlabels)):
        if predictedlabels[i] != truelabels[i][0]:
            misclassified += 1
    return misclassified / len(predictedlabels)


