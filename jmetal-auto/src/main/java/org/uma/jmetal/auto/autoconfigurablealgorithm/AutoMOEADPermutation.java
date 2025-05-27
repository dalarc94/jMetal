package org.uma.jmetal.auto.autoconfigurablealgorithm;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.uma.jmetal.auto.parameter.*;
import org.uma.jmetal.auto.parameter.catalogue.AggregationFunctionParameter;
import org.uma.jmetal.auto.parameter.catalogue.CreateInitialSolutionsParameter;
import org.uma.jmetal.auto.parameter.catalogue.CrossoverParameter;
import org.uma.jmetal.auto.parameter.catalogue.ExternalArchiveParameter;
import org.uma.jmetal.auto.parameter.catalogue.MutationParameter;
import org.uma.jmetal.auto.parameter.catalogue.ProbabilityParameter;
import org.uma.jmetal.auto.parameter.catalogue.SelectionParameter;
import org.uma.jmetal.auto.parameter.catalogue.SequenceGeneratorParameter;
import org.uma.jmetal.auto.parameter.catalogue.VariationParameter;
import org.uma.jmetal.component.algorithm.EvolutionaryAlgorithm;
import org.uma.jmetal.component.catalogue.common.evaluation.Evaluation;
import org.uma.jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluation;
import org.uma.jmetal.component.catalogue.common.evaluation.impl.SequentialEvaluationWithArchive;
import org.uma.jmetal.component.catalogue.common.solutionscreation.SolutionsCreation;
import org.uma.jmetal.component.catalogue.common.termination.Termination;
import org.uma.jmetal.component.catalogue.common.termination.impl.TerminationByEvaluations;
import org.uma.jmetal.component.catalogue.ea.replacement.Replacement;
import org.uma.jmetal.component.catalogue.ea.replacement.impl.MOEADReplacement;
import org.uma.jmetal.component.catalogue.ea.selection.Selection;
import org.uma.jmetal.component.catalogue.ea.selection.impl.PopulationAndNeighborhoodSelection;
import org.uma.jmetal.component.catalogue.ea.variation.Variation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.ProblemFactory;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.aggregationfunction.AggregationFunction;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.neighborhood.Neighborhood;
import org.uma.jmetal.util.neighborhood.impl.WeightVectorNeighborhood;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Class to configure NSGA-II with an argument string using class {@link EvolutionaryAlgorithm}
 *
 * @autor Antonio J. Nebro
 */
public class AutoMOEADPermutation implements AutoConfigurableAlgorithm {
  public List<Parameter<?>> autoConfigurableParameterList = new ArrayList<>();
  public List<Parameter<?>> fixedParameterList = new ArrayList<>();
  private StringParameter problemNameParameter;
  public StringParameter referenceFrontFilenameParameter;
  private PositiveIntegerValue randomGeneratorSeedParameter;
  private PositiveIntegerValue maximumNumberOfEvaluationsParameter;
  private CategoricalParameter algorithmResultParameter;
  private ExternalArchiveParameter<PermutationSolution<Integer>> externalArchiveParameter;
  private PositiveIntegerValue populationSizeParameter;
  private CategoricalIntegerParameter offspringPopulationSizeParameter;
  private CreateInitialSolutionsParameter createInitialSolutionsParameter;
  private SelectionParameter<PermutationSolution<Integer>> selectionParameter;
  private VariationParameter variationParameter;
  private ProbabilityParameter neighborhoodSelectionProbabilityParameter;
  private IntegerParameter neighborhoodSizeParameter;
  private IntegerParameter maximumNumberOfReplacedSolutionsParameter;
  private AggregationFunctionParameter aggregationFunctionParameter;
  private CategoricalParameter normalizeObjectivesParameter;
  private SequenceGeneratorParameter subProblemIdGeneratorParameter;

  @Override
  public List<Parameter<?>> configurableParameterList() {
    return autoConfigurableParameterList;
  }

  @Override
  public List<Parameter<?>> fixedParameterList() {
    return fixedParameterList;
  }

  public AutoMOEADPermutation() {
    this.configure();
  }

  public void configure() {
    subProblemIdGeneratorParameter =
        new SequenceGeneratorParameter(List.of("permutation", "integerSequence"));

    normalizeObjectivesParameter = new CategoricalParameter("normalizeObjectives", List.of("TRUE", "FALSE"));
    RealParameter epsilonParameterForNormalizing =
        new RealParameter("epsilonParameterForNormalizing", 0.0000001, 25.0);
    normalizeObjectivesParameter.addSpecificParameter("TRUE", epsilonParameterForNormalizing);

    problemNameParameter = new StringParameter("problemName");
    randomGeneratorSeedParameter = new PositiveIntegerValue("randomGeneratorSeed");
    maximumNumberOfEvaluationsParameter = new PositiveIntegerValue("maximumNumberOfEvaluations");

    referenceFrontFilenameParameter = new StringParameter("referenceFrontFileName");

    populationSizeParameter = new PositiveIntegerValue("populationSize");

    offspringPopulationSizeParameter =
        new CategoricalIntegerParameter("offspringPopulationSize", List.of(1));

    fixedParameterList.add(populationSizeParameter);
    fixedParameterList.add(problemNameParameter);
    fixedParameterList.add(referenceFrontFilenameParameter);
    fixedParameterList.add(maximumNumberOfEvaluationsParameter);
    fixedParameterList.add(randomGeneratorSeedParameter);

    neighborhoodSizeParameter = new IntegerParameter("neighborhoodSize", 5, 50);
    neighborhoodSelectionProbabilityParameter =
        new ProbabilityParameter("neighborhoodSelectionProbability");
    maximumNumberOfReplacedSolutionsParameter =
        new IntegerParameter("maximumNumberOfReplacedSolutions", 1, 5);

    aggregationFunctionParameter =
        new AggregationFunctionParameter(
            List.of(
                "tschebyscheff",
                "weightedSum",
                "penaltyBoundaryIntersection",
                "modifiedTschebyscheff"));
    RealParameter pbiTheta = new RealParameter("pbiTheta", 1.0, 200);
    aggregationFunctionParameter.addSpecificParameter("penaltyBoundaryIntersection", pbiTheta);
    aggregationFunctionParameter.addGlobalParameter(normalizeObjectivesParameter);

    algorithmResult();
    createInitialSolution();
    selection();
    variation();

    autoConfigurableParameterList.add(neighborhoodSizeParameter);
    autoConfigurableParameterList.add(maximumNumberOfReplacedSolutionsParameter);
    autoConfigurableParameterList.add(aggregationFunctionParameter);
    autoConfigurableParameterList.add(subProblemIdGeneratorParameter);

    autoConfigurableParameterList.add(algorithmResultParameter);
    autoConfigurableParameterList.add(createInitialSolutionsParameter);
    autoConfigurableParameterList.add(variationParameter);
    autoConfigurableParameterList.add(selectionParameter);
  }

  private void variation() {
    CrossoverParameter crossoverParameter =
        new CrossoverParameter(List.of("PMX", "CX", "OXD", "positionBased", "edgeRecombination"));

    RealParameter crossoverProbability = new RealParameter("crossoverProbability", 0.6, 0.9);
    crossoverParameter.addGlobalParameter(crossoverProbability);

    MutationParameter mutationParameter =
        new MutationParameter(List.of("swap", "displacement", "insert", "scramble", "inversion", "simpleInversion"));

    RealParameter mutationProbability =
        new RealParameter("mutationProbability", 0.05, 0.1);
    mutationParameter.addGlobalParameter(mutationProbability);

    variationParameter = new VariationParameter(List.of("crossoverAndMutationVariation"));
    variationParameter.addSpecificParameter("crossoverAndMutationVariation", crossoverParameter);
    variationParameter.addSpecificParameter("crossoverAndMutationVariation", mutationParameter);
    variationParameter.addSpecificParameter(
        "crossoverAndMutationVariation", offspringPopulationSizeParameter);
  }

  private void selection() {
    selectionParameter =
        new SelectionParameter<>(List.of("populationAndNeighborhoodMatingPoolSelection"));
    neighborhoodSelectionProbabilityParameter =
        new ProbabilityParameter("neighborhoodSelectionProbability");
    selectionParameter.addSpecificParameter(
        "populationAndNeighborhoodMatingPoolSelection", neighborhoodSelectionProbabilityParameter);
  }

  private void createInitialSolution() {
    createInitialSolutionsParameter = new CreateInitialSolutionsParameter(List.of("random"));
  }

  private void algorithmResult() {
    algorithmResultParameter =
        new CategoricalParameter("algorithmResult", List.of("externalArchive", "population"));
    externalArchiveParameter =
        new ExternalArchiveParameter(List.of("crowdingDistanceArchive", "unboundedArchive"));

    algorithmResultParameter.addSpecificParameter("externalArchive", externalArchiveParameter);
  }

  @Override
  public void parse(String[] arguments) {
    String[] offspringPopulationSizeValue = new String[] {"--offspringPopulationSize", "1"};
    String[] extendedArguments =
        Stream.concat(Arrays.stream(arguments), Arrays.stream(offspringPopulationSizeValue))
            .toArray(
                size ->
                    (String[]) Array.newInstance(arguments.getClass().getComponentType(), size));

    for (Parameter<?> parameter : fixedParameterList) {
      parameter.parse(extendedArguments).check();
    }
    for (Parameter<?> parameter : configurableParameterList()) {
      parameter.parse(extendedArguments).check();
    }
  }

  protected Problem<PermutationSolution<Integer>> problem() {
    return ProblemFactory.loadProblem(problemNameParameter.value());
  }

  /**
   * Creates an instance of MOEA/D from the parsed parameters
   *
   * @return
   */
  public EvolutionaryAlgorithm<PermutationSolution<Integer>> create() {
    JMetalRandom.getInstance().setSeed(randomGeneratorSeedParameter.value());

    var problem = (PermutationProblem<PermutationSolution<Integer>>) problem();

    Archive<PermutationSolution<Integer>> archive = null;
    Evaluation<PermutationSolution<Integer>> evaluation;
    if (algorithmResultParameter.value().equals("externalArchive")) {
      externalArchiveParameter.setSize(populationSizeParameter.value());
      archive = externalArchiveParameter.getParameter();
      evaluation = new SequentialEvaluationWithArchive<>(problem, archive);
    } else {
      evaluation = new SequentialEvaluation<>(problem);
    }

    var initialSolutionsCreation =
        (SolutionsCreation<PermutationSolution<Integer>>)
            createInitialSolutionsParameter.getParameter(problem, populationSizeParameter.value());

    Termination termination =
        new TerminationByEvaluations(maximumNumberOfEvaluationsParameter.value());

    MutationParameter mutationParameter =
        (MutationParameter) variationParameter.findSpecificParameter("mutation");

    mutationParameter.addNonConfigurableParameter("permutationLength", problem.numberOfVariables());

    Neighborhood<PermutationSolution<Integer>> neighborhood = null;

    if (problem.numberOfObjectives() == 2) {
      neighborhood =
          new WeightVectorNeighborhood<>(
              populationSizeParameter.value(), neighborhoodSizeParameter.value());
    } else {
      try {
        neighborhood =
            new WeightVectorNeighborhood<>(
                populationSizeParameter.value(),
                problem.numberOfObjectives(),
                neighborhoodSizeParameter.value(),
                "resources/weightVectorFiles/moead");
      } catch (FileNotFoundException exception) {
        exception.printStackTrace();
      }
    }

    subProblemIdGeneratorParameter.sequenceLength(populationSizeParameter.value());
    var subProblemIdGenerator = subProblemIdGeneratorParameter.getParameter();

    selectionParameter.addNonConfigurableParameter("neighborhood", neighborhood);
    selectionParameter.addNonConfigurableParameter("subProblemIdGenerator", subProblemIdGenerator);
    variationParameter.addNonConfigurableParameter("subProblemIdGenerator", subProblemIdGenerator);

    var variation =
        (Variation<PermutationSolution<Integer>>)
            variationParameter.getPermutationSolutionParameter();

    var selection =
        (PopulationAndNeighborhoodSelection<PermutationSolution<Integer>>)
            selectionParameter.getParameter(variation.getMatingPoolSize(), null);

    int maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutionsParameter.value();

    boolean normalizeObjectives = Boolean.valueOf(normalizeObjectivesParameter.value());
    aggregationFunctionParameter.normalizedObjectives(normalizeObjectives);
    AggregationFunction aggregativeFunction = aggregationFunctionParameter.getParameter();
    var replacement =
        new MOEADReplacement<>(
            selection,
            (WeightVectorNeighborhood<PermutationSolution<Integer>>) neighborhood,
            aggregativeFunction,
            subProblemIdGenerator,
            maximumNumberOfReplacedSolutions,
                normalizeObjectives);

    class EvolutionaryAlgorithmWithArchive
        extends EvolutionaryAlgorithm<PermutationSolution<Integer>> {
      private Archive<PermutationSolution<Integer>> archive;

      /**
       * Constructor
       *
       * @param name Algorithm name
       * @param initialPopulationCreation
       * @param evaluation
       * @param termination
       * @param selection
       * @param variation
       * @param replacement
       */
      public EvolutionaryAlgorithmWithArchive(
          String name,
          SolutionsCreation<PermutationSolution<Integer>> initialPopulationCreation,
          Evaluation<PermutationSolution<Integer>> evaluation,
          Termination termination,
          Selection<PermutationSolution<Integer>> selection,
          Variation<PermutationSolution<Integer>> variation,
          Replacement<PermutationSolution<Integer>> replacement,
          Archive<PermutationSolution<Integer>> archive) {
        super(
            name,
            initialPopulationCreation,
            evaluation,
            termination,
            selection,
            variation,
            replacement);
        this.archive = archive;
      }

      @Override
      public List<PermutationSolution<Integer>> result() {
        return archive.solutions();
      }
    }

    if (algorithmResultParameter.value().equals("externalArchive")) {
      return new EvolutionaryAlgorithmWithArchive(
          "MOEAD",
          initialSolutionsCreation,
          evaluation,
          termination,
          selection,
          variation,
          replacement,
          archive);
    } else {
      return new EvolutionaryAlgorithm<>(
          "MOEAD",
          initialSolutionsCreation,
          evaluation,
          termination,
          selection,
          variation,
          replacement);
    }
  }

  public static void print(List<Parameter<?>> parameterList) {
    parameterList.forEach(System.out::println);
  }
}
