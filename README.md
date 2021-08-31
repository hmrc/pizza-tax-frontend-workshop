![GitHub release (latest by date)](https://img.shields.io/github/v/release/hmrc/pizza-tax-frontend) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/hmrc/pizza-tax-frontend) ![GitHub last commit](https://img.shields.io/github/last-commit/hmrc/pizza-tax-frontend)

# pizza-tax-frontend workshop

An imaginary pizza tax service demonstrating how to build a frontend microservice using [play-fsm](https://github.com/hmrc/play-fsm).


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
