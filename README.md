# employee-management-api

This repository contains a Spring Boot RESTful API for managing employees, utilizing MongoDB as the database.

## Features
* Perform CRUD operations on employee records
* MongoDB database integration
* DTOs for request/response handling

## Prerequisites
* Java 21
* Maven 3+
* MongoDB (locally or via MongoDB Atlas)
* Postman for API testing

## Setup Instructions
1. Clone the Repository <br>
    ```sh
    git clone https://github.com/t-w-i-n-k-l-y/employee-management-api.git
    cd employee-management-api
    ```
2. Configure MongoDB - Update `application.properties` file based on your setup.
    ```sh
    spring.data.mongodb.uri=mongodb://localhost:27017/<your_database_name>
    ```
    For MongoDB Atlas
    ```sh 
    spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster0.mongodb.net/<your_database_name>?retryWrites=true&w=majority
    ```

3. Build and Run the Application
    ```sh
    mvn clean install
    mvn spring-boot:run
    ```

## API Endpoints

| Method | Endpoint                                                                | Description                                     |
|--------|-------------------------------------------------------------------------|-------------------------------------------------|
| GET    | `/api/employees`                                                        | Retrieve all employees                          |
| GET    | `/api/employees` (with `employeeId` query param)                        | Retrieve employee by `employeeID`               |
| GET    | `/api/employees/{id}`                                                   | Retrieve employee by path `id` (MongoDB _id)    |
| GET    | `/api/employees/search` (with `fullName` and `department` query params) | Search employees by `fullName` and `department` |
| POST   | `/api/employees`                                                        | Create a new employee                           |
| PUT    | `/api/employees/{id}`                                                   | Update an employee by `id`                      |
| DELETE | `/api/employees/{id}`                                                   | Delete an employee by `id`                      |

## API Request Payloads
You can import the Postman collection for ready-to-use API requests.
