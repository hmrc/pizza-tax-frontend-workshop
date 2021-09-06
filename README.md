# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating step-by-step how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- **02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)**

## Step 02 - Extend journey model and explore alternatives

In this step we elaborate the model by adding new states and transitions, to match given user flow diagram:

                        ┌─────────┐
                        │  Start  │
                        └────┬────┘
                             │
                             ▼
                 HaveYouBeenHungryRecently
                             │
                  ┌────no────┴────yes───┐
                  │                     ▼
                  │              WhatYouDidToAddressHunger
                  │                     │
                  │ ┌──other────────────┴───┐
                  │ │                       │
                  ▼ ▼                 HungerSolution
        DidYouOrderPizzaAnyway        == OrderPizza
                  │                         │
                  ├───yes─────────────────┐ │
                  │                       ▼ ▼
                  │              HowManyPizzasDidYouOrder
                  │                         │
                  no                ┌───L───┴───H────┐
                  │                 │                ▼
                  │                 │    AreYouEligibleForSpecialAllowance
                  │                 │                │
                  │                 │      ┌──other──┴─ITWorker──┐
                  │                 │      │                     ▼
                  │                 │      │              WhatIsYourITRole
                  │                 │      │                     │
                  │                 │      │      ┌──────────────┘
                  │                 │      │      │
                  │                 ▼      ▼      ▼
                  │              QuestionnaireSummary
                  │                        │               ┌─────────────┐
                  │                        ├──calculate───►│ Backend API │
                  │                        │               └─────────────┘
    ┌─────────────▼────────┐    ┌──────────▼─────────────┐
    │NotEligibleForPizzaTax│    │TaxStatementConfirmation│
    └──────────────────────┘    └────────────────────────┘

In the `PizzaTaxJourneyModelAlt1` we explore an alternative design of the journey model where all answers are explicitly remembered in the `QuestionnaireAnswers` object in optional fields. We let questionaire states extend the helper trait `HasAnswers`. To keep questionnaire entity always valid we implement `isValid` method and instead of `goto` we use  `gotoIfValid` to progress to the new state.

The `HungerSolution`, `PizzaAllowance` and `ITRole` traits and objects model enumeration of user input options. 

The `BasicPizzaAllowanceLimits` is an example of parametrising the model with an external configuration.

Abstract type `PizzaTaxAssessmentAPI` is an example of modeling dependency of an external API call.

### Things to learn:

- Each journey model has its own types of `State` and `Transition`,
- States can reference external classes, e.g. `QuestionnaireAnswers`,
- One can declare transition as a value, but one can also decalre it as  a function taking parameters representing external configuration or an external API function.
- There are multiple ways of encoding similar journey model, depending on the amount of information we want to keep. 

## Project content after changes

Newly added files are marked with (+) , modified with (*) , removed with (x) .

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
    │                   │   ├── (+) BasicPizzaAllowanceLimits.scala
    │                   │   ├── (+) CanValidate.scala
    │                   │   ├── (+) HungerSolution.scala
    │                   │   ├── (+) ITRole.scala
    │                   │   ├── (+) PizzaAllowance.scala
    │                   │   ├── (+) PizzaOrdersDeclaration.scala
    │                   │   ├── (+) PizzaTaxAssessmentRequest.scala
    │                   │   ├── (+) PizzaTaxAssessmentResponse.scala
    │                   │   └── (+) QuestionnaireAnswers.scala
    │                   └── utils
    │                       └── OptionOps.scala
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
    │                   ├── support
    │                   │   ├── DummyContext.scala
    │                   │   ├── InMemoryStore.scala
    │                   │   ├── JourneyModelSpec.scala
    │                   │   └── TestJourneyService.scala
    │                   └── utils
    │                       └── (+) OptionOpsSpec.scala
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
