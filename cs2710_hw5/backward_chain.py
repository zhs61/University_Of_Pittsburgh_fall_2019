from Propositional_KB_agent import *

def backwardchain(KB, theorem):
    if not KB.is_in_FB(theorem):
        for r in KB.RB:
            if r.then_part == theorem:
                #check antecedents
                in_cond = True
                for cond in r.cond_part:
                    if not KB.is_in_FB(cond):
                        new_theorem = cond
                        in_cond = backwardchain(KB, new_theorem)
                if in_cond:
                    KB.add_fact(r.then_part)
                    return True
        return False
    return True
if __name__ == '__main__':
    print("Theorem 1:")
    print(backwardchain(KBase, theorem1))
    print()
    KBase.reset_FB(init_FB)
    print("Theorem 2:")
    print(backwardchain(KBase, theorem2))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 3:")
    print(backwardchain(KBase, theorem3))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 4:")
    print(backwardchain(KBase, theorem4))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 5:")
    print(backwardchain(KBase, theorem5))