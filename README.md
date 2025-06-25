# Airbnb UI QA Automation Project

A Selenium-based Java automation suite for end-to-end Airbnb search and reservation flows.
This repository provides a production-grade, scalable UI automation framework for Airbnb, 
built with **Java 17**, **Selenium WebDriver**, and **TestNG**.
It emphasizes modularity, configurability, and maintainability 
to support large test suites and distributed execution.

___

## Project Structure

```

airbnb-ui-automation/
├── src/
│   ├── main/
│   ├─ java/com/airbnb/
│   │   ├─ config/                        # Configuration loader
│   │   ├─ pages/                         # Page Objects
│   │   ├─ utils/                         # Wait, Time, URL utilities
│   │   └─ model/                         # Data models
│   └── test/
│       ├── java/com/airbnb/tests/        # TestNG classes
│       ├── resources/
│       │   ├── test-properties/          # Default environment settings
│       │   └── simplelogger.properties   # Default logger settings
│       └── test-suites
├── Dockerfile                            # Builds test runner image
├── docker-compose.yml                    # Selenium Grid + test runner
├── pom.xml                               # Maven build and dependency management
└── README.md                             # This document

```
___

## Prerequisites

- **Java 17** SDK
- **Maven** 3.8+
- **Docker** & **Docker Compose** (for containerized runs)
- Chrome browser and matching ChromeDriver
- Network access to target environments (Airbnb staging or production)

___

## Configuration

### test.properties

Located in `src/test/resources`, keys include:

```properties
# Test data
app.url=https://www.airbnb.com
search.location=Tel Aviv
search.checkin=7/25/2025
search.checkout=7/30/2025
search.adults=2
search.children=1
search.infants=0
search.pets=0

# Timeout settings for WaitUtil
wait.timeout.seconds=30
wait.polling.millis=500
```

Override via:

- System properties, can be configured in `docker-compose.yml` file (`-Dsearch.location="Tel Aviv"`)
- Environment variables (e.g. `export search.location="Tel Aviv"`)

### SimpleLogger

We use **SLF4J SimpleLogger** for lightweight logging .

Adjust packages log levels using `simplelogger.properties` on the classpath:

```
org.slf4j.simpleLogger.log.com.airbnb.pages=info
org.slf4j.simpleLogger.log.com.airbnb.tests=debug
```

Adjust levels per package. See [SimpleLogger configuration](https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html).

___

## Running Tests

### Local

```
mvn clean verify
```

### Docker
The `docker-compose.yml` maps env vars and volumes for logs and artifacts.

```
docker compose up --build
```

___

## Reporting & Logs

- **Console logs** courtesy of SimpleLogger
- To change log level at runtime, edit `simplelogger.properties`, rebuild, or pass:

```
org.slf4j.simpleLogger.log.com.airbnb.pages=debug
org.slf4j.simpleLogger.log.com.airbnb.tests=debug
```

___

## Out of Scope & optional Enhancements

- CI/CD pipeline configurations
- Advanced logging frameworks or external log aggregation
- Linting
- Code style $ Formatting enforcement
- Support for browsers other than Chrome
- Parallel execution strategies beyond default TestNG suites
- Performance Monitoring

___