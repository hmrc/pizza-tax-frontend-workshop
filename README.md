# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating step-by-step how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- 02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)
- **03 [explore alternative model designs](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-03-alternative-model-design#readme)**
- 04 [add state persistence layer](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-04-configure-state-persistence-layer#readme)
- 05 [start building a controller and views](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-05-start-building-a-controller-and-views#readme)

## Step 03 - Explore alternative designs of the journey model

In this step we have a look at the alternative design of the journey model `PizzaTaxJourneyModelAlt1`, where all the answers are explictly remembered in the dedicated record `QuestionnaireAnswers`. This is to allow outer layers (controller) to populate input fields when travelling back and forth the journey. 

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

We use helper trait `HasAnswers` to mark states holding `answers` property. 

To help keep questionnaire entity always valid we implement `isValid` method and instead of `goto` we use  `gotoIfValid` helper method to progress to the new state.

### Things to learn:

- The journey should keep enough information to support target application flow, nothing more, nothing less.

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
    │                   │   ├── BasicPizzaAllowanceLimits.scala
    │                   │   ├── (+) CanValidate.scala
    │                   │   ├── HungerSolution.scala
    │                   │   ├── ITRole.scala
    │                   │   ├── PizzaAllowance.scala
    │                   │   ├── PizzaOrdersDeclaration.scala
    │                   │   ├── PizzaTaxAssessmentRequest.scala
    │                   │   ├── PizzaTaxAssessmentResponse.scala
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
