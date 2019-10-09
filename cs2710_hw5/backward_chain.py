from Propositional_KB_agent import *

def backwardchain(KB, theorem):
    print("Theorem: ", theorem)
    if not KB.is_in_FB(theorem):
        for r in KB.RB:
            if r.then_part == theorem:
                print("rule: ", r.name)
                #check antecedents
                in_cond = True
                for cond in r.cond_part:
                    if not KB.is_in_FB(cond):
                        new_theorem = cond
                        in_cond = backwardchain(KB, new_theorem)
                if in_cond:
                    print("success to prove")
                    print(r.then_part)
                    KB.add_fact(r.then_part)
                    return True
                else:
                    print("failure to prove")
        return False
    return True

if __name__ == '__main__':
    print("Theorem 1:")
    print("Prove the theorem: ", backwardchain(KBase, theorem1))
    print("FB size: ", len(KBase.FB))
    print()
    KBase.reset_FB(init_FB)
    print("Theorem 2:")
    print("Prove the theorem: ", backwardchain(KBase, theorem2))
    print("FB size: ", len(KBase.FB))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 3:")
    print("Prove the theorem: ", backwardchain(KBase, theorem3))
    print("FB size: ", len(KBase.FB))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 4:")
    print("Prove the theorem: ", backwardchain(KBase, theorem4))
    print("FB size: ", len(KBase.FB))
    KBase.reset_FB(init_FB)
    print()
    print("Theorem 5:")
    print("Prove the theorem: ", backwardchain(KBase, theorem5))
    print("FB size: ", len(KBase.FB))