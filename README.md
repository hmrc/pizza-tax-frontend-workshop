![GitHub release (latest by date)](https://img.shields.io/github/v/release/hmrc/pizza-tax-frontend) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/hmrc/pizza-tax-frontend) ![GitHub last commit](https://img.shields.io/github/last-commit/hmrc/pizza-tax-frontend)

# pizza-tax-frontend workshop

An imaginary pizza tax service showcasing how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

If you are interested in starting a brand-new microservice you can generate it quickly from [template-play-frontend-fsm.g8](https://github.com/hmrc/template-play-frontend-fsm.g8).

## Workshop steps

- **00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)**
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- 02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)
- 03 [explore alternative model designs](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-03-alternative-model-design#readme)
- 04 [add state persistence layer](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-04-configure-state-persistence-layer#readme)
- 05 [start building a controller and views](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-05-start-building-a-controller-and-views#readme)

## Goal

The aim of the workshop is to build step-by-step a frontend microservice implementing the user flow presented on the chart:

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

## Empty project content

    .
    ├── project
    │   ├── build.properties
    │   └── plugins.sbt
    ├── LICENSE
    ├── README.md
    ├── build.sbt
    └── repository.yaml

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally


    sm --start AWESOME_STUBS AWESOME_STUBS_FRONTEND ASSETS_FRONTEND
    sbt run

It should then be listening on port 12345

    browse http://localhost:12345/pay-as-you-eat

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
