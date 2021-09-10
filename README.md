# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- **01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)**
- 02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)
- 03 [explore alternative model designs](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-03-alternative-model-design#readme)
- 04 [add state persistence layer](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-04-configure-state-persistence-layer#readme)
- 05 [start building a controller and views](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-05-start-building-a-controller-and-views#readme)

## Step 01 - Create an initial journey model

In this step we build our initial journey model in `PizzaTaxJourneyModel` object, 
and we show how to unit test it in `PizzaTaxJourneyModelSpec` specification.

```
                     Start
                       │
                       ▼
            HaveYouBeenHungryRecently
                       │
            ┌──────────┴──────────┐
         no │                     │ yes
            ▼                     ▼
WorkInProgressDeadEnd     WorkInProgressDeadEnd
```

The key principle of `fsm` is that one can move from state to state only by applying the transition. Because of this it is required for all states and transtions to be defined inside the single object inheriting from `JourneyModel`.

By convention, we define states inside an intermediary object `State` and transitions inside `Transitions`. This makes it easier to reference them later.

Our initial model will have only three states: `Start`, `HaveYouBeenHungryRecently`, `WorkInProgressDeadEnd` and two possible transitions between them: `start`,`submittedHaveYouBeenHungryRecently`.

### Things to learn:

1. state can be defined either as a case object or a case class,
1. every state must be a subtype of the `State` type,
1. transitions are ordinary partial functions constructed using `Transition {...}` factory,
1. if a transition case is not supported then the transition will not happen
1. one can use `WorkInProgressDeadEnd` to plumb loose ends in the journey while developing
1. unit testing requires a drop-in of a few support classes
1. unit testing the journey is as easy as writing: ```given(State_A).when(transition).thenGoes(State_B)```

## Project content after changes

Newly added files are marked with (+), modified with (*), removed with (x).

    .
    ├── app
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   └── (+) PizzaTaxJourneyModel.scala
    ├── project
    │   ├── build.properties
    │   └── plugins.sbt
    ├── test
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   └── (+) PizzaTaxJourneyModelSpec.scala
    │                   └── support
    │                       ├── (+) DummyContext.scala
    │                       ├── (+) InMemoryStore.scala
    │                       ├── (+) JourneyModelSpec.scala
    │                       └── (+) TestJourneyService.scala
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
