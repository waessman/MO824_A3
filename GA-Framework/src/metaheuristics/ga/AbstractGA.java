package metaheuristics.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import problems.Evaluator;
import solutions.Solution;

/**
 * Abstract class for metaheuristic GA (Genetic Algorithms). It consider the
 * maximization of the chromosome fitness.
 * 
 * @author ccavellucci, fusberti
 * @param <G>
 *            Generic type of the chromosome element (genotype).
 * @param <F>
 *            Generic type of the candidate to enter the solution (fenotype).
 */
public abstract class AbstractGA<G extends Number, F> {

	@SuppressWarnings("serial")
	public class Chromosome extends ArrayList<G> {
		public int GetCurrentPeso() {
			Double pesos[] = ObjFunction.getPesos();
			int peso = 0;
			for(int i = 0; i < chromosomeSize; i++) {
				Integer gene = (Integer) this.get(i);
				if(gene == 1) {
					peso += pesos[i];					
				}
			}
			return peso;
		}
	}

	@SuppressWarnings("serial")
	public class Population extends ArrayList<Chromosome> {

	}

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;

	/**
	 * a random number generator
	 */
	public static final Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<F> ObjFunction;

	/**
	 * maximum number of generations being executed
	 */
	protected int timeToRun;

	/**
	 * the size of the population
	 */
	protected int popSize;

	/**
	 * the size of the chromosome
	 */
	protected int chromosomeSize;

	/**
	 * the probability of performing a mutation
	 */
	protected double mutationRate;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;

	/**
	 * the best solution
	 */
	protected Solution<F> bestSol;

	/**
	 * the best chromosome, according to its fitness evaluation
	 */
	protected Chromosome bestChromosome;

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<F> createEmptySol();

	/**
	 * A mapping from the genotype (domain) to the fenotype (image). In other
	 * words, it takes a chromosome as input and generates a corresponding
	 * solution.
	 * 
	 * @param chromosome
	 *            The genotype being considered for decoding.
	 * @return The corresponding fenotype (solution).
	 */
	protected abstract Solution<F> decode(Chromosome chromosome);

	/**
	 * Generates a random chromosome according to some probability distribution
	 * (usually uniform).
	 * 
	 * @return A random chromosome.
	 */
	protected abstract Chromosome generateRandomChromosome();

	/**
	 * Determines the fitness for a given chromosome. The fitness should be a
	 * function strongly correlated to the objective function under
	 * consideration.
	 * 
	 * @param chromosome
	 *            The genotype being considered for fitness evaluation.
	 * @return The fitness value for the input chromosome.
	 */
	protected abstract Double fitness(Chromosome chromosome);

	/**
	 * Mutates a given locus of the chromosome. This method should be preferably
	 * called with an expected frequency determined by the {@link #mutationRate}.
	 * 
	 * @param chromosome
	 *            The genotype being mutated.
	 * @param locus
	 *            The position in the genotype being mutated.
	 */
	protected abstract void mutateGene(Chromosome chromosome, Integer locus);

	/**
	 * The constructor for the GA class.
	 * 
	 * @param objFunction
	 *            The objective function being optimized.
	 * @param generations
	 *            Number of generations to be executed.
	 * @param popSize
	 *            Population size.
	 * @param mutationRate
	 *            The mutation rate.
	 */
	public AbstractGA(Evaluator<F> objFunction, Integer timeToRun, Integer popSize, Double mutationRate) {
		this.ObjFunction = objFunction;
		this.timeToRun = timeToRun;
		this.popSize = popSize;
		this.chromosomeSize = this.ObjFunction.getDomainSize();
		this.mutationRate = mutationRate;
	}

	/**
	 * The GA mainframe. It starts by initializing a population of chromosomes.
	 * It then enters a generational loop, in which each generation goes the
	 * following steps: parent selection, crossover, mutation, population update
	 * and best solution update.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Population population;
	
	public Solution<F> solve() {

		/* starts the initial population */
		//population = initializePopulation();
		population = initializePopulationLatinHypercube();

		bestChromosome = getBestChromosome(population);
		bestSol = decode(bestChromosome);
		System.out.println("(Gen. " + 0 + ") BestSol = " + bestSol);

		/*
		 * enters the main loop and repeats until a given number of generations
		 */
		long startTime = System.currentTimeMillis();
        long endTime = startTime + timeToRun;
		for (int g = 1; true; g++) {
			if (System.currentTimeMillis() > endTime)
                break;
			Population parents = selectParents(population);

			Population offsprings = uniformCrossover(parents, 0.5);
			//Population offsprings = crossover(parents);
			
			Population mutants = mutate(offsprings);

			Population newpopulation = selectPopulation(mutants);

			population = newpopulation;

			bestChromosome = getBestChromosome(population);

			if (fitness(bestChromosome) > bestSol.cost) {
				bestSol = decode(bestChromosome);
				if (verbose)
					System.out.println("(Gen. " + g + ") BestSol = " + bestSol);
			}

		}

