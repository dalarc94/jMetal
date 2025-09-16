package org.uma.jmetal.problem.myproblems;

import org.uma.jmetal.problem.integerproblem.IntegerProblem;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimizar f(x) = x^2 con x ∈ [-5, 5] (enteros)
 */
public class MyQuadraticProblemInteger implements IntegerProblem {

    private final List<Bounds<Integer>> bounds;

    public MyQuadraticProblemInteger() {
        bounds = new ArrayList<>();
        bounds.add(Bounds.create(-5, 5)); // x ∈ [-5,5]
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
        return "MyQuadraticProblemInteger";
    }

    @Override
    public List<Bounds<Integer>> variableBounds() {
        return bounds;
    }

    @Override
    public IntegerSolution createSolution() {
        return new DefaultIntegerSolution(variableBounds(), numberOfObjectives(), numberOfConstraints());
    }

    @Override
    public IntegerSolution evaluate(IntegerSolution solution) {
        // Redondear a entero (por seguridad)
        int x = solution.variables().get(0);
        solution.variables().set(0, x); // guardar el valor entero
        solution.objectives()[0] = x * x; // calcular f(x)

        // Imprimir valor de x y f(x)
        System.out.println("Evaluando x = " + x + ", f(x) = " + solution.objectives()[0]);

        return solution;
    }
}
