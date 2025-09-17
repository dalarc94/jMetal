package org.uma.jmetal.lab.examples;

import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uma.jmetal.problem.ejemplos.Ex001Funcion;

public class ExecEx001Funcion {

    public static void main(String[] args) {
        BinaryProblem problem = new Ex001Funcion();
        int populationSize = 50;
        int maxGenerations = 100;
        double crossoverProbability = 0.9;
        double mutationProbability = 0.05;

        Random random = new Random();

        // Crear población inicial
        List<BinarySolution> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            BinarySolution sol = problem.createSolution();
            population.add(problem.evaluate(sol));
        }

        SinglePointCrossover crossover = new SinglePointCrossover(crossoverProbability);
        BitFlipMutation mutation = new BitFlipMutation(mutationProbability);

        // Bucle de generaciones
        for (int gen = 0; gen < maxGenerations; gen++) {
            List<BinarySolution> newPopulation = new ArrayList<>();
            while (newPopulation.size() < populationSize) {
                // Selección: torneo binario
                BinarySolution parent1 = population.get(random.nextInt(populationSize));
                BinarySolution parent2 = population.get(random.nextInt(populationSize));

                List<BinarySolution> offspring = crossover.execute(List.of(parent1, parent2));
                for (BinarySolution child : offspring) {
                    mutation.execute(child);
                    newPopulation.add(problem.evaluate(child));
                    if (newPopulation.size() >= populationSize) break;
                }
            }
            population = newPopulation;
        }

        // Encontrar la mejor solución
        BinarySolution best = population.get(0);
        for (BinarySolution sol : population) {
            if (sol.objectives()[0] < best.objectives()[0]) best = sol;
        }

        // Mostrar resultados decodificados
        for (int i = 0; i < best.variables().size(); i++) {
            System.out.println("Variable " + i + ": " + decode(best.variables().get(i)));
        }
        System.out.println("Valor objetivo: " + (-best.objectives()[0])); // revertimos negación
    }

    private static int decode(BinarySet bits) {
        int value = 0;
        for (int i = 0; i < bits.getBinarySetLength(); i++) {
            if (bits.get(i)) value += (1 << i);
        }
        return value;
    }
}
