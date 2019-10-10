import sys


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


# check the text with the grammar, binarization if needed, store in grammar dict
# def binarization():
#     num = 1
#     for line in g:
#         line = line.split(" ", 1)
#         right_side = []
#         # line[0] is probability, line[1] is grammar
#         prob = line[0]
#         # first element of right side is the prob
#         right_side.append(prob)
#         the_grammar = line[1]
#         the_grammar = the_grammar.split("->")
#         # now the_grammar[0] is the key, and the second is next
#         key = the_grammar[0]
#         value = the_grammar[1].split(" ")
#
#         # now if there are 1 or 2 values on the left side, no needed to do binarization
#         if len(value) == 1:
#             right_side.append(value[0])
#         elif len(value) == 2:
#             right_side.append(value[0])
#             right_side.append(value[1])
#         else: #the rest of the values will have more than 2 elements, so do binarization
#             # since the length is greater than 2, we need to form a sub list
#             sub_list = []
#             for i in range(len(value)):
#                 sub_list.append(value[i])
#                 # when ever we have two elements in sub_list,
#                 # we can add it to the right side with a unique name
#                 if len(sub_list) == 2:
#                     unique_id = 'id_' + str(num)
#                     num += 1
#                     grammar[unique_id] = sub_list
#                     right_side.append(unique_id)
#                     sub_list = []
#                 elif i == (len(value) - 1) : # when we get to the last element, add it to rightside
#                     right_side.append(value[i])
#         # now all the cases have been covered, next, store to the grammar dict
#         exist = grammar.get(key, [])
#         exist.append(right_side)
#         grammar[key] = exist


def cky(words):
    table = [[[] for i in range(len(words))] for j in range(len(words))]
    rtable = [[[] for i in range(len(words))] for j in range(len(words))]
    for i in range(len(words)):
        word = words[i]
        for key in lexicon:
            for v in lexicon.get(key):
                if v[0] == word:
                    tu = [key, v[1]]
                    n = Node(key, None, None, float(v[1]), True, word)
                    rtable[i][i].append(n)
                    table[i][i].append(tu)
        for key in modified_grammar:
            for v in modified_grammar.get(key):
                if isinstance(v[0],list):
                    for ele in v:
                        for e in ele:
                            if e[0] == word:
                                tu = [key, e[1]]
                                n = Node(key, None, None, float(e[1]), True, word)
                                rtable[i][i].append(n)
                                table[i][i].append(tu)
        for j in reversed(range(0, i)):
            for k in range(j , i):
                for g_key in modified_grammar:
                    for r in modified_grammar.get(g_key):
                        if len(r) == 3:
                            B = r[0]
                            C = r[1]

                            # compare the factors from the left  with the factor from the bottom
                            # see if there is any conbaantion could satisfy new rule
                            for r1 in table[j][k]:
                                r1 = r1[0]
                                for r2 in table[k+1][i]:
                                    r2 = r2[0]
                                    if B == r1 and C == r2:
                                        tu = [g_key, r[2]]
                                        table[j][i].append(tu)
                            for left_child in rtable[j][k]:
                                for right_child in rtable[k+1][i]:
                                    if left_child.key == B and right_child.key == C:
                                        n = Node(g_key, left_child, right_child, left_child.prob * right_child.prob * float(r[2]), False, "")
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
    original= {}
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


def binarization_grammar():
    # convert teminals in rule to dummy non terminals
    temp = []
    for key in modified_grammar:
        value = modified_grammar.get(key)
        for v in value:
            for element in v[:len(v)-1]:
                for word in lexicon:
                    l = lexicon.get(word)
                    for i in l:
                        if element == i[0]:
                            dummy = element.upper()
                            temp.append([dummy, element, 1])
    for i in temp:
        modified_grammar[i[0]] = [i[1], i[2]]

    terms = []
    p=0.0
    # convert Unit production
    for key in modified_grammar:
        record = []
        value = modified_grammar.get(key)
        for v in value:
            if len(v) == 2:
                t = []
                s = []
                t.append(key)
                single_non_terminal = v[0]
                p = float(v[1])
                if single_non_terminal in modified_grammar:
                    secondaryvalue = modified_grammar[single_non_terminal]
                    for se in secondaryvalue:
                        if len(se) > 2:
                            s.append(key)
                            s = s + (se)
                            terms.append(s)
                            s = []
                single_non_terminal, po = helper_find_unit(single_non_terminal, 1.0)
                p *= po
                if single_non_terminal in lexicon:
                    t.append(lexicon.get(single_non_terminal))
                terms.append(t)
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
                for i in range(len(v)-1):
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
                    elif i == len(v)-2:
                        vt = vt + sub_list + [v[i+1]]
                value[h] = vt
    for u in total_list:
        modified_grammar[u[1]] = [u[2:]]


single_prob = []
def helper_find_unit(single_non_terminal, po):
    if single_non_terminal in modified_grammar:
        value = modified_grammar.get(single_non_terminal)
        for v in value:
            if len(v) == 2:
                single_non_terminal = v[0]
                po *= float(v[1])
                helper_find_unit(single_non_terminal, po)
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

    line +=  node.word + ']]'
    return line


def printTree(node):
    id = ''
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
        return '[' + node.key + ' ' + left + ' ' + right + ']'


if __name__ == '__main__':
    if len(sys.argv) != 4:
        sys.exit('Wrong number of command line arguments.')
    else:
        #read the grammar and lexicon
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
        for s in rtable[0][len(words)-1]:
            if s.key == 'S':
                result.append([printTree(s),s.prob])
        max_prob = -1
        if len(result) == 0:
            print('Sentence rejected')
        else:
            for r in result:
                if r[1] > max_prob:
                    max_prob = r[1]
            print('Sentence accepted')
            for r in result:
                if r[1] == max_prob:
                    print(r[0])
                    print(r[1])
