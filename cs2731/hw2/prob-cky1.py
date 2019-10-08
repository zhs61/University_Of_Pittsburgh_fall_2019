import sys

"""
    Note: all probability store in the first element of the right side list
"""
g = []
l = []
grammar = {}
modified_grammar = {}
lexicon = {}

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
def binarization():
    num = 1
    for line in g:
        line = line.split(" ", 1)
        right_side = []
        # line[0] is probability, line[1] is grammar
        prob = line[0]
        # first element of right side is the prob
        right_side.append(prob)
        the_grammar = line[1]
        the_grammar = the_grammar.split("->")
        # now the_grammar[0] is the key, and the second is next
        key = the_grammar[0]
        value = the_grammar[1].split(" ")

        # now if there are 1 or 2 values on the left side, no needed to do binarization
        if len(value) == 1:
            right_side.append(value[0])
        elif len(value) == 2:
            right_side.append(value[0])
            right_side.append(value[1])
        else: #the rest of the values will have more than 2 elements, so do binarization
            # since the length is greater than 2, we need to form a sub list
            sub_list = []
            for i in range(len(value)):
                sub_list.append(value[i])
                # when ever we have two elements in sub_list,
                # we can add it to the right side with a unique name
                if len(sub_list) == 2:
                    unique_id = 'id_' + str(num)
                    num += 1
                    grammar[unique_id] = sub_list
                    right_side.append(unique_id)
                    sub_list = []
                elif i == (len(value) - 1) : # when we get to the last element, add it to rightside
                    right_side.append(value[i])
        # now all the cases have been covered, next, store to the grammar dict
        exist = grammar.get(key, [])
        exist.append(right_side)
        grammar[key] = exist


def cky(words, grammar):
    n = len(words)
    table = {i: {j: {} for j in range(n+1)} for i in range(n+1)}
    for j in range(1, len(words)+1):
        word = words[j-1]
        for key in lexicon:
            value = lexicon.get(key)
            for v in value:
                if v[1] == word:
                    table[j-1][j][key] = v[0]
        for i in range(j-2, -1, -1):
            for k in range(i+1, j):
                for g_key in grammar:
                    rule = grammar.get(g_key)
                    for r in rule:
                        if len(r) == 3:
                            if (r[1] in table[i][k]) and (r[2] in table[k][j]):
                                if table[i][k][r[1]] > 0 and table[k][j][r[2]] > 0:
                                    print(r)


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
        right_side.append(value)
        right_side.append(prob)
        exist = lexicon.get(key, [])
        exist.append(right_side)
        lexicon[key] = exist
    #print(lexicon)
# ==============================================================
def get_grammar():
    original_grammar = {}
    for line in g:
        line = line.split(" ", 1)
        prob = line[0]
        line = line[1]
        line = line.split("->")
        key = line[0]
        line = line[1].split(" ")
        value = original_grammar.get(key, [])
        value.append(line)
        line.append(prob)
        original_grammar[key] = value
    return original_grammar

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
    # convert Unit production
    for key in modified_grammar:
        record = []
        value = modified_grammar.get(key)
        for v in value:
            if len(v) == 2:
                t = []
                t.append(key)
                single_non_terminal = v[0]
                single_non_terminal = helper_find_unit(single_non_terminal)
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
        for v in value:
            if len(v) > 3:
                sub_list = []
                for i in range(len(v)-1):
                    sub_list.append(v[i])
                    # when ever we have two elements in sub_list,
                    # we can add it to the right side with a unique name
                    if len(sub_list) == 2:
                        unique_id = 'X' + str(num)
                        num += 1
                        t = [key, unique_id, sub_list[0], sub_list[1]]
                        total_list.append(t)
                        sub_list = []
                    # elif i == len(v)-2:
                    #     total_list.append(sub_list)
    for l in total_list:
        modified_grammar[l[1]] = l[2:]
        modified_grammar[l[0]].remove(l[2])
        modified_grammar[l[0]].remove(l[3])

        # elif i == (len(value) - 1):  # when we get to the last element, add it to rightside




def helper_find_unit(single_non_terminal):

    if single_non_terminal in modified_grammar:
        value = modified_grammar.get(single_non_terminal)
        for v in value:
            if len(v) == 2:
                single_non_terminal = v[0]
                helper_find_unit(single_non_terminal)
        return single_non_terminal
    return single_non_terminal



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
        # binarization()

        words = sys.argv[2]
        words = words[0].lower() + words[1:]
        words = words.split(" ")
        #cky(words, modified_grammar)