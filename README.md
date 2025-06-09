### Swagger API documentation available at `http://localhost:8080/swagger-ui/index.html`

## Application config requirements

### 1. Setup ENVs
- database_username
- database_password
- database_url

### 2. Create schema `db_changelog` in database

### 3. Make sure you have `Docker` installed and can run it without `sudo`, as it is required during tests for `testcontainers` library.


## Local environment
1. Run the `docker-compose -f docker/docker-compose.yml up --build`

## Production environment

1. create `db_changelog` schema in the database
2. `./mvnw clean install`
3. Run the `recruitment-task-app.jar` program passing correct env variables to the database
```
   ~/.jdks/openjdk-21.0.1/bin/java -Djava.net.preferIPv4Stack=true -jar recruitment-task-app.jar \
   --database_url=jdbc:postgresql://localhost:5432/recruitment-task \
   --database_username=postgres \
   --database_password=postgres
```
