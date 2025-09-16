package org.uma.jmetal.lab.examples;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerSBXCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerPolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.myproblems.MyQuadraticProblemInteger;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;

import java.util.List;

public class FuncionEscalarInteger extends AbstractAlgorithmRunner {
    public static void main(String[] args) {

        var problem = new MyQuadraticProblemInteger();

        // Operadores compatibles con enteros
        var crossover = new IntegerSBXCrossover(0.9, 20.0);
        var mutation = new IntegerPolynomialMutation(1.0 / problem.numberOfVariables(), 20.0);
        var selection = new BinaryTournamentSelection<IntegerSolution>();

        // NSGA-II
        NSGAII<IntegerSolution> algorithm = new NSGAIIBuilder<>(
                problem,
                crossover,
                mutation,
                50 // tamaño de la población
        )
                .setMaxEvaluations(500) // suficiente para converger
                .setSelectionOperator(selection)
                .build();

        algorithm.run();

        // Obtener población final
        List<IntegerSolution> population = algorithm.getPopulation();

        // Imprimir soluciones finales
        printFinalSolutionSet(population);

        // Mejor solución
        IntegerSolution best = population.stream()
                .min((s1, s2) -> Double.compare(s1.objectives()[0], s2.objectives()[0]))
                .orElseThrow();



        System.out.println("Mejor solución: x = " + best.variables().get(0)
                + ", f(x) = " + best.objectives()[0]);
    }
}
