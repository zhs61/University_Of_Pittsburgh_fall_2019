#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Sep  3 17:01:00 2019
implementation of the evaluation function driven search
@author: milos
"""

from Puzzle8 import *


#### ++++++++++++++++++++++++++++++++++++++++++++++++++++
#### evaluation function driven search


def eval_function_driven_search(problem):
    global numOfNodeGenerated
    global numOfNodeExpanded
    global maxLenOfQueue
    queue = Priority_Queue()
    root = TreeNode(problem, problem.initial_state)
    queue.add_to_queue(root)
    numOfNodeGenerated = 1
    numOfNodeExpanded = 0
    maxLenOfQueue = 1
    while (queue.is_empty() == False):
        # Find the max length of the queue
        if len(queue.queue) > maxLenOfQueue:
            maxLenOfQueue = len(queue.queue)
        next = queue.pop_queue()
        if next.goalp():
            del (queue)
            return next.path()
        else:
            new_nodes = next.generate_new_tree_nodes()
            numOfNodeExpanded += 1
            numOfNodeGenerated += len(new_nodes)
            for new_node in new_nodes:
                queue.add_to_queue(new_node)
    print('No solution')
    return NULL


problem = Puzzle8_Problem(Example1)
output = eval_function_driven_search(problem)
print('Example 1 Solution:')
print_path(output)
print('The number of nodes generated: ', numOfNodeGenerated)
print('The number of nodes expanted: ', numOfNodeExpanded)
print('The maximum length of the queue: ', maxLenOfQueue)
print('The length of solution: ', len(output))
print('\n')

problem = Puzzle8_Problem(Example2)
output = eval_function_driven_search(problem)
print('Example 2 Solution:')
print_path(output)
print('The number of nodes generated: ', numOfNodeGenerated)
print('The number of nodes expanted: ', numOfNodeExpanded)
print('The maximum length of the queue: ', maxLenOfQueue)
print('The length of solution: ', len(output))
print('\n')

problem = Puzzle8_Problem(Example3)
output = eval_function_driven_search(problem)
print('Example 3 Solution:')
print_path(output)
print('The number of nodes generated: ', numOfNodeGenerated)
print('The number of nodes expanted: ', numOfNodeExpanded)
print('The maximum length of the queue: ', maxLenOfQueue)
print('The length of solution: ', len(output))