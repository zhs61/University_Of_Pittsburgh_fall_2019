import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score

commend_tox = {}
punctuations = '''!()-[]{};:'"\,<>./?@#$%^&*_~'''
list_of_words = {}
bag_of_word = {}
##https://medium.com/analytics-vidhya/a-guide-to-machine-learning-in-r-for-beginners-part-5-4c00f2366b90
# https://www.youtube.com/watch?v=zM4VZR0px8E
# https://www.datacamp.com/community/tutorials/understanding-logistic-regression-python
# bag of word:
#    https://medium.com/greyatom/an-introduction-to-bag-of-words-in-nlp-ac967d43b428
def read_file():
    excel_file = 'SFUcorpus.xlsx'
    corpus = pd.read_excel(excel_file, dtype={'comment_text': str, 'toxicity_level': str})
    list_comment_text = corpus['comment_text']
    list_toxi = corpus['toxicity_level']
    for i in range(list_comment_text.size):
        key = list_comment_text[i]
        val = list_toxi[i][0]
        commend_tox[key] = val
    return list_comment_text, list_toxi


def remove_punctuation(features):
    result = []
    for sentence in features:
        no_punct = ""
        for char in sentence:
            if char not in punctuations:
                no_punct = no_punct + char
        result.append(no_punct)
    return result


def get_list_of_word(no_punc_features):
    for sentence in no_punc_features:
        words = sentence.split(" ")
        for word in words:
            if word not in list_of_words:
                list_of_words[word] = 0


def bag_of_word_preprocess(no_punc_features):
    for sentence in no_punc_features:
        words = sentence.split(" ")
        vector = list_of_words.copy()
        for word in words:
            vector[word] = vector[word]+1
        bag_of_word[sentence] = vector



def main():
    features, toxi = read_file()
    no_punc_features = remove_punctuation(features)
    # vectorizer = CountVectorizer()
    # X = vectorizer.fit_transform(no_punc_features).toarray()
    # tfidfconverter = TfidfTransformer()
    # X = tfidfconverter.fit_transform(X).toarray()
    X_train, X_test, y_train, y_test = train_test_split(no_punc_features, toxi, test_size=0.2, random_state=0)
    vectorizer = CountVectorizer(ngram_range=(1, 2), max_features = 10000)
    train_data_features = vectorizer.fit_transform(no_punc_features)
    train_data_features = train_data_features.toarray()
    tfidf = TfidfTransformer()
    tfidf_features = tfidf.fit_transform(train_data_features).toarray()
    vocab = vectorizer.get_feature_names()
    ml_model = LogisticRegression(C=100, random_state=0)
    ml_model.fit(tfidf_features, y_train)

    test_data_features = vectorizer.transform(X_test)
    # Convert to numpy array
    test_data_features = test_data_features.toarray()
    test_data_tfidf_features = tfidf.fit_transform(test_data_features)
    # Convert to numpy array
    test_data_tfidf_features = test_data_tfidf_features.toarray()

    predicted_y = ml_model.predict(test_data_tfidf_features)
    correctly_identified_y = predicted_y == y_test
    accuracy = np.mean(correctly_identified_y) * 100
    print(accuracy)



    #get_list_of_word(no_punc_features)
    #bag_of_word_preprocess(no_punc_features)
    # for x in no_punc_features:
    #     print(x)


if __name__ == '__main__':
    main()
