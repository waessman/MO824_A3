package problems.qbf.solvers;

import java.io.IOException;
import metaheuristics.ga.AbstractGA;
import metaheuristics.ga.AbstractGA.Chromosome;
import metaheuristics.ga.AbstractGA.Population;
import problems.qbf.QBF;
import solutions.Solution;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Metaheuristic GA (Genetic Algorithm) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). 
 * 
 * @author ccavellucci, fusberti
 */
public class GA_QBF extends AbstractGA<Integer, Integer> {

	/**
	 * Constructor for the GA_QBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_QBF(Integer timeToRun, Integer popSize, Double mutationRate, String filename) throws IOException {
		super(new QBF(filename), timeToRun, popSize, mutationRate);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		sol.weigth = 0.0;
		return sol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#decode(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Solution<Integer> decode(Chromosome chromosome) {

		Solution<Integer> solution = createEmptySol();
		for (int locus = 0; locus < chromosome.size(); locus++) {
			if (chromosome.get(locus) == 1) {
				solution.add(new Integer(locus));
			}
		}

		ObjFunction.evaluate(solution);
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {
		
		

        Double pesos[] = ObjFunction.getPesos();
        Integer pesoMaximo = ObjFunction.getPesoMax();
        
		Chromosome chromosome = new Chromosome();
		
		int currentPeso = 0;
		
		//Gera lista aleat�ria para n�o ficar tendendo a pegar sempre os primeiros
		Integer[] possiveis = new Integer[chromosomeSize];
		Integer[] chromosomoTemp = new Integer[chromosomeSize];
		
		for (int i = 0; i < chromosomeSize; i++) {
			possiveis[i] = i;
			chromosomoTemp[i] = 0;
		}
		
		List<Integer> lista = Arrays.asList(possiveis);

        // Embaralha a lista
        Collections.shuffle(lista);

        // Converte a lista embaralhada de volta para um array
        Integer[] vetorEmbaralhado = lista.toArray(new Integer[0]);
				
		
		for (int i : vetorEmbaralhado) {
			int escolha = rng.nextInt(2);
			if(escolha == 1) {
				if(currentPeso + pesos[i] <= pesoMaximo) {
					currentPeso += pesos[i];
					chromosomoTemp[i] = escolha;
				} else {
					chromosomoTemp[i] = 0;
				}
			} else {				
				chromosomoTemp[i] = 0;
			}
		}
		
		for (int i = 0; i < chromosomeSize; i++) {
			chromosome.add(chromosomoTemp[i]);
		}

		return chromosome;
	}
	
	@Override
	protected Population initializePopulationLatinHypercube() {

		Population population = new Population();
		ArrayList<Chromosome> cList = new ArrayList<Chromosome>(popSize);
		ArrayList<Integer> column = new ArrayList<Integer>(Collections.nCopies(popSize, 0));
		
		for(int i = 0; i < popSize; i++) {
			column.set(i, i % 2);
			cList.add(new Chromosome());
		}

		for(int i = 0; i < chromosomeSize; i++) {
			Collections.shuffle(column);
			for(int j = 0; j < popSize; j++) {
				cList.get(j).add(column.get(j));
			}
		}
		
		for(Chromosome c : cList) {
			makeCromosomeFeasible(c);
			population.add(c);
		}

		return population;

	}
	
	// if a chromosome violates the knapsack restriction, remove random items from it until it is feasible
	protected Chromosome makeCromosomeFeasible(Chromosome c) {
		Integer pesoMaximo = ObjFunction.getPesoMax();
		while(ObjFunction.solutionWeight(decode(c)) > pesoMaximo) {
			removeRandomElementFromChromosome(c);
		}
		return c;
	}
	
	protected Chromosome removeRandomElementFromChromosome(Chromosome c) {
		ArrayList<Integer> currentElements = new ArrayList<Integer>();
		for(int i = 0; i < c.size(); i++) {
			if(c.get(i) == 1) {
				currentElements.add(i);
			}
		}
		c.set(currentElements.get(rng.nextInt(currentElements.size())), 0);
		return c;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#fitness(metaheuristics.ga.AbstractGA.
	 * Chromosome)
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {

		return decode(chromosome).cost;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * metaheuristics.ga.AbstractGA#mutateGene(metaheuristics.ga.AbstractGA.
	 * Chromosome, java.lang.Integer)
	 */
	@Override
	protected void mutateGene(Chromosome chromosome, Integer locus) {
		var pesoAtual = chromosome.GetCurrentPeso();
		int pesoMaximo = ObjFunction.getPesoMax();
		Double pesos[] = ObjFunction.getPesos();
		
		int valorAtual = chromosome.get(locus);
		
		if(valorAtual == 0 && ((pesoAtual + pesos[locus]) <= pesoMaximo)) {
			chromosome.set(locus, 1);
		} else {
			chromosome.set(locus, 0);
		}

	}

	/**
	 * A main method used for testing the GA metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		GA_QBF ga = new GA_QBF(100000, 100, 3.0 / 100.0, "instances/kqbf/kqbf040");
		Solution<Integer> bestSol = ga.solve();
		System.out.println("maxVal = " + bestSol);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");

	}

}
