# M17 – Spring Boot REST pagrindas

Šis etapas sukuria tik minimalų HTTP/JSON serverio pagrindą. Kliento ir
vairuotojo CRUD endpoint'ai bus realizuojami kitame etape, kai šis paleidimas
bus patikrintas lokaliai.

## Technologijos

- Java 21
- Spring Boot 3.4.13
- Spring Web
- Spring HATEOAS
- Spring Data JPA
- MySQL

Spring Boot naudoja integruotą Tomcat serverį, todėl atskiro Tomcat ar XAMPP
šiam projektui nereikia.

## Konfigūracija

REST aplikacija naudoja tą patį projekto šaknyje esantį, į Git nekeliamą
`db.properties` failą:

```properties
db.url=jdbc:mysql://localhost:3306/food_delivery
db.user=root
db.password=
```

Hibernate nustatytas į `validate`, todėl serveris tik patikrina jau sukurtą
schemą ir pats tyliai nekeičia lentelių.

## Paleidimas

```bash
mvn clean test
mvn spring-boot:run
```

Kai konsolėje matoma `Started RestApiApplication`, naršyklėje arba Postman
atidaryti:

```text
http://localhost:8080/api/health
```

Tikėtinas JSON:

```json
{
  "status": "UP",
  "service": "food-delivery-api"
}
```

Serveris sustabdomas terminale paspaudus `Control + C`.
