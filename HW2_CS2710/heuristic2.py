# Manhattan distance hueristic
def h_function(state,target_state):
    result = 0
    for i in range(0,9,1):
        c = state[i]
        if c==0:
            continue
        c_x = i % 3
        c_y = int(i / 3)
        target_x, target_y = find_target_position(c, target_state)
        result += abs(c_x-target_x) + abs(c_y-target_y)
    return result

def find_target_position(c, target_state):
    for i in range(0,9,1):
        if c == target_state[i]:
            return (i % 3), int(i / 3)


