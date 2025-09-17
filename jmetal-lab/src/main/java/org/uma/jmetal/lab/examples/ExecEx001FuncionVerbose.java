package org.uma.jmetal.lab.examples;

import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.problem.ejemplos.Ex001Funcion;
import org.uma.jmetal.util.binarySet.BinarySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExecEx001FuncionVerbose {

    public static void main(String[] args) {
        BinaryProblem problem = new Ex001Funcion();
        int populationSize = 10;   // menor tamaño para ejemplo más legible
        int maxGenerations = 5;    // pocas generaciones para ver la evolución
        double crossoverProbability = 0.9;
        double mutationProbability = 0.05;

        Random random = new Random(); // puedes poner new Random(12345) para reproducibilidad

        // Crear población inicial
        List<BinarySolution> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            BinarySolution sol = problem.createSolution();

            // --- Mostrar la solución antes de evaluar ---
            System.out.println("Creando solución " + i + ":");
            for (int j = 0; j < sol.variables().size(); j++) {
                System.out.println("Variable " + j + " bits: " + sol.variables().get(j));
                System.out.println("Variable " + j + " decodificada: " + decode(sol.variables().get(j)));
            }

            problem.evaluate(sol); // evalúa la función objetivo

            System.out.println("Objetivo: " + (-sol.objectives()[0]));
            System.out.println("-------------------------");

            population.add(sol); // agregar a la población
        }

        SinglePointCrossover crossover = new SinglePointCrossover(crossoverProbability);
        BitFlipMutation mutation = new BitFlipMutation(mutationProbability);

        // Bucle de generaciones
        for (int gen = 0; gen < maxGenerations; gen++) {
            System.out.println("===== Generación " + (gen + 1) + " =====");

            // Mostrar población actual decodificada
            for (int i = 0; i < population.size(); i++) {
                BinarySolution sol = population.get(i);
                System.out.print("Solución " + i + ": ");
                for (BinarySet var : sol.variables()) {
                    System.out.print(decode(var) + " ");
                }
                System.out.println(" | Objetivo: " + (-sol.objectives()[0]));
            }

            // Crear nueva población
            List<BinarySolution> newPopulation = new ArrayList<>();
            while (newPopulation.size() < populationSize) {
                // Selección aleatoria de padres (torneo binario simplificado)
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

        // Mejor solución final
        BinarySolution best = population.get(0);
        for (BinarySolution sol : population) {
            if (sol.objectives()[0] < best.objectives()[0]) best = sol;
        }

        System.out.println("\n===== Mejor solución encontrada =====");
        for (int i = 0; i < best.variables().size(); i++) {
            System.out.println("Variable " + i + ": " + decode(best.variables().get(i)));
        }
        System.out.println("Valor objetivo: " + (-best.objectives()[0]));
    }

    private static int decode(BinarySet bits) {
        int value = 0;
        for (int i = 0; i < bits.getBinarySetLength(); i++) {
            if (bits.get(i)) value += (1 << i);
        }
        return value;
    }
}
