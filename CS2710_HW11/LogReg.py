import numpy as np


def LogReg_probability(w, x):
    sum = 0
    w0 = w[0]
    for i in range(len(x)):
        sum = sum + w[i+1] * x[i]
    return 1 / (1 + np.exp(-(w0 + sum)))

def LogReg_online_GD(x_train, y_train, num_iterations):
    w = [0] * len(x_train)
    for i in num_iterations:
        sum = 0
        for row in x_train:
            diff = LogReg_probability
            error = row[-1] - diff
            sum += error ** 2
            w[0] = w[0] + y_train * error * diff * (1.0 - diff)

