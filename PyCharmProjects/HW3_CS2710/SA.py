
from TSP import *

def sim_anneal(TSP_problem, no_of_steps, init_temperature ):
    init_tour = TSP_problem.generate_random_tour()
    init_tour = TSP_problem.permute_tour(init_tour)
    init_energy = TSP_problem.evaluate_tour(init_tour)
    tour = init_tour
    energy = init_energy
    no_of_accepted = 0
    for i in range(no_of_steps):
        newTour = TSP_problem.permute_tour(tour)
        newEnergy = TSP_problem.evaluate_tour(newTour)
        if newEnergy < energy:
            tour = newTour
            energy = newEnergy
            no_of_accepted += 1
        else:
            temperature = init_temperature/no_of_steps*(no_of_steps-i)
            p = math.exp(energy - newEnergy / temperature)
            rand = random.random()
            if rand <= p:
                no_of_accepted += 1
                tour = newTour
                energy = newEnergy

    print(init_tour, init_energy)
    print(init_temperature)
    print(no_of_steps)
    print(no_of_accepted)
    print(tour, energy)

if __name__ == '__main__':
    sim_anneal(TSP_Problem(Standard_Cities),100000,100)