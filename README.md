# Book REST service
## Prepare
Create working database booksDB (may use script scripts/init.sh)
## Build
mvn package
## Launch
java -jar target/bookservice-0.0.1-SNAPSHOT.jar
## Test API
Go to http://localhost:8080/swagger-ui.html