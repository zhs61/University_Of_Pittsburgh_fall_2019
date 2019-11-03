import gensim
import nltk
import numpy as np
from nltk.stem.porter import PorterStemmer
stemmer = PorterStemmer()
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer, TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split, KFold
from pyspark.mllib.linalg import SparseVector, DenseVector
from warnings import simplefilter
simplefilter(action='ignore', category=FutureWarning)
from gensim.models import Word2Vec

#===================

class MyTokenizer:
    def __init__(self):
        pass

    def fit(self, X, y=None):
        return self

    def transform(self, X):
        transformed_X = []
        for document in X:
            tokenized_doc = []
            for sent in nltk.sent_tokenize(document):
                tokenized_doc += nltk.word_tokenize(sent)
            transformed_X.append(np.array(tokenized_doc))
        return np.array(transformed_X)

    def fit_transform(self, X, y=None):
        return self.transform(X)


class MeanEmbeddingVectorizer(object):
    def __init__(self, word2vec):
        self.word2vec = word2vec
        # if a text is empty we should return a vector of zeros
        # with the same dimensionality as all the other vectors
        self.dim = len(word2vec.wv.syn0[0])

    def fit(self, X, y=None):
        return self

    def transform(self, X):
        X = MyTokenizer().fit_transform(X)

        return np.array([
            np.mean([self.word2vec.wv[w] for w in words if w in self.word2vec.wv]
                    or [np.zeros(self.dim)], axis=0)
            for words in X
        ])

    def fit_transform(self, X, y=None):
        return self.transform(X)

#=========================
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

def get_bow(no_punc_features):
    cvectorizer = CountVectorizer()
    bow_noarray = cvectorizer.fit_transform(no_punc_features)
    bow = bow_noarray.toarray()
    return bow


def get_tf_idf(no_punc_features):
    tfidf_vectorizer = TfidfVectorizer(use_idf=True)
    tfidf_vectorizer_vectors = tfidf_vectorizer.fit_transform(no_punc_features)
    tfidf = tfidf_vectorizer_vectors.toarray()
    return tfidf

def cross_validation(text_representation, toxi, train_size, test_size):
    #X_train, X_test, y_train, y_test = train_test_split(text_representation, toxi, train_size=train_size, test_size=test_size, random_state=0)
    score = []
    text_representation = np.array(text_representation)
    toxi = np.array(toxi)
    kf = KFold(n_splits=10, shuffle=True)
    for train_index, test_index in kf.split(text_representation):
        X_train, X_test, y_train, y_test = text_representation[train_index], text_representation[test_index], toxi[train_index], toxi[test_index]
        classifier = get_classifier(X_train, y_train)
        score.append(classifier.score(X_test, y_test))
    return score


def get_classifier(X_train, y_train):
    classifier = LogisticRegression(solver='lbfgs', multi_class='auto', max_iter=1500)
    classifier.fit(X_train, y_train)
    return classifier


def main():
    features, toxi = read_file()
    no_punc_features = remove_punctuation(features)
    bow = get_bow(no_punc_features)
    score = cross_validation(bow, toxi, 0.8, 0.2)
    print(np.mean(score))

    tfidf = get_tf_idf(no_punc_features)
    score = cross_validation(tfidf, toxi, 0.8, 0.2)
    print(np.mean(score))

    # all_words = [nltk.word_tokenize(sent) for sent in no_punc_features]
    # word2vec = Word2Vec(no_punc_features, min_count=2, workers=4)
    # max_dataset_size = len(word2vec.wv.syn0)
    # #score = cross_validation(word2vec.wv.syn0, toxi[:max_dataset_size], 0.8, 0.2)
    # X_train, X_test, y_train, y_test = train_test_split(word2vec.wv, toxi[:max_dataset_size], train_size=0.8, test_size=0.2, random_state=0)
    # classifier = get_classifier(X_train, y_train)
    # score = classifier.score(X_test, y_test)
    # print(np.mean(score))
    model = gensim.models.Word2Vec(no_punc_features, workers=4)
    mean_embedding_vectorizer = MeanEmbeddingVectorizer(model)
    mean_embedded = mean_embedding_vectorizer.fit_transform(no_punc_features)
    #X_train, X_test, y_train, y_test = train_test_split(mean_embedded, toxi, train_size=0.8,test_size=0.2, random_state=0)
    score = cross_validation(mean_embedded, toxi, 0.8, 0.2)
    print(np.mean(score))


if __name__ == '__main__':
    main()


