from Propositional_KB_agent import *

def forwardchain(KB, theorem):
    for r in KB.RB:
        if not KB.is_in_FB(r.then_part):
            cond_in_fact = True
            for cond in r.cond_part:
                if not KB.is_in_FB(cond):
                    cond_in_fact = False
            if cond_in_fact:
                KB.add_fact(r.then_part)
                KB.add_rule(r)
                print(r.name)
                print(r.then_part)
            if KB.is_in_FB(theorem):
                KB.print_FB()
                return True
    KB.print_FB()
    return False

if __name__ == '__main__':
    print("Theorem 1:")
    print(forwardchain(KBase, theorem1))
    print()
    KBase.reset_FB(init_FB)
    print("Theorem 2:")
    print(forwardchain(KBase, theorem2))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 3:")
    print(forwardchain(KBase, theorem3))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 4:")
    print(forwardchain(KBase, theorem4))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 5:")
    print(forwardchain(KBase, theorem5))