# Getting Started

**IMPORTANT: Do not send pull requests to this repository. This is a template repository and is not used for grading. Any pull requests will be closed and ignored.**

## Introduction

If you are reading this, you are probably have received this project as a coding challenge. Please read the instructions
carefully and follow the steps below to get started.

## Setup

### Pre-requisites

To run the application you would require:

- [Java](https://www.azul.com/downloads/#zulu)
- [Temporal](https://docs.temporal.io/cli#install)
- [Docker](https://docs.docker.com/get-docker/)
- [Stripe API Keys](https://stripe.com/docs/keys)

### On macOS:

First, you need to install Java 21 or later. You can download it from [Azul](https://www.azul.com/downloads/#zulu) or
use [SDKMAN](https://sdkman.io/).

```sh
brew install --cask zulu21
```


or visit [Temporal Installation](https://docs.temporal.io/cli#install) for more information.

You can install Docker using Homebrew

```sh
brew install docker
```

or visit [Docker Installation](https://docs.docker.com/get-docker/) for more information.

### Other platforms

Please check the official documentation for the installation of Java, Temporal, and Docker for your platform.

### Stripe API Keys

Sign up for a Stripe account and get your API keys from the [Stripe Dashboard](https://dashboard.stripe.com/apikeys).
Then in `application.properties` file add the following line with your secret key.

```properties
stripe.api-key=sk_test_51J3j
```

## Run

Spring Docker Compose will handle the running of temporal server and database of the project
Run the application using the following command or using your IDE.

```sh
./gradlew bootRun
```

### Other commands

### Running test cases
Integration test cases are written using test containers which spins up the database container automatically
for the project and deletes it once the test cases are executed by making efficient use of resources

Prerequisites

To run test cases it requires temporal server which can be executed using docker compose command

```sh
docker compose up -d
```

Once the docker containers are up and running we can use the following command to execute the clean build

```sh
./gradlew clean build
```

#### Lint
To run lint checks, use the following command

```sh
./gradlew sonarlintMain
```

#### Code Formatting
To format the code, use the following command

```sh
./gradlew spotlessApply
```

## Implementation Approach

### Workflow Implementation
this project includes the implementation files for workflow defined which follows
the required operations
- It initiates the account creation in the system
- calls the Stripe payment service and creates the customer and saves the customer information in the system
### Activity Implementation
This project includes the implementation of the Activity which defines the steps to be followed in the workflow
- It includes the save Account step which saves the account of the user in the database
- It includes the create Payment Account which creates the customer on Stripe
## Testing
This project integrates TestContainers to test the sever with integration test which tests the code end to end
with production grade setup which ensures the working of the flow
- It Spins up the containers required for the test cases and executes the test cases in the order mentioned
- It verifies the API operations performed and ensures we get the correct result

## Guides
The following guides illustrate how to use some features concretely:

- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Temporal Quick Start](https://docs.temporal.io/docs/quick-start)
- [Temporal Java SDK Quick Guide](https://docs.temporal.io/dev-guide/java)
- [Stripe Quick Start](https://stripe.com/docs/quickstart)
- [Stripe Java SDK](https://stripe.com/docs/api/java)

### Code Walkthrough Video
Please refer to this video for detailed explanation
- Project Walkthrough Video: [`Click here`](https://drive.google.com/file/d/1K4QUMItoBllWYPgJEoZjMioK3uCGhTL-/view)

### Screenshots
Screenshots for the running walkthrough is added in screenshots directory

### Github Action Integration
This project includes github actions to automate the clean build process and verifies the code being pushed
is up and running does not contains any issues

### Docker file Integration

This project contains Dockerfile which creates the image for the project which can be deployed
to server

### Docker Compose support

This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

- postgres: [`postgres:latest`](https://hub.docker.com/_/postgres)
- temporal: [`temporalio/auto-setup:latest`](https://hub.docker.com/r/temporalio/auto-setup)

Please review the tags of the used images and set them to the same as you're running in production.
