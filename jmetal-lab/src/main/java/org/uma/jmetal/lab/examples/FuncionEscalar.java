package org.uma.jmetal.lab.examples;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.myproblems.MyQuadraticProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;

import java.util.List;

public class FuncionEscalar extends AbstractAlgorithmRunner {
    public static void main(String[] args) {
        var problem = new MyQuadraticProblem();

        var crossover = new SBXCrossover(0.9, 20.0);
        var mutation = new PolynomialMutation(1.0 / problem.numberOfVariables(), 20.0);
        var selection = new BinaryTournamentSelection<DoubleSolution>();

        // Declarar la variable como NSGAII<DoubleSolution>
        NSGAII<DoubleSolution> algorithm = new NSGAIIBuilder<>(
                problem,
                crossover,
                mutation,
                100 // tamaño de la población
        )
                .setMaxEvaluations(250000)
                .setSelectionOperator(selection)
                .build();

        // Ejecutar el algoritmo
        algorithm.run();

        // En jMetal 6.9.3, el resultado se obtiene con getPopulation()
        List<DoubleSolution> population = algorithm.getPopulation();

        printFinalSolutionSet(population);

        DoubleSolution best = population.stream()
                .min((s1, s2) -> Double.compare(s1.objectives()[0], s2.objectives()[0]))
                .orElseThrow();

        System.out.println("Mejor solución: x = " + best.variables().get(0)
                + ", f(x) = " + best.objectives()[0]);
    }
}
