import re
import gensim
import nltk
import numpy as np
from nltk.stem.porter import PorterStemmer
import warnings
from scipy.stats import stats
warnings.filterwarnings('ignore')
stemmer = PorterStemmer()
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split, KFold
# from warnings import simplefilter
# simplefilter(action='ignore', category=FutureWarning)
from sklearn import metrics
import timeit

#===================
# dowload the google pretrained model:
#https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit
#
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
# precission and recall
#https://simonhessner.de/why-are-precision-recall-and-f1-score-equal-when-using-micro-averaging-in-a-multi-class-problem/
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

def remove_punctuation_num__stopword(features):
    result = []
    for sentence in features:
        no_punct = ""
        sentence = sentence.lower()
        for char in sentence:
            if char.isalpha() or char == ' ':
                no_punct = no_punct + char
        result.append(no_punct)
    result = [re.sub(r'\d+', 'num', X) for X in result]
    stopwords = set(nltk.corpus.stopwords.words('english') + ['reuter', '\x03'])
    result = [[word for word in article.split() if word not in stopwords] for article in result]
    stemmer = nltk.stem.PorterStemmer()
    result = [" ".join([stemmer.stem(word) for word in article]) for article in result]
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
    precision_macro = []
    recall_macro = []
    f1_macro = []
    text_representation = np.array(text_representation)
    toxi = np.array(toxi)
    kf = KFold(n_splits=10, shuffle=True)
    for train_index, test_index in kf.split(text_representation):
        X_train, X_test, y_train, y_test = text_representation[train_index], text_representation[test_index], toxi[train_index], toxi[test_index]
        classifier = get_classifier(X_train, y_train)
        score.append(classifier.score(X_test, y_test))
        y_pred = classifier.predict(X_test)
        precision_macro.append(metrics.precision_score(y_test, y_pred, average='macro'))
        recall_macro.append(metrics.recall_score(y_test, y_pred, average='macro'))
        f1_macro.append(metrics.f1_score(y_test, y_pred, average='macro'))
    return score, precision_macro, recall_macro, f1_macro


def get_classifier(X_train, y_train):
    classifier = LogisticRegression(solver='lbfgs', multi_class='auto', max_iter=1500)
    classifier.fit(X_train, y_train)
    return classifier


def main():
    start = timeit.default_timer()
    features, toxi = read_file()
    no_punc_features = remove_punctuation(features)
    #Pre processing method for step 5
    #no_punc_features = remove_punctuation_num__stopword(features)

    # Bag of words
    bow = get_bow(no_punc_features)
    score_bow, precision_bow, recall_bow, f1_bow = cross_validation(bow, toxi, 0.8, 0.2)
    print('Bag-of-word Score: ', np.mean(score_bow))
    print('Bag-of-word Precision: ', np.mean(precision_bow))
    print('Bag-of-word Recall: ', np.mean(recall_bow))
    print('Bag-of-word f1: ', np.mean(f1_bow))

    # majority voting
    temp_toxi = []
    for x in toxi:
        temp_toxi.append('1')
    print('Majority Voting: ', metrics.accuracy_score(toxi, temp_toxi))
    print('Majority Precision: ', metrics.precision_score(toxi, temp_toxi, average='macro'))
    print('Majority Recall: ', metrics.recall_score(toxi, temp_toxi, average='macro'))
    print('Majority f1: ', metrics.f1_score(toxi, temp_toxi, average='macro'))

    #tf-idf
    tfidf = get_tf_idf(no_punc_features)
    score_tf, precision_tf, recall_tf, f1_tf = cross_validation(tfidf, toxi, 0.8, 0.2)
    print('Tf-idf Score: ', np.mean(score_tf))
    print('Tf-idf Precision: ', np.mean(precision_tf))
    print('Tf-idf Recall: ', np.mean(recall_tf))
    print('Tf-idf f1: ', np.mean(f1_tf))

    #Word2vec
    #load the pretrained Google word2vec model
    model = gensim.models.KeyedVectors.load_word2vec_format('GoogleNews-vectors-negative300.bin', binary=True, limit=100000)
    mean_embedding_vectorizer = MeanEmbeddingVectorizer(model)
    mean_embedded = mean_embedding_vectorizer.fit_transform(no_punc_features)
    score_w2v, precision_w2v, recall_w2v, f1_w2v = cross_validation(mean_embedded, toxi, 0.8, 0.2)
    print('Word2vec Score: ', np.mean(score_w2v))
    print('Word2vec Precision: ', np.mean(precision_w2v))
    print('Word2vec Recall: ', np.mean(recall_w2v))
    print('Word2vec f1: ', np.mean(f1_w2v))
    stop = timeit.default_timer()
    print('Time: ', stop - start)

    print(stats.ttest_rel(score_tf, score_bow))
    print(stats.ttest_rel(score_w2v, score_bow))
    print(stats.ttest_rel(score_tf, score_w2v))



if __name__ == '__main__':
    main()


