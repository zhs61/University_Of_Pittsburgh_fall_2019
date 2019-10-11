import sys
from nltk.tree import *


class Node:
    def __init__(self, key, left, right, prob, leaf, word):
        self.key = key
        self.left = left
        self.right = right
        self.prob = prob
        self.leaf = leaf
        self.word = word


"""
    Note: all probability store in the first element of the right side list
"""
g = []
l = []
grammar = {}
modified_grammar = {}
lexicon = {}
binary = []
jumped_prob = []
pass_count = 0


# read in file line by line, skip grammar and lexicon, store in list
def readFile(filename):
    getting_grammar = True

    for line in open(filename):
        line = line.strip('\n')
        # skip enpty line
        if line == '':
            break
        # skip the first line
        if line == 'Grammar':
            continue
        # start to get lexicon
        if line == 'Lexicon':
            getting_grammar = False
            continue
        if getting_grammar:
            g.append(line)
        else:
            l.append(line)


def find_correct_prob(key, word):
    for j in jumped_prob:
        if j[0] == key and j[1] == word:
            return j[2]


def cky(words):
    # table = [[[] for i in range(len(words))] for j in range(len(words))]
    rtable = [[[] for i in range(len(words))] for j in range(len(words))]
    for i in range(len(words)):
        word = words[i]
        for key in lexicon:
            for v in lexicon.get(key):
                if v[0] == word:
                    tu = [key, v[1]]
                    n = Node(key, None, None, float(v[1]), True, word)
                    rtable[i][i].append(n)
                    # table[i][i].append(tu)
        for key in modified_grammar:
            for v in modified_grammar.get(key):
                if isinstance(v[0], list):
                    for ele in v:
                        for e in ele:
                            if e[0] == word:
                                tu = [key, e[1]]
                                pro = find_correct_prob(key, word)
                                n = Node(key, None, None, float(pro), True, word)
                                rtable[i][i].append(n)
                                # table[i][i].append(tu)
        for j in reversed(range(0, i)):
            for k in range(j, i):
                for g_key in modified_grammar:
                    for r in modified_grammar.get(g_key):
                        if len(r) == 3:
                            B = r[0]
                            C = r[1]

                            # compare the factors from the left  with the factor from the bottom
                            # see if there is any conbaantion could satisfy new rule
                            # for r1 in table[j][k]:
                            #     r1 = r1[0]
                            #     for r2 in table[k + 1][i]:
                            #         r2 = r2[0]
                            #         if B == r1 and C == r2:
                            #             tu = [g_key, r[2]]
                            #             table[j][i].append(tu)
                            for left_child in rtable[j][k]:
                                for right_child in rtable[k + 1][i]:
                                    if left_child.key == B and right_child.key == C:
                                        n = Node(g_key, left_child, right_child,
                                                 left_child.prob * right_child.prob * float(r[2]), False, "")
                                        rtable[j][i].append(n)
    return rtable


# read the lexicon from l
def get_lexicon():
    for line in l:
        line = line.split(" ", 1)
        right_side = []
        prob = line[0]
        the_lex = line[1]
        the_lex = the_lex.split("->")
        key = the_lex[0]
        value = the_lex[1]
        value = value.lower()
        right_side.append(value)
        right_side.append(prob)
        exist = lexicon.get(key, [])
        exist.append(right_side)
        lexicon[key] = exist


# ==============================================================
def get_grammar():
    original = {}
    for line in g:
        line = line.split(" ", 1)
        prob = line[0]
        line = line[1]
        line = line.split("->")
        key = line[0]
        line = line[1].split(" ")
        value = original.get(key, [])
        value.append(line)
        line.append(prob)
        original[key] = value
    return original


removed_key = []  # store the removed key


def binarization_grammar():
    # convert teminals in rule to dummy non terminals
    temp = []
    for key in modified_grammar:
        value = modified_grammar.get(key)
        for v in value:
            for element in v[:len(v) - 1]:
                for word in lexicon:
                    l = lexicon.get(word)
                    for i in l:
                        if element == i[0]:
                            dummy = element.upper()
                            temp.append([dummy, element, 1])
    for i in temp:
        modified_grammar[i[0]] = [i[1], i[2]]

    terms = []
    p = 0.0
    # convert Unit production
    for key in modified_grammar:
        record = []

        value = modified_grammar.get(key)
        for v in value:
            if len(v) == 2:
                t = []
                s = []
                removed_key_ele = []
                t.append(key)
                removed_key_ele.append(key)
                single_non_terminal = v[0]
                removed_key_ele.append(single_non_terminal)
                p = float(v[1])
                if single_non_terminal in modified_grammar:
                    secondaryvalue = modified_grammar[single_non_terminal]
                    for se in secondaryvalue:
                        if len(se) > 2:
                            s.append(key)
                            s = s + (se)
                            terms.append(s)
                            s = []
                single_non_terminal, po = helper_find_unit(single_non_terminal, 1.0, removed_key_ele)
                p *= po
                if single_non_terminal in lexicon:
                    for voi in lexicon.get(single_non_terminal):
                        x = [key, voi[0], p * float(voi[1])]
                        jumped_prob.append(x)
                    t.append(lexicon.get(single_non_terminal))
                terms.append(t)
                removed_key.append(removed_key_ele)
                record.append(v)
        for v in record:
            value.remove(v)
    for t in terms:
        key = t[0]
        ty = modified_grammar.get(key, [])
        ty.append(t[1:])
        modified_grammar[key] = ty

    # convert to binary
    num = 1
    sub_list = []
    total_list = []
    for key in modified_grammar:
        value = modified_grammar.get(key)
        # for v in value:
        for h in range(len(value)):
            v = value[h]
            if len(v) > 3:
                vt = []
                sub_list = []
                for i in range(len(v) - 1):
                    sub_list.append(v[i])
                    # when ever we have two elements in sub_list,
                    # we can add it to the right side with a unique name
                    if len(sub_list) == 2:
                        if sub_list in binary:
                            e = binary.index(sub_list) + 1
                            vt = vt + [("X" + str(e))]
                        else:
                            binary.append(sub_list)
                            unique_id = 'X' + str(num)
                            num += 1
                            g = [unique_id]
                            vt = vt + g
                            t = [key, unique_id, sub_list[0], sub_list[1]] + ['1.00']
                            total_list.append(t)
                        sub_list = []
                    elif i == len(v) - 2:
                        vt = vt + sub_list + [v[i + 1]]
                value[h] = vt
    for u in total_list:
        modified_grammar[u[1]] = [u[2:]]


single_prob = []


def helper_find_unit(single_non_terminal, po, removed_key_ele):
    if single_non_terminal in modified_grammar:
        value = modified_grammar.get(single_non_terminal)
        for v in value:
            if len(v) == 2:
                single_non_terminal = v[0]
                removed_key_ele.append(single_non_terminal)
                po *= float(v[1])
                helper_find_unit(single_non_terminal, po, removed_key_ele)
                single_prob.append(po)
        return single_non_terminal, po
    return single_non_terminal, po


def help_find_mid(node):
    line = '[' + node.key + ' '
    for key in lexicon:
        for value in lexicon.get(key):
            if node.word in value:
                if node.key == key:
                    return '[' + node.key + ' ' + node.word + ']'
                else:
                    for item in original_grammar:
                        for v in original_grammar.get(item):
                            if len(v) == 2 and v[0] == key:
                                line += '[' + v[0] + ' '

    line += node.word + ']]'
    return line


def real_grammar(node):
    exist = False
    if node.key in original_grammar and node.left != None and node.right != None:
        value = original_grammar.get(node.key)
        for v in value:
            temp = [node.left.key, node.right.key]
            if temp[0] == v[0] and temp[1] == v[1]:
                exist = True
        return exist
    return True


def convert_X(x, node):
    if x == 'left':
        return [node.left.left.key, node.left.right.key]
    else:
        return [node.right.left.key, node.right.right.key]


def find_mid_2(node):
    p = 0
    temp = []
    for key in original_grammar:
        for v in original_grammar.get(key):
            if (node.left.key.startswith('X') or node.right.key.startswith('X')):
                if node.left.key.startswith('X'):
                    temp += convert_X('left', node)
                else:
                    temp += [node.left.key]
                if node.right.key.startswith('X'):
                    temp += convert_X('right', node)
                else:
                    temp += [node.right.key]
            else:
                temp = [node.left.key, node.right.key]
            if temp == v[:len(v) - 1]:
                p += 1
                if key != node.key:
                    return '[' + key + ' ', p
            temp = []
    return '', 0


def printTree(node):
    l1 = ''
    pass_c = 0
    if not real_grammar(node):
        l1, pass_c = find_mid_2(node)
    if node.leaf:
        return help_find_mid(node)
    else:
        left = ''
        right = ''
        if node.left != None:
            left = printTree(node.left)
        if node.right != None:
            right = printTree(node.right)
        if node.key.startswith('X'):
            return ' ' + left + ' ' + right
        line = '[' + node.key + l1 + left + ' ' + right + ']'
        for i in range(pass_c):
            line += ']'
        return line


def gernateTree(sentence):
    sentence = sentence.replace('[', '(')
    sentence = sentence.replace(']', ')')
    return Tree.fromstring(sentence.lower())


def getCount(sentence):
    gold_standard_count = []
    for s in sentence.subtrees():
        terminal = False
        for key in lexicon:
            if key.lower() == s.label():
                terminal = True
        if not terminal:
            gold_standard_count.append([s.label().upper(), s.leaves()])
    return gold_standard_count


def calculate_recall(gs_count, ss_count):
    correct_count = 0
    for c1 in ss_count:
        for c2 in gs_count:
            if c1[0] == c2[0]:
                v1 = c1[1]
                v2 = c2[1]
                if v1 == v2:
                    correct_count += 1
    return correct_count / len(gs_count)


def calculate_percision(gs_count, ss_count):
    correct_count = 0
    for c1 in ss_count:
        for c2 in gs_count:
            if c1[0] == c2[0]:
                v1 = c1[1]
                v2 = c2[1]
                if v1 == v2:
                    correct_count += 1
    return correct_count / len(ss_count)


if __name__ == '__main__':
    if len(sys.argv) != 4:
        sys.exit('Wrong number of command line arguments.')
    else:
        # read the grammar and lexicon
        readFile(sys.argv[1])
        original_grammar = get_grammar()
        modified_grammar = get_grammar()
        get_lexicon()
        binarization_grammar()

        words = sys.argv[2]
        words = words.lower()
        # print(words)
        words = words.split(" ")
        rtable = cky(words)
        result = []

        gold_standard = gernateTree(sys.argv[3])
        gs_count = getCount(gold_standard)
        for s in rtable[0][len(words) - 1]:
            if s.key == 'S':
                result.append([printTree(s), s.prob])
        total_prob = 1.0
        if len(result) == 0:
            print('Sentence rejected')
        else:
            print('Sentence accepted')
            for r in result:
                total_prob *= float(r[1])
            print('Sentence Probability: ', total_prob)
            for r in result:
                print(r[0])
                print(r[1])
                sentence_standard = gernateTree(r[0].lower())
                ss_count = getCount(sentence_standard)
                print('recall: ', calculate_recall(gs_count, ss_count))
                print('precision: ', calculate_percision(gs_count, ss_count))
s