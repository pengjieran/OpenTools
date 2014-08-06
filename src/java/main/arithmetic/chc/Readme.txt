
Compiling:

  CHC now requires the following sources from the cvs repository:
      mlj/src/chc
      mlj/src/id3
      mlj/src/nb
      mlj/src/shared
      mlj/src/c45

  Compile CHC from the src directory with the command
  listed below.

      javac chc/*.java

Running:

   Run chc from the src directory with the following 
   command

      java chc.CHCDriver [datafile] [options]

  Datafile:

    The data file to be tested with CHC. ie. vote, anneal, iris.
    CHC uses the datafile system {.names, .all, .data, .test, .train, .val} 
    Make sure to include the path for the datafile if it is in another
    directory.

  Options:
  
      finalgeneration=# - CHC will terminate at the end of
                          that generation - 1.
      populationsize=# - the maximum number of Hypothesisi
                         spawned each generation.
      inducer=[inducer] - the inducer to be used when running chc.
                          Options are naive, id3, c4.5.
      bitmask=[mask] - mask is a string of 7 bits. 1 indicating on;
                                                   0 indicatinf off;
                       a description of the options controlled by the
                       bitmask is below in the Printing Options section
                       of this file in the order they appear. they
                       correspond to the CHC Printing options of the
                       MLJ-Options.file.

    Options can also be controlled from the MLJ-Options.file
    under the CHC options section

      finalgeneration #
      populationsize #
      inducer [inducer]
      FitnessDistribution #,#,#
    
    These are the same options as above. except FitnessDistribution
    which is used to implement the new outerloop fitness equation.
    The three numbers should all add to 1.0 and correspond to a, b, and
    c in the fitness equation.

Log Options

    CHC uses the WRAPPER loglevel in the MLJ-Options.file.

    loglevel                     output
       0                 only basic errors
       1                 prints basic generational information.
       2                 print out the process of chc.
       3                 internal working of chc plus non basic errors
       4                 print whole population each generation
      5-6                no change
       7                 debugging

    Inducers now run on the INDUCER loglevel in the
    MLJ-Options.file and the CatTestResults uses the CTR.
    If you are getting unwanted results try setting these
    loglevels to 0.

Printing Options

    They are in the MLJ-Options file.

      print_all_hypothesis:                   prints all the hypothesesi
                                              created durring this run.
      best_overall_hypotheses:                prints the top ten hypotheses
      average_fitness_per_generation:         prints the average fitness per
                                              generation
      best_Spawned_Hypothesis_of_generation:  prints the information of the
                                              best individual spawned in each
                                              generation
      best_overall_Hypothesis_of_generation:  will print the information of
                                              the best overall Hypothesis to
                                              this point in the data run  
      best_fitness_of_each_generation:        I forgot. nothing right now
                                              though. don't worry about it. 
      correct_vs_incorrect_instances:         will print the correctly
                                              classified instances vs the
                                              incorrectly classified instances.

Note:

  Cataclysmic mutation is automatically implemented when needed.
  It is used to fill any empty spots in the new population.
