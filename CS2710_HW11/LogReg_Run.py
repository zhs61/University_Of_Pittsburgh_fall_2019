from LogReg import *
from LogReg_data_load import *

if __name__=='__main__':
    w_train = LogReg_online_GD(x_train, y_train, 1500)
    w_test = LogReg_online_GD(x_test, y_test, 1500)
    train_miscal = 0
    for j in range(20):
        output = []
        for i in range(len(x_train)):
            output.append(LogReg_classify(w_train, x_train[i]))
        train_miscal += mis_error(output, y_train)
    print(train_miscal/20)

    test_miscal = 0
    for j in range(20):
        output = []
        for i in range(len(x_test)):
            output.append(LogReg_classify(w_test, x_test[i]))
        test_miscal += mis_error(output, y_test)
    print(test_miscal / 20.0)
