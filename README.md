![GitHub release (latest by date)](https://img.shields.io/github/v/release/hmrc/pizza-tax-frontend) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/hmrc/pizza-tax-frontend) ![GitHub last commit](https://img.shields.io/github/last-commit/hmrc/pizza-tax-frontend)

# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)

## Step 01 - Create an initial journey model

In this step we define our initial journey model in `PizzaTaxJourneyModel` object, 
and test it with `PizzaTaxJourneyModelSpec`.

Initial model have only three states: `Start`, `HaveYouBeenHungryRecently`, `WorkInProgressDeadEnd` and three possible transitions: `start`, `askHaveYouBeenHungryRecently`,`submittedHaveYouBeenHungryRecently`.

## Project content

    .
    ├── app
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   └── PizzaTaxJourneyModel.scala
    │                   └── models
    │                       └── QuestionnaireAnswers.scala
    ├── project
    │   ├── build.properties
    │   ├── metals.sbt
    │   └── plugins.sbt
    ├── test
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   └── PizzaTaxJourneyModelSpec.scala
    │                   └── support
    │                       ├── DummyContext.scala
    │                       ├── InMemoryStore.scala
    │                       ├── JourneyModelMatchers.scala
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
