# Projekto paleidimo instrukcija

Šiame faile sudėtos dažniausiai reikalingos komandos. Visos komandos skirtos
macOS ir projekto aplankui `~/Downloads/JAVA_KUR_M07`.

## Kasdienis paleidimas

### 1. Atnaujinti projektą

```bash
cd ~/Downloads/JAVA_KUR_M07
git switch main
git pull --ff-only origin main
```

### 2. Pasirinkti JDK 24

Projektas kuriamas kaip Java 21 programa, tačiau lokaliems testams šiame
kompiuteryje naudojamas JDK 24. JDK 26 netinka Mockito testams.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Terminalas turi rodyti Java 24.

### 3. Paleisti MySQL

```bash
brew services start mysql
brew services list
```

Jeigu MySQL jau paleistas, pirmoji komanda nieko nesugadins.

### 4. Paleisti REST serverį

Pirmame terminalo lange:

```bash
cd ~/Downloads/JAVA_KUR_M07
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

Serveris veikia adresu `http://localhost:8080`. Šį terminalo langą reikia
palikti veikiantį. Serveris stabdomas paspaudus `Control + C`.

### 5. Prijungti Android telefoną

Antrame terminalo lange:

```bash
~/Library/Android/sdk/platform-tools/adb devices
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
```

Tada Android Studio:

1. atidaryti projekto aplanką `android-app`;
2. paspausti **Sync Project with Gradle Files**;
3. pasirinkti prijungtą telefoną;
4. paspausti **Run ▶**.

Android programėle gali naudotis klientas ir vairuotojas. REST serverio
terminalas turi likti paleistas.

### 6. Paleisti JavaFX programą

Trečiame terminalo lange:

```bash
cd ~/Downloads/JAVA_KUR_M07
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
mvn javafx:run
```

JavaFX programa skirta administratoriui ir restorano savininkui. Ji naudoja tą
pačią MySQL duomenų bazę kaip REST serveris ir Android programėlė.

## Testų paleidimas

### Backend ir JavaFX testai

```bash
cd ~/Downloads/JAVA_KUR_M07
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean test
```

Sėkmingas rezultatas baigiasi tekstu `BUILD SUCCESS`.

### Android testai

```bash
cd ~/Downloads/JAVA_KUR_M07/android-app
./gradlew --no-daemon :app:testDebugUnitTest
```

## Duomenų bazės konfigūracija

Projekto šaknyje turi būti lokalus failas `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/food_delivery?useSSL=false&serverTimezone=Europe/Vilnius&allowPublicKeyRetrieval=true
db.user=root
db.password=JUSU_MYSQL_SLAPTAZODIS
```

Šio failo negalima kelti į GitHub. Pirmą kartą paruošiant duomenų bazę MySQL
Workbench iš eilės vykdomi failai:

1. `database/V1__create_food_delivery_schema.sql`;
2. `database/V2__add_user_active.sql`;
3. `database/V3__add_order_management_fields.sql`.

Migracijos vykdomos tik vieną kartą.

## Naudingos Workbench užklausos

Parodyti visus vartotojus:

```sql
USE food_delivery;

SELECT id, username, role, active
FROM users
ORDER BY id;
```

Parodyti naujausius užsakymus:

```sql
SELECT id, status, client_id, restaurant_id, driver_id, total_price
FROM food_orders
ORDER BY id DESC;
```

Slaptažodžių Workbench parodyti negali, nes jie saugomi kaip BCrypt maišos.

## Dažniausios problemos

### Mockito klaida su Java 26

Jeigu rodoma `Mockito cannot mock this class`, tame pačiame terminale vykdyti:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean test
```

### Telefonas nepasiekia serverio

Patikrinti, ar serveris vis dar paleistas, ir pakartoti:

```bash
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
```

### Užimtas 8080 prievadas

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

Jeigu liko anksčiau paleistas serveris, grįžti į jo terminalą ir paspausti
`Control + C`, tada serverį paleisti iš naujo.

### Nepavyksta prisijungti prie MySQL

```bash
brew services list
```

Patikrinti, ar MySQL būsena yra `started`, ir ar `db.properties` įrašytas
teisingas MySQL slaptažodis.

## Greita demonstracijos seka

1. Paleisti MySQL.
2. Paleisti REST serverį.
3. Atlikti `adb reverse` ir paleisti Android programėlę.
4. Atskirame terminale paleisti JavaFX.
5. Demonstravimo eigai naudoti `FINAL_DEMO_SCRIPT.md`.
