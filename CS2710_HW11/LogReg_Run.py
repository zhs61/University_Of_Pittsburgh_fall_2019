from LogReg import *
from LogReg_data_load import *

if __name__=='__main__':
    w_train = LogReg_online_GD(x_train, y_train, 1500)

    # Part b: print the weight
    print(w_train)

    # part c test
    predictions = []
    for z in range(30):
        predictions.append(LogReg_classify(w_train, x_test[z], True))
    print("Predictions: ", predictions)
    print("Mis_error of first 30: ", mis_error(predictions, y_test))

    # part d:
    train_output = []
    for i in range(len(x_train)):
        train_output.append(LogReg_classify(w_train, x_train[i]))
    print("Mis_error of Train data: ", mis_error(train_output, y_train))

    test_output = []
    for i in range(len(x_test)):
        test_output.append(LogReg_classify(w_train, x_test[i]))
    print("Mis_error of Test data: ", mis_error(test_output, y_test))

    # part e
    train_miscal = 0
    test_miscal = 0
    for j in range(20):
        w_train = LogReg_online_GD(x_train, y_train, 1500)
        print("Weight learned: ", w_train)
        tr_output = []
        for i in range(len(x_train)):
            tr_output.append(LogReg_classify(w_train, x_train[i]))
        train_miscal += mis_error(tr_output, y_train)

        ts_output = []
        for i in range(len(x_test)):
            ts_output.append(LogReg_classify(w_train, x_test[i]))
        test_miscal += mis_error(ts_output, y_test)
    print("Mis_error of Train data with 20 iteration: ", train_miscal/20.0)
    print("Mis_error of Test data with 20 iteration: ", test_miscal / 20.0)

