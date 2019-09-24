
from TSP import *

def sim_anneal(TSP_problem, no_of_steps, init_temperature ):
    init_tour = TSP_problem.generate_random_tour()
    init_tour = TSP_problem.permute_tour(init_tour)
    init_energy = TSP_problem.evaluate_tour(init_tour)
    tour = init_tour
    energy = init_energy
    no_of_accepted = 0
    no_steps = 0
    for i in range(no_of_steps):
        no_steps+=1
        temperature = init_temperature / no_of_steps * (no_of_steps - i)
        newTour = TSP_problem.permute_tour(tour)
        newEnergy = TSP_problem.evaluate_tour(newTour)
        if newEnergy < energy:
            tour = newTour
            energy = newEnergy
            no_of_accepted += 1
        else:
            p = math.exp((energy - newEnergy) / temperature)
            if random.random() <= p:
                no_of_accepted += 1
                tour = newTour
                energy = newEnergy

    print("init tour: ",init_tour, init_energy)
    print("init_temperature: ", init_temperature)
    print("Number of steps: ", no_steps)
    print("Number of accepted: ", no_of_accepted)
    print("Final tour: ", tour, energy)

if __name__ == '__main__':
    sim_anneal(TSP_Problem(Standard_Cities),500000,100)