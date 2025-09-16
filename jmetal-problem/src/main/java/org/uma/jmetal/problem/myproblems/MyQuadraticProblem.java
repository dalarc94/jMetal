package org.uma.jmetal.problem.myproblems;

import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.bounds.Bounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Ejemplo simple: minimizar f(x) = x^2 con x en [-5, 5]
 */
public class MyQuadraticProblem implements DoubleProblem {

    private final List<Bounds<Double>> bounds;

    public MyQuadraticProblem() {
        bounds = new ArrayList<>();
        bounds.add(Bounds.create(-5.0, 5.0)); // variable x ∈ [-5, 5]
    }

    @Override
    public int numberOfVariables() {
        return bounds.size();
    }

    @Override
    public int numberOfObjectives() {
        return 1;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    @Override
    public String name() {
        return "MyQuadraticProblem";
    }

    @Override
    public List<Bounds<Double>> variableBounds() {
        return bounds;
    }

    @Override
    public DoubleSolution createSolution() {
        // Constructor seguro: (List<Bounds<Double>> variableBounds, int numberOfObjectives, int numberOfConstraints)
        return new DefaultDoubleSolution(variableBounds(), numberOfObjectives(), numberOfConstraints());
    }

    @Override
    public DoubleSolution evaluate(DoubleSolution solution) {
        double x = solution.variables().get(0);

        //redondear para trabajar con enteros
        //int x = (int) Math.round(solution.variables().get(0));
        double fx = x * x;   // f(x) = x^2
        solution.objectives()[0] = fx;
        return solution;
    }
}
