# Implement this API

#### In this assessment you will be tasked with filling out the functionality of different methods that will be listed further down.

These methods will require some level of API interactions with Mock Employee API at http://localhost:8112/api/v1/employee.

Please keep the following in mind when doing this assessment:
* clean coding practices
* test driven development
* logging
* scalability

### Endpoints to implement

_See `com.reliaquest.api.controller.IEmployeeController` for details._

getAllEmployees()

    output - list of employees
    description - this should return all employees

getEmployeesByNameSearch(...)

    path input - name fragment
    output - list of employees
    description - this should return all employees whose name contains or matches the string input provided

getEmployeeById(...)

    path input - employee ID
    output - employee
    description - this should return a single employee

getHighestSalaryOfEmployees()

    output - integer of the highest salary
    description - this should return a single integer indicating the highest salary of amongst all employees

getTop10HighestEarningEmployeeNames()

    output - list of employees
    description - this should return a list of the top 10 employees based off of their salaries

createEmployee(...)

    body input - attributes necessary to create an employee
    output - employee
    description - this should return a single employee, if created, otherwise error

deleteEmployeeById(...)

    path input - employee ID
    output - name of the employee
    description - this should delete the employee with specified id given, otherwise error

### Testing
Please include proper integration and/or unit tests.





# Employee Management Microservice

This is a Spring Boot-based microservice that manages employee data. It integrates with a **mock upstream API** using **WebClient**, adds resilience with **Resilience4j Retry**, and leverages **Redis** for caching to improve performance.

---

## Features

- Search and retrieve employee records
- In-memory mock API with rate-limiting simulation
- Resilience4j Retry integration for external calls
- Redis-backed caching for high performance
- Well-structured unit tests using Mockito & JUnit
- Global exception handling with `@RestControllerAdvice`

---

## Tech Stack

| Layer        | Technology                     |
|--------------|--------------------------------|
| Backend      | Spring Boot                    |
| HTTP Client  | Spring WebClient               |
| Caching      | Redis                          |
| Retry Logic  | Resilience4j                   |
| Mock API     | Spring Boot + DataFaker        |
| Testing      | JUnit 5, Mockito               |
| Language     | Java 17+                       |

---

## Getting Started

### Prerequisites

- Java 17+
- Redis installed locally
- Gradle
- IntelliJ IDEA

---

### Install Redis on macOS using brew

# Install Redis
`brew install redis`

# Start Redis server
`brew services start redis`


Start **Server** Spring Boot application.
`./gradlew server:bootRun`

Start **Api** Spring Boot application.
`./gradlew api:bootRun`

Run **Tests** in the **Api** Spring Boot application.
`./gradlew test`
