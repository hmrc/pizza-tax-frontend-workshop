# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating step-by-step how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- 02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)
- 03 [explore alternative model designs](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-03-alternative-model-design#readme)
- 04 [add state persistence layer](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-04-configure-state-persistence-layer#readme)
- **05 [start building a controller and views](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-05-start-building-a-controller-and-views#readme)**

## Step 05 - Start building a controller and views

In this step we start getting a feeling of a final application by adding controller and views.

### Things to learn:


## Project content after changes

Newly added files are marked with (+) , modified with (*) , removed with (x) .

    .
    ├── app
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── config
    │                   │   └── (*) AppConfig.scala
    │                   ├── connectors
    │                   │   └── (+) FrontendAuthConnector.scala
    │                   ├── controllers
    │                   │   ├── (+) AuthActions.scala
    │                   │   ├── (+) BaseJourneyController.scala
    │                   │   └── (+) PizzaTaxJourneyController.scala
    │                   ├── journeys
    │                   │   ├── PizzaTaxJourneyModel.scala
    │                   │   ├── PizzaTaxJourneyModelAlt1.scala
    │                   │   ├── PizzaTaxJourneyModelAlt1Formats.scala
    │                   │   └── PizzaTaxJourneyModelFormats.scala
    │                   ├── models
    │                   │   ├── BasicPizzaAllowanceLimits.scala
    │                   │   ├── CanValidate.scala
    │                   │   ├── HungerSolution.scala
    │                   │   ├── ITRole.scala
    │                   │   ├── PizzaAllowance.scala
    │                   │   ├── PizzaOrdersDeclaration.scala
    │                   │   ├── PizzaTaxAssessmentRequest.scala
    │                   │   ├── PizzaTaxAssessmentResponse.scala
    │                   │   └── QuestionnaireAnswers.scala
    │                   ├── repository
    │                   │   ├── CacheRepository.scala
    │                   │   └── JourneyCacheRepository.scala
    │                   ├── services
    │                   │   ├── JourneyCache.scala
    │                   │   ├── MongoDBCachedJourneyService.scala
    │                   │   ├── PizzaTaxJourneyAlt1Service.scala
    │                   │   └── PizzaTaxJourneyService.scala
    │                   ├── utils
    │                   │   ├── (+) CallOps.scala
    │                   │   ├── EnumerationFormats.scala
    │                   │   └── OptionOps.scala
    │                   ├── views
    │                   │   └── (+) Views.scala
    │                   └── FrontendModule.scala
    ├── conf
    │   ├── app.routes
    │   ├── application-json-logger.xml
    │   ├── application.conf
    │   ├── logback.xml
    │   └── prod.routes
    ├── it
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── controllers
    │                   │   ├── (+) AuthActionsISpec.scala
    │                   │   └── (+) PizzaTaxJourneyISpec.scala
    │                   ├── services
    │                   │   ├── MongoDBCachedJourneyServiceISpec.scala
    │                   │   ├── MongoDBCachedPizzaTaxJourneyAlt1ServiceSpec.scala
    │                   │   └── MongoDBCachedPizzaTaxJourneyServiceSpec.scala
    │                   ├── stubs
    │                   │   ├── (+) AuthStubs.scala
    │                   │   └── (+) DataStreamStubs.scala
    │                   └── support
    │                       ├── AppISpec.scala
    │                       ├── BaseISpec.scala
    │                       ├── (+) MetricsTestSupport.scala
    │                       ├── Port.scala
    │                       ├── (+) ServerISpec.scala
    │                       ├── TestAppConfig.scala
    │                       ├── (+) TestJourneyService.scala
    │                       ├── UnitSpec.scala
    │                       └── WireMockSupport.scala
    ├── project
    │   ├── build.properties
    │   └── plugins.sbt
    ├── test
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   ├── PizzaTaxJourneyModelAlt1FormatsSpec.scala
    │                   │   ├── PizzaTaxJourneyModelAlt1Spec.scala
    │                   │   ├── PizzaTaxJourneyModelFormatsSpec.scala
    │                   │   └── PizzaTaxJourneyModelSpec.scala
    │                   ├── support
    │                   │   ├── DummyContext.scala
    │                   │   ├── InMemoryStore.scala
    │                   │   ├── JourneyModelSpec.scala
    │                   │   ├── JsonFormatTest.scala
    │                   │   └── TestJourneyService.scala
    │                   └── utils
    │                       ├── CallOpsSpec.scala
    │                       ├── EnumerationFormatsSpec.scala
    │                       └── OptionOpsSpec.scala
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
