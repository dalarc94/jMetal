package org.uma.jmetal.auto.autoconfigurablealgorithm.examples;

import org.uma.jmetal.auto.autoconfigurablealgorithm.AutoNSGAII;
import org.uma.jmetal.auto.autoconfigurablealgorithm.AutoSMSEMOA;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.problem.multiobjective.zcat.DefaultZCATSettings;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.observer.impl.EvaluationObserver;
import org.uma.jmetal.util.observer.impl.RunTimeChartObserver;

/**
 * Class configuring NSGA-II using arguments in the form <key, value> and the {@link AutoNSGAII}
 * class.
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class SMSEMOAConfiguredFromZCAT1 {

  public static void main(String[] args) {
    String referenceFrontFileName = "resources/referenceFrontsCSV/ZCAT1.3D.csv";

    DefaultZCATSettings.numberOfObjectives = 3 ;

    String[] parameters =
        ("--problemName org.uma.jmetal.problem.multiobjective.zcat.ZCAT1 "
                + "--randomGeneratorSeed 12 "
                + "--referenceFrontFileName "
                + referenceFrontFileName
                + " "
                + "--maximumNumberOfEvaluations 100000 "
                + "--populationSize 100 "
                + "--algorithmResult population  "
                + "--createInitialSolutions random "
                + "--offspringPopulationSize 1 "
                + "--variation crossoverAndMutationVariation --crossover SBX "
                + "--crossoverProbability 0.9 "
                + "--crossoverRepairStrategy bounds "
                + "--sbxDistributionIndex 20.0 "
                + "--mutation polynomial "
                + "--mutationProbabilityFactor 1.0 "
                + "--polynomialMutationDistributionIndex 20.0 "
                + "--mutationRepairStrategy bounds "
                + "--selection tournament "
                + "--selectionTournamentSize 2 \n")
            .split("\\s+");

    var autoSMSEMOA = new AutoSMSEMOA();
    autoSMSEMOA.parse(parameters);

    autoSMSEMOA.print(autoSMSEMOA.fixedParameterList());
    autoSMSEMOA.print(autoSMSEMOA.configurableParameterList());

    EvolutionaryAlgorithm<DoubleSolution> nsgaII = autoSMSEMOA.create();

    EvaluationObserver evaluationObserver = new EvaluationObserver(1000);
    RunTimeChartObserver<DoubleSolution> runTimeChartObserver =
        new RunTimeChartObserver<>("NSGA-II", 80, 1000, referenceFrontFileName, "F1", "F2");

    nsgaII.observable().register(evaluationObserver);
    nsgaII.observable().register(runTimeChartObserver);

    nsgaII.run();

    JMetalLogger.logger.info("Total computing time: " + nsgaII.totalComputingTime());
    ;

    new SolutionListOutput(nsgaII.result())
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();
  }
}
