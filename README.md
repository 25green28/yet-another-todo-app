# Yet another To-Do app
Yet another to-do app made using React(Vite) and Spring Boot.

## Introduction & purpose
This app is made by myself. The idea was very simple - learn how to create a project connecting Spring Boot and React. I wrote every piece of code by myself, but I discovered some things like SSE by using AI tools. 

## Building progress
First, I made a simple backend, which I tested by making tests and then using Postman. After this, I decided that I'd like to write some frontend to present the effects of my work. I decided to use Vite + React. Why? Just because. I made one full-stack project with React in Next.js and I liked it. Here I only needed frontend, so I decided to go with Vite. During the development, I discovered that there was a problem with more than one device using this app at once. The data was inconsistent, so I went to AI to get advice, and I discovered a lightweight protocol: server -> client (SSE Server-Sent Events). 

## Running the application
To run the application you'll need Java, Maven, and Node.js. I wrote the project using the versions that I provided below. When you have the dependencies installed (I don't guarantee that the project will work on versions other than those provided below) you can run the application by using two terminals:
1. Go to the main catalog (project directory) in both terminals
2. In the first one write (terminal):

   ```cd viteAndReact```
   
   ```npm i```
   
   ```npm run dev```
3. In the second one (terminal):

   ```cd springBoot```
   
     ```./mvnw spring-boot:run```

### Used tools
```
WebStorm 2025.1.3 -> Vite and React
IntelliJ IDEA 2025.1.3 (Community Edition) -> Spring Boot
Postman v11.57.6 -> Check the API endpoints
Spring initializr -> To create the Spring Boot project
Brave & Firefox -> Checking the frontend UI
Node.js v20.18.0. -> To run the frontend server
Java 21.0.2 -> To run the backend server
Apache Maven 3.9.11 -> Complie the Spring Boot application from CLI 
```

### Additional info about database
Currently, I have set up a default support for my own locally running PostgreSQL server. If you don't want to configure your own, you can simply change the property in ```springBoot/src/main/resources/application.properties```  

from ```spring.profiles.active=postgres``` 

to ```spring.profiles.active=h2```

> [!WARNING]
> The H2 database is an in-memory database, which means that all data will be lost after the application restarts.

If you would like to configure your own PostgreSQL database, you can modify the properties in ```springBoot/src/main/resources/application-postgres.properties```

### (Maybe) Future ideas:
- Implement Spring Web Security
- Add Authentication/Authorization
- Make the user access only their collections
