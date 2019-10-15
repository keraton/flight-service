# Flight-Service Acceptance Tests

Postman based acceptance tests combined with Newman CLI for CICD

## Getting Started

These instructions will help you run tests on your local machine using npm or directly on Postman. 
Available tests environments are `localhost` and `gcp`.

### Prerequisites

You will need nodejs installation: [download](https://nodejs.org/en/download/).  
Also, if you want to edit or run collections interactively you will need Postman: [download](https://www.getpostman.com/downloads/)

### Installing

With npm:

```
npm install
```

With Postman, import both collections and environments.

## Running the tests

Available tests are located in package.json (i.e. `npm run`)

### local testing

Application should be running, e.g.:

```
npm run local
```

With Postman, select `localhost` environment and start Runner on imported collections

### gcp testing

Application can be tested in GCP, e.g.:

```
npm run gcp
```

With Postman, select `gcp` environment and start Runner on imported collections

## Built With

* [Postman](https://www.getpostman.com/) - API environment
* [Newman](https://learning.getpostman.com/docs/postman/collection_runs/command_line_integration_with_newman/) - CLI for Postman
* [NodeJS](https://nodejs.org/en/) - Tests runtime