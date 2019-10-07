import sys
import string
import copy

#https://github.com/stensaethf/CKY-Parser/blob/master/parser.py

def readFile(filename):
    f = open(filename, 'r')
    lines = f.readlines()
    getting_Grammar = True
    grammar = {}
    lexicon = {}
    for line in lines:
        if line == "\n":
            break
        # remove the \n
        line = line.strip('\n')
        # skip the first line
        if line == 'Grammar':
            continue
        # start to get lexicon
        if line == 'Lexicon':
            getting_Grammar = False
            continue
        # getting the grammar
        if getting_Grammar:
            split_sent = line.split(" ", 1)
            g = split_sent[1].split("->")
            temp = {g[1]: split_sent[0]}
            if g[0] in grammar:
                grammar[g[0]] = {**grammar.get(g[0]), **temp}
            else:
                grammar[g[0]] = temp
        else:
            split_sent = line.split(" ", 1)
            g = split_sent[1].split("->")
            temp = {g[1].lower(): split_sent[0]}
            if g[0] in lexicon:
                lexicon[g[0]] = {**lexicon.get(g[0]), **temp}
            else:
                lexicon[g[0]] = temp
    return grammar, lexicon
    # print(grammar.items())

# iterative through the dictionary,
# if there are more than two terminals break it

def binarization(grammar):
    bi_grammar = copy.deepcopy(grammar)
    for non_termianl in grammar:
        for t in grammar[non_termianl]:
            g = t.split(" ")
            while len(g) > 2:
                non_t1 = g[len(g) - 1]
                non_t2 = g[len(g) - 2]
                temp = [non_t1, non_t2]
                g.remove(non_t2)
                g.remove(non_t1)
                g.append(temp)
                r = g
                r = str(r).replace('[', "")
                r = str(r).replace(']', "")
                bi_grammar[non_termianl][r] = bi_grammar[non_termianl].pop(t)
    return bi_grammar
class Node:
    def __init__(self, rule, child1, child2 = None):
        self.rule = rule
        self.child1 = child1
        self.child2 = child2

    def __repr__(self):
        return self.rule

def cky_parse(words, grammar, lexicon):
    if len(words) != 0:
        words = words.lower()
        sent = words.split(" ")
    table = [[[] for x in range(len(sent)+1)] for y in range(len(sent)+1)]

    # j = 1
    # for word in sent:
    #     for i in lexicon:
    #         tempr = lexicon[i].keys()
    #         if word in tempr:
    #             table[0][j].append(Node(i, word))
    #     j += 1
    for j in range(1, len(sent)+1):
        word = sent[j-1]
        for i in lexicon:
            tempr = lexicon[i].keys()
            if word in tempr:
                table[j-1][j].append(Node(i, word))
    print(table)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        sys.exit('Wrong number of command line arguments.')
    else:
        #read the grammar and lexicon
        grammar, lexicon = readFile(sys.argv[1])
        bi_grammar = binarization(grammar)
        cky_parse(sys.argv[2], grammar, lexicon)

