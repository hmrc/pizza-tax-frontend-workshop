# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating step-by-step how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).

## Workshop steps

- 00 [start with an empty repository](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/master#readme)
- 01 [create an initial journey model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-01-create-a-journey#readme)
- 02 [further elaborate the model](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-02-extend-journey-model#readme)
- 03 [explore alternative model designs](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-03-alternative-model-design#readme)
- **04 [add state persistence layer](https://github.com/hmrc/pizza-tax-frontend-workshop/tree/step-04-configure-state-persistence-layer#readme)**

## Step 04 - Add state persistence layer

In this step we add and test components required to persist the journey state in MongoDB.

### Things to learn:

- Each journey model needs a concrete `JourneyService` instance to be useful to the final application,
- in a web application like our frontend the state has to be persisted between HTTP requests so we use specialized `PersistentJourneyService` interface,
- how to persist the state is not a concern of `play-fsm` itself, each application have to decide what would work best,
- in our service we build state persistence mechanism based on the `CacheRepository` interface from the `hmrc-mongo` library,
- since our serialization is JSON-based we have to define `Format[State]` typeclass instance, this is where` JsonStateFormats[State]` helps,
- a generic trait `JourneyCache` is responsible for orchestrating all cache read-write operations in a locally sequential manner for each unique `journeyId` key,
- an abstract `MongoDBCachedJourneyService` class binds `PersistentJourneyService` together with `JourneyCache` and adds encrypted serialization capability to the mix,
- finally, we end with two concrete classes `MongoDBCachedPizzaTaxJourneyService` and `MongoDBCachedPizzaTaxJourneyAlt1Service`, one for each journey model.

## Project content after changes

Newly added files are marked with (+) , modified with (*) , removed with (x) .

    .
    ├── app
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── config
    │                   │   └── (+) AppConfig.scala
    │                   ├── journeys
    │                   │   ├── PizzaTaxJourneyModel.scala
    │                   │   ├── PizzaTaxJourneyModelAlt1.scala
    │                   │   ├── (+) PizzaTaxJourneyModelAlt1Formats.scala
    │                   │   └── (+) PizzaTaxJourneyModelFormats.scala
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
    │                   │   ├── (+) CacheRepository.scala
    │                   │   └── (+) JourneyCacheRepository.scala
    │                   ├── services
    │                   │   ├── (+) JourneyCache.scala
    │                   │   ├── (+) MongoDBCachedJourneyService.scala
    │                   │   └── (+) PizzaTaxJourneyService.scala
    │                   ├── utils
    │                   │   ├── EnumerationFormats.scala
    │                   │   └── OptionOps.scala
    │                   └── (+) FrontendModule.scala
    ├── conf
    │   ├── (+) app.routes
    │   ├── (+) application-json-logger.xml
    │   ├── (+) application.conf
    │   ├── (+) logback.xml
    │   └── (+) prod.routes
    ├── it
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── services
    │                   │   ├── (+) MongoDBCachedJourneyServiceISpec.scala
    │                   │   ├── (+) MongoDBCachedPizzaTaxJourneyAlt1ServiceSpec.scala
    │                   │   └── (+) MongoDBCachedPizzaTaxJourneyServiceSpec.scala
    │                   └── support
    │                       ├── (+) AppISpec.scala
    │                       ├── (+) BaseISpec.scala
    │                       ├── (+) Port.scala
    │                       ├── (+) TestAppConfig.scala
    │                       ├── (+) UnitSpec.scala
    │                       └── (+) WireMockSupport.scala
    ├── project
    │   ├── build.properties    
    │   └── plugins.sbt
    ├── test
    │   └── uk
    │       └── gov
    │           └── hmrc
    │               └── pizzatax
    │                   ├── journeys
    │                   │   ├── PizzaTaxJourneyModelAlt1Spec.scala
    │                   │   ├── (+) PizzaTaxJourneyModelAlt1FormatsSpec.scala
    │                   │   ├── PizzaTaxJourneyModelSpec.scala
    │                   │   └── (+) PizzaTaxJourneyModelFormatsSpec.scala
    │                   ├── support
    │                   │   ├── DummyContext.scala
    │                   │   ├── InMemoryStore.scala
    │                   │   ├── JourneyModelSpec.scala
    │                   │   ├── JsonFormatTest.scala
    │                   │   └── TestJourneyService.scala
    │                   └── utils
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
