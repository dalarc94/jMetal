package org.uma.jmetal.problem.ejemplos;

import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.binarySet.BinarySet;

import java.util.ArrayList;
import java.util.List;

/*
    Ejemplo descrito en:
    AN INTRODUCTION TO GENETIC ALGORITHMS
    Scott M. Thede (2004)
*/

public class Ex001Funcion implements BinaryProblem {
    private final List<Integer> numberOfBits;

    public Ex001Funcion() {
        numberOfBits = new ArrayList<>();
        // Cada variable (w,x,y,z) se representa con 4 bits
        numberOfBits.add(4); // w
        numberOfBits.add(4); // x
        numberOfBits.add(4); // y
        numberOfBits.add(4); // z
    }

    public Ex001Funcion(List<Integer> numberOfBits) {
        this.numberOfBits = numberOfBits;
    }

    @Override
    public int numberOfVariables() {
        return 4;
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
        return "Ex001Funcion";
    }

    @Override
    public List<Integer> numberOfBitsPerVariable() {
        return numberOfBits;
    }

    @Override
    public int totalNumberOfBits() {
        return numberOfBits.stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public BinarySolution createSolution() {
        return new DefaultBinarySolution(numberOfBitsPerVariable(), numberOfObjectives());
    }

    @Override
    public BinarySolution evaluate(BinarySolution solution) {
        // Decodificar variables desde bits → enteros
        int w = decode(solution.variables().get(0));
        int x = decode(solution.variables().get(1));
        int y = decode(solution.variables().get(2));
        int z = decode(solution.variables().get(3));

        // Evaluar función
        double value = Math.pow(w, 3) + Math.pow(x, 2) - Math.pow(y, 2) - Math.pow(z, 2)
                + 2 * y * z - 3 * w * x + w * z - x * y + 2;

        // jMetal minimiza por defecto → para maximizar guardamos -f → se multiplica por -1
        solution.objectives()[0] = -value;

        return solution;
    }

    /** Decodificar un BinarySet a entero */
    private int decode(BinarySet bits) {
        int value = 0;
        for (int i = 0; i < bits.getBinarySetLength(); i++) {
            if (bits.get(i)) {
                value += (1 << i);
            }
        }
        return value;
    }
}
