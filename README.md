# Transfer Funds API

## Description

FundsTransfer API is a RESTful service for handling financial transactions, including creating accounts, transfers, deposits, and withdrawals. The API supports multiple operations essential for managing account and transaction data in a reliable and scalable way. The project is built with Spring Boot, and it integrates MongoDB for persistence and Moneta for currency and monetary operations.

## Features
### Account Management: Create and retrieve accounts with support for multiple currencies.
### Transaction Processing: Transfer funds, withdraw, and deposit amounts with concurrency-safe operations.
### Custom Exception Handling: Graceful handling of invalid operations like unsupported currencies and non-existent accounts.
### API Documentation: Detailed Swagger/OpenAPI documentation for easy API exploration and testing.

## Technology Stack
Java: Version 23 (using Lombok for code generation)
Spring Boot: For RESTful APIs, DI, and JPA (via MongoDB)
H2: in-memory database
Moneta (JSR-354): For handling currency and monetary amounts
JUnit & Mockito: For unit and integration testing
Swagger/OpenAPI: For API documentation

## Getting Started

### Installation

1. **Clone the repository**:
    ```sh
    git clone https://github.com/bilelneb13/funds-transfer-api.git
    ```

2. **Navigate to the project directory**:
    ```sh
    cd your-directory
    ```

3. **Build the project**:
    ```sh
    mvn clean install
    ```

4. **Run the application**:
    ```sh
    mvn spring-boot:run
    ```

## Usage

### Endpoints

The application provides various endpoints for user interaction, which are described under this url
http://localhost:8080/swagger-ui.html