		return bestSol;
	}

	/**
	 * Randomly generates an initial population to start the GA.
	 * 
	 * @return A population of chromosomes.
	 */
	protected Population initializePopulation() {

		Population population = new Population();

		while (population.size() < popSize) {
			population.add(generateRandomChromosome());
		}

		return population;

	}
	
	protected abstract Population initializePopulationLatinHypercube();

	/**
	 * Given a population of chromosome, takes the best chromosome according to
	 * the fitness evaluation.
	 * 
	 * @param population
	 *            A population of chromosomes.
	 * @return The best chromosome among the population.
	 */
	protected Chromosome getBestChromosome(Population population) {

		double bestFitness = Double.NEGATIVE_INFINITY;
		Chromosome bestChromosome = null;
		for (Chromosome c : population) {
			double fitness = fitness(c);
			if (fitness > bestFitness) {
				bestFitness = fitness;
				bestChromosome = c;
			}
		}

		return bestChromosome;
	}

	/**
	 * Given a population of chromosome, takes the worst chromosome according to
	 * the fitness evaluation.
	 * 
	 * @param population
	 *            A population of chromosomes.
	 * @return The worst chromosome among the population.
	 */
	protected Chromosome getWorseChromosome(Population population) {

		double worseFitness = Double.POSITIVE_INFINITY;
		Chromosome worseChromosome = null;
		for (Chromosome c : population) {
			double fitness = fitness(c);
			if (fitness < worseFitness) {
				worseFitness = fitness;
				worseChromosome = c;
			}
		}

		return worseChromosome;
	}

	/**
	 * Selection of parents for crossover using the tournament method. Given a
	 * population of chromosomes, randomly takes two chromosomes and compare
	 * them by their fitness. The best one is selected as parent. Repeat until
	 * the number of selected parents is equal to {@link #popSize}.
	 * 
	 * @param population
	 *            The current population.
	 * @return The selected parents for performing crossover.
	 */
	protected Population selectParents(Population population) {

		Population parents = new Population();
		
		while (parents.size() < popSize) {
			int index1 = rng.nextInt(popSize);
			Chromosome parent1 = population.get(index1);
			int index2 = rng.nextInt(popSize);
			Chromosome parent2 = population.get(index2);
			if (fitness(parent1) > fitness(parent2)) {
				parents.add(parent1);
			} else {
				parents.add(parent2);
			}
		}

		return parents;

	}

	/**
	 * The crossover step takes the parents generated by {@link #selectParents}
	 * and recombine their genes to generate new chromosomes (offsprings). The
	 * method being used is the 2-point crossover, which randomly selects two
	 * locus for being the points of exchange (P1 and P2). For example:
	 * 
	 *                        P1            P2
	 *    Parent 1: X1 ... Xi | Xi+1 ... Xj | Xj+1 ... Xn
	 *    Parent 2: Y1 ... Yi | Yi+1 ... Yj | Yj+1 ... Yn
	 * 
	 * Offspring 1: X1 ... Xi | Yi+1 ... Yj | Xj+1 ... Xn
	 * Offspring 2: Y1 ... Yi | Xi+1 ... Xj | Yj+1 ... Yn
	 * 
	 * @param parents
	 *            The selected parents for crossover.
	 * @return The resulting offsprings.
	 */
	protected Population crossover(Population parents) {

		Population offsprings = new Population();
		
		Double pesos[] = ObjFunction.getPesos();
		int pesoMaximo = ObjFunction.getPesoMax();
		
		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);

			int crosspoint1 = rng.nextInt(chromosomeSize + 1);
			int crosspoint2 = crosspoint1 + rng.nextInt((chromosomeSize + 1) - crosspoint1);

			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();
			
	        int pesoOffspring1 = 0;
	        int pesoOffspring2 = 0;

			for (int j = 0; j < chromosomeSize; j++) {
				
	            Integer geneToAddFrom1 = (Integer) parent1.get(j);
	            Integer geneToAddFrom2 = (Integer) parent2.get(j);
				
				if (j >= crosspoint1 && j < crosspoint2) {
					
					//Add gene2 ao offspring1 se possivel, senao add 0
					if(geneToAddFrom2 == 1) {
						if(pesoOffspring1 + pesos[j] <= pesoMaximo) {
							offspring1.add((G) geneToAddFrom2);
							pesoOffspring1 += pesos[j];
						} else {
							offspring1.add((G) ((Integer) 0));
						}
					} else {
						offspring1.add((G) geneToAddFrom2);						
					}
					
					//Add gene1 ao offspring2 se possivel, senao add 0
					if(geneToAddFrom1 == 1) {
						if(pesoOffspring2 + pesos[j] <= pesoMaximo) {
							offspring2.add((G) geneToAddFrom1);
							pesoOffspring2 += pesos[j];
						} else {
							offspring2.add((G) ((Integer) 0));
						}
					} else {
						offspring2.add((G) geneToAddFrom1);						
					}
					
				} else {
					//Add gene1 ao offspring1 se possivel, senao add 0
					if(geneToAddFrom1 == 1) {
						if(pesoOffspring1 + pesos[j] <= pesoMaximo) {
							offspring1.add((G) geneToAddFrom1);
							pesoOffspring1 += pesos[j];
						} else {
							offspring1.add((G) ((Integer) 0));
						}
					} else {
						offspring1.add((G) geneToAddFrom1);						
					}
					
					//Add gene2 ao offspring2 se possivel, senao add 0
					if(geneToAddFrom2 == 1) {
						if(pesoOffspring2 + pesos[j] <= pesoMaximo) {
							offspring2.add((G) geneToAddFrom2);
							pesoOffspring2 += pesos[j];
						} else {
							offspring2.add((G) ((Integer) 0));
						}
					} else {
						offspring2.add((G) geneToAddFrom2);						
					}
				}
			}
			//System.out.println("Peso offspring1: "+pesoOffspring1 + " | peso offspring2: "+pesoOffspring2);
			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
		return offsprings;
	}
	
	
	/**
	 * The crossover step takes the parents generated by {@link #selectParents}
	 * and recombine their genes to generate new chromosomes (offsprings). The
	 * method being used is the 2-point crossover, which randomly selects two
	 * locus for being the points of exchange (P1 and P2). For example:
	 * 
	 * @param parents
	 *            The selected parents for crossover.
	 * @param p
	 * 			  O par�metro de "Vi�s" acima de determinado pai, sendo:
	 * 0.5 = completamente aleat�rio
	 * < 0.5 tende ao parent1
	 * > 0.5 tende ao parent2
	 *
	 * @return The resulting offsprings.
	 */
	protected Population uniformCrossover(Population parents, Double p) {
		Population offsprings = new Population();
		
		Double pesos[] = ObjFunction.getPesos();
		int pesoMaximo = ObjFunction.getPesoMax();
		
		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);

			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();
			
	        int pesoOffspring1 = 0;
	        int pesoOffspring2 = 0;
	        
	        Integer[] mask = new Integer[chromosomeSize];
	        
	        for (int j = 0; j < chromosomeSize; j++) {
	        	double valor = rng.nextDouble();
	        	if(valor < p) {
	        		mask[j] = 1;
	        	} else {
	        		mask[j] = 0;
	        	}
	        }

	        
	        //Adicionando elementos ao offspring 1
			for (int j = 0; j < chromosomeSize; j++) {
	            Integer geneToAdd = mask[j] == 1 ? (Integer) parent2.get(j) : (Integer) parent1.get(j);
	            if(geneToAdd == 1) {
	            	if(pesoOffspring1 + pesos[j] <= pesoMaximo) {
	            		offspring1.add((G) geneToAdd);
	            		pesoOffspring1 += pesos[j];
	            	} else {
	            		offspring1.add((G) ((Integer) 0));
	            	}
	            } else {
	            	offspring1.add((G) geneToAdd);
	            }
			}
			
			// Gera outra mask para o offspring2
	        for (int j = 0; j < chromosomeSize; j++) {
	        	double valor = rng.nextDouble();
	        	if(valor < p) {
	        		mask[j] = 1;
	        	} else {
	        		mask[j] = 0;
	        	}
	        }
	        
	        //Adicionando elementos ao offspring 2
			for (int j = 0; j < chromosomeSize; j++) {
	            Integer geneToAdd = mask[j] == 1 ? (Integer) parent2.get(j) : (Integer) parent1.get(j);
	            if(geneToAdd == 1) {
	            	if(pesoOffspring2 + pesos[j] <= pesoMaximo) {
	            		offspring2.add((G) geneToAdd);
	            		pesoOffspring2 += pesos[j];
	            	} else {
	            		offspring2.add((G) ((Integer) 0));
	            	}
	            } else {
	            	offspring2.add((G) geneToAdd);
	            }
			}
			
			//System.out.println("Peso offspring1: "+pesoOffspring1 + " | peso offspring2: "+pesoOffspring2);
			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
		return offsprings;
	}
	/**
	 * The mutation step takes the offsprings generated by {@link #crossover}
	 * and to each possible locus, perform a mutation with the expected
	 * frequency given by {@link #mutationRate}.
	 * 
	 * @param offsprings
	 *            The offsprings chromosomes generated by the
	 *            {@link #crossover}.
	 * @return The mutated offsprings.
	 */
	protected Population mutate(Population offsprings) {

		for (Chromosome c : offsprings) {
			for (int locus = 0; locus < chromosomeSize; locus++) {
				if (rng.nextDouble() < mutationRate) {
					mutateGene(c, locus);
				}
			}
		}

		return offsprings;
	}

	/**
	 * Updates the population that will be considered for the next GA
	 * generation. The method used for updating the population is the elitist,
	 * which simply takes the worse chromosome from the offsprings and replace
	 * it with the best chromosome from the previous generation.
	 * 
	 * @param offsprings
	 *            The offsprings generated by {@link #crossover}.
	 * @return The updated population for the next generation.
	 */
	protected Population selectPopulation(Population offsprings) {

		Chromosome worse = getWorseChromosome(offsprings);
		if (fitness(worse) < fitness(bestChromosome)) {
			offsprings.remove(worse);
			offsprings.add(bestChromosome);
		}

		return offsprings;
	}

}
