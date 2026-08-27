# Maisto užsakymų ir pristatymo valdymo sistema

Baigtas Java kursinio darbo projektas su keturiomis rolėmis:

- klientas ir vairuotojas naudoja Android programėlę;
- restorano savininkas ir administratorius naudoja JavaFX desktop programą;
- Spring Boot REST API jungia Android su ta pačia MySQL duomenų baze.

Technologijos: Java 21, Maven, JavaFX/FXML, Spring Boot, Hibernate/JPA, MySQL,
Android Java/XML, JUnit 5 ir Mockito.

> Greitam kasdieniam paleidimui naudokite
> [PALEIDIMO_INSTRUKCIJA.md](PALEIDIMO_INSTRUKCIJA.md).

## 1. Reikalavimai kompiuteriui

- MySQL 8.x;
- Maven;
- Android Studio ir Android SDK;
- JDK 24 rekomenduojamas lokaliems testams.

Projektas kompiliuojamas į Java 21. JDK 26 šiuo metu nerekomenduojamas testams,
nes inline Mockito agentas su juo negali modifikuoti kai kurių klasių.

macOS terminale:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 2. Duomenų bazė

MySQL Workbench iš eilės įvykdyti:

1. `database/V1__create_food_delivery_schema.sql`;
2. `database/V2__add_user_active.sql`;
3. `database/V3__add_order_management_fields.sql`.

Jeigu schema jau buvo naudojama šiame projekte, V1 kartoti nereikia. V2 ir V3
vykdomos tik vieną kartą.

Projekto šaknyje sukurti `db.properties` pagal `db.properties.example`:

```properties
db.url=jdbc:mysql://localhost:3306/food_delivery?useSSL=false&serverTimezone=Europe/Vilnius&allowPublicKeyRetrieval=true
db.user=root
db.password=JUSU_MYSQL_SLAPTAZODIS
```

`db.properties` yra ignoruojamas Git ir negali būti įkeliamas į GitHub.

## 3. Testai

```bash
mvn clean test
```

Android testai:

```bash
cd android-app
./gradlew --no-daemon :app:testDebugUnitTest
```

## 4. JavaFX desktop programa

Projekto šaknyje:

```bash
mvn javafx:run
```

Prisijungti gali tik `ADMIN` arba `RESTAURANT_OWNER`.

Administratorius valdo vartotojus, visus užsakymus, restoranų patiekalus,
žinutes ir atsiliepimus. Restorano savininkas valdo savo restoranus, meniu,
užsakymus, vairuotojo paėmimo leidimą ir pokalbius.

## 5. REST API ir Android

Atskirame terminale:

```bash
mvn spring-boot:run
```

Serveris veikia `http://localhost:8080`.

Prijungus fizinį Android telefoną:

```bash
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
```

Android Studio atidaryti `android-app`, atlikti Gradle Sync ir paleisti
`app`. Android prisijungti gali tik klientas arba vairuotojas.

## 6. Užsakymo eiga

```text
CREATED -> CONFIRMED -> PREPARING -> READY_FOR_PICKUP
READY_FOR_PICKUP -> PICKED_UP -> DELIVERING -> COMPLETED
CREATED / CONFIRMED / PREPARING -> CANCELLED (su priežastimi)
```

Kainos koeficientas apskaičiuojamas serveryje. Užbaigus užsakymą klientui vieną
kartą skiriami lojalumo taškai. Atliktiems ir atmestiems užsakymams žinutės lieka
matomos, bet naujų siųsti negalima.

## 7. Testinių paskyrų patikra

Duomenų bazėje paskyras galima pamatyti:

```sql
USE food_delivery;
SELECT id, username, role, active FROM users ORDER BY id;
```

Slaptažodžiai saugomi tik kaip BCrypt `password_hash`, todėl Workbench jų
atkurti negali. Naudoti projekto demonstracijai sukurtas paskyras.

## 8. Galutiniai dokumentai

- `PALEIDIMO_INSTRUKCIJA.md` – kasdienės paleidimo komandos ir klaidų sprendimai;
- `COURSE_REQUIREMENTS_CHECKLIST.md` – reikalavimų atsekamumas;
- `FINAL_DEMO_SCRIPT.md` – trumpas atsiskaitymo scenarijus;
- `M30_FINAL_RELEASE.md` – galutinio etapo turinys.
