# misplaced tile hueristic
def h_function(state,target_state):
    result = 0
    for i in range(0,9,1):
        if (state[i] !=0 ) and (state[i] != target_state[i]):
            result += 1
    return result
