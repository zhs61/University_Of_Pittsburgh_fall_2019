
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
        if (no_of_accepted > 20000) or (i-no_of_accepted > 20000):
            break
        no_steps+=1
        temperature = init_temperature * pow(0.99, i)
        if temperature==0:
            continue
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
    return energy

if __name__ == '__main__':
    total_energy = 0.0;
    for i in range(10):
        total_energy += sim_anneal(TSP_Problem(Standard_Cities),100000,100)
    print("average energy: ", total_energy/10.0)