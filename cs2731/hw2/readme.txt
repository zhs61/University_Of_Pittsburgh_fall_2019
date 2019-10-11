Python Version: 3.6

Command line arguments:

    argv[1] = grammar file
    argv[2] = "Test sentence"
    argv[3] = "Goald stnadard s-esppression"

Requirement:

    from nltk.tree import *:

    need to have nltk installed

Binarization:
    Follow the instruction on the text book:

        1. Copy all conforming rules to the new grammar unchanged.
        2. Convert terminals within rules to dummy non-terminals.
        3. Convert unit productions.
        4. Make all rules binary and add them to new grammar

    The binarization process in method "binarization_grammar()"