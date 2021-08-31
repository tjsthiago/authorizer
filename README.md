# Tecnical and Architetural decisions

To be simple and focus on business rules implementations I chose to use only Java without any framework, Junit to automated tests, and Gson to handle the conversion from JSON as String to Object.

I used the Specification Pattern to keep business rules implementation simple and respect the single responsibility principle. 

There is a class called **"AuthorizerSpecificationsBuilder"** that controls what Specifications the application used to validate operations.

Each Specification handle with only one business rule.

The **"Application"** class is the entry point of the application and if we need to apply a different number of specifications to validate operations we can instantiate the **"Authorizer"** class with another **"SpecificationsBuilder"** implementation that can have a different number and kind of specifications set in the **"specifications"** set. With this strategy, we achieve good flexibility in the application with no big changes.


# About tests

The **"Application Test"** class contain the tests used to validate business rules.


# Requirements to build the application

The application was developed using Java 8 and I used a maven to build the application.

In the application root folder, you can use the command below to build the application.

```
mvn package
```


# How to run the application

After building the project with "mvn package" you can use the command below to run the application.

```
cat ./src/main/resources/operations | java -jar ./target/authorizer.jar
```

***Note that you need be in the application root folder to run this command.***

To keep easy test the application running the ***authorizer.jar*** I let different files on the ***authorizer\src\main\resources*** folder. Each file contains a different kind of operations that can be used to validate different business rules. 

To run the application with a different list of operations you can use the command below.

```
cat ./src/main/resources/<file-name> | java -jar ./target/authorizer.jar
```


