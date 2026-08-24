# Maisto užsakymų ir pristatymo valdymo sistema

Java 21, Maven, Hibernate/JPA ir MySQL pagrindu sukurta kursinio darbo sistema.
Ji turi keturias roles: klientą, restorano savininką, vairuotoją ir administratorių.

## Paleidimas

1. Paleisti MySQL ir sukurti schemą vykdant
   `database/V1__create_food_delivery_schema.sql`.
2. Jei naudojama senesnė jau sukurta schema, vieną kartą vykdyti
   `database/V2__add_user_active.sql`.
3. Projekto šaknyje greta `pom.xml` sukurti neversijuojamą `db.properties` pagal
   `db.properties.example`.
4. Patikrinti projektą:

```bash
mvn clean test
```

5. IntelliJ paleisti `com.example.javakur.consoleCourseWork.StartNoGui`.

## Užsakymo eiga

```text
CREATED -> CONFIRMED -> PREPARING -> READY_FOR_PICKUP
READY_FOR_PICKUP -> PICKED_UP -> DELIVERING -> COMPLETED
```

Klientas sukuria kelių patiekalų užsakymą. Savininkas jį patvirtina ir paruošia.
Vairuotojas pats pasiima laisvą `READY_FOR_PICKUP` užsakymą. Baigus pristatymą,
klientas gali įvertinti restoraną ir vairuotoją, o vairuotojas – klientą.

## Rolės

- `CLIENT`: restoranai, krepšelis, užsakymai, žinutės ir atsiliepimai.
- `RESTAURANT_OWNER`: restoranai, meniu, užsakymai ir jų paruošimo būsenos.
- `DRIVER`: laisvi užsakymai, paėmimas ir pristatymo būsenos.
- `ADMIN`: naudotojų blokavimas, visų restoranų, užsakymų ir atsiliepimų peržiūra.

## Saugumas

- Slaptažodžiai saugomi kaip BCrypt maišos reikšmės.
- `db.properties` ignoruojamas Git ir negali būti keliamas į repozitoriją.
- Prieiga prie užsakymų tikrinama pagal rolę ir užsakymo dalyvį.
- Naudotojai blokuojami neištrinant istorinės informacijos.

Detali galutinė patikra aprašyta `M15_FINAL_ACCEPTANCE.md`.
