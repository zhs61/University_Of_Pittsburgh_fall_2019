from LogReg import *
from LogReg_data_load import *

if __name__=='__main__':
    w_train = LogReg_online_GD(x_train, y_train, 1500)

    # Part b: print the weight
    print(w_train)

    # part c test
    for z in range(30):
        print(LogReg_classify(w_train, x_test[z]))
    for z in range(30):
        print(y_test[z][0])

    # part d:
    train_output = []
    for i in range(len(x_train)):
        train_output.append(LogReg_classify(w_train, x_train[i]))
    print(mis_error(train_output, y_train))

    test_output = []
    for i in range(len(x_test)):
        test_output.append(LogReg_classify(w_train, x_test[i]))
    print(mis_error(test_output, y_test))

    # part e
    train_miscal = 0
    test_miscal = 0
    for j in range(20):
        w_train = LogReg_online_GD(x_train, y_train, 1500)
        tr_output = []
        for i in range(len(x_train)):
            tr_output.append(LogReg_classify(w_train, x_train[i]))
        train_miscal += mis_error(tr_output, y_train)

        ts_output = []
        for i in range(len(x_test)):
            ts_output.append(LogReg_classify(w_train, x_test[i]))
        test_miscal += mis_error(ts_output, y_test)
    print(train_miscal/20.0)
    print(test_miscal / 20.0)

