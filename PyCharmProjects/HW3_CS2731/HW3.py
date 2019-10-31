import re
import nltk
from sklearn.ensemble import VotingClassifier

nltk.download('punkt')
from nltk.tokenize import word_tokenize
nltk.download('stopwords')
from nltk.corpus import stopwords
from nltk.stem.porter import PorterStemmer
stemmer = PorterStemmer()
from autocorrect import spell
import numpy as np
import pandas as pd
import xlrd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split


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
    comment_text = []
    comment_tox = []
    for i in range(list_comment_text.size):
        comment_text.append(list_comment_text[i])
        comment_tox.append(list_toxi[i][0])
    return comment_text, comment_tox


def remove_punctuation(features):
    result = []
    for sentence in features:
        no_punct = ""
        sentence = sentence.lower()
        for char in sentence:
            if char.isalpha() or char == ' ':
                no_punct = no_punct + char
        result.append(no_punct)
    return result


def main():
    features, toxi = read_file()
    no_punc_features = remove_punctuation(features)
    vectorizer = CountVectorizer()
    bow = vectorizer.fit_transform(no_punc_features).toarray()
    X_train, X_test, y_train, y_test = train_test_split(bow, toxi, test_size=0.3, random_state=0)
    bow_classifier = LogisticRegression()
    bow_classifier.fit(X_train, y_train)
    y_pred = bow_classifier.predict(X_test)
    print(bow_classifier.score(X_test, y_test))


    # majority_classifier = VotingClassifier(estimators=[X_train, y_train])
    # majority_classifier.fit(X_train, y_train)
    # print(majority_classifier.score(X_test, y_test))

    #get_list_of_word(no_punc_features)
    #bag_of_word_preprocess(no_punc_features)
    # for x in no_punc_features:
    #     print(x)


if __name__ == '__main__':
    main()
