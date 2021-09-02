# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating step-by-step how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- 02 [further extend journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)

## Goals

- Model the "selection from the list" question step.
- Explore alternative encodings of the journey model

## Step 02 - Extend journey model and explore alternatives

In this step we extend the model by adding additional steps, mainly `WhatYouDidToAddressHunger`, `DidYouOrderPizzaAnyway` and `NotEligibleForPizzaTax`. 
We also connect new states to the previous ones by new set of transitions: `submittedWhatYouDidToAddressHunger` and `submittedDidYouOrderPizzaAnyway`.

In the `PizzaTaxJourneyModelAlt1` we explore an alternative design of the journey model where all answers are explicitly remembered in the `QuestionnaireAnswers` object. To make common `answer` field available easily we let states extend the helper trait `HasAnswers`.

The `HungerSolution` trait and object model enumeration of options. 

### Things to learn:

- Each journey model has its own types of `State` and `Transition`,
- States can reference external classes, e.g. `QuestionnaireAnswers`,
- There are multiple ways of encoding similar journey model, depending on the amount of information we want to keep. 

## Project content after changes

Newly added files are marked with (+), modified with (*), removed with (x).

    .
    ├── app
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   ├── (*) PizzaTaxJourneyModel.scala
    │                   │   └── (+) PizzaTaxJourneyModelAlt1.scala
    │                   ├── models
    │                   │   ├── (+) HungerSolution.scala
    │                   │   └── (+) QuestionnaireAnswers.scala
    │                   └── utils
    │                       └── (+) OptionOps.scala
    ├── project
    │   ├── build.properties
    │   └── plugins.sbt
    ├── test
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   ├── (+) PizzaTaxJourneyModelAlt1Spec.scala
    │                   │   └── (*) PizzaTaxJourneyModelSpec.scala
    │                   └── support
    │                       ├── DummyContext.scala
    │                       ├── InMemoryStore.scala
    │                       ├── JourneyModelSpec.scala
    │                       └── TestJourneyService.scala
    ├── LICENSE
    ├── README.md
    ├── build.sbt
    └── repository.yaml

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sbt run

It should then be listening on port 12345

    browse http://localhost:12345/pay-as-you-eat

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
