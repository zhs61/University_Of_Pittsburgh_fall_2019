import pandas as pd
commend_tox = {}


##https://medium.com/analytics-vidhya/a-guide-to-machine-learning-in-r-for-beginners-part-5-4c00f2366b90
def read_file():
    excel_file = 'SFUcorpus.xlsx'
    corpus = pd.read_excel(excel_file, converters={'comment_text':str, 'toxicity_level':str})
    list_comment_text = corpus['comment_text']
    list_toxi = corpus['toxicity_level']
    for i in range(list_comment_text.size):
        key = list_comment_text[i]
        val = list_toxi[i][0]
        commend_tox[key] = val


def main():
    read_file()


if __name__=='__main__':
    main()


