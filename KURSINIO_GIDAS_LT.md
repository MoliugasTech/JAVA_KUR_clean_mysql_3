# Kursinio darbo gidas: ką darome, kaip veikia kodas ir kaip jį paaiškinti

> Šis failas yra gyvas projekto konspektas. Jis pildomas po kiekvieno etapo.
> Tikslas – kad projekto autorė galėtų ne tik paleisti programą, bet ir savo
> žodžiais paaiškinti architektūrą, svarbiausias klases, duomenų kelią ir
> priimtus sprendimus.

## 1. Projekto esmė vienu sakiniu

Kuriama maisto užsakymų ir pristatymo sistema, kurioje administratorius ir
restorano savininkas dirba JavaFX desktop programoje, o klientas ir vairuotojas
– Android programėlėje; visų naudotojų duomenys saugomi vienoje MySQL duomenų
bazėje.

## 2. Keturi naudotojų tipai

| Rolė | Sąsaja | Pagrindiniai veiksmai |
|---|---|---|
| Klientas | Android | Registruojasi, mato restoranus ir meniu, kuria užsakymą, seka būseną, rašo žinutes, palieka atsiliepimus |
| Vairuotojas | Android | Registruojasi, mato laisvus užsakymus, pasiima pristatymą, keičia būseną, rašo žinutes, palieka atsiliepimus |
| Restorano savininkas | JavaFX desktop | Valdo restoraną ir patiekalus, priima bei filtruoja savo užsakymus, rašo žinutes |
| Administratorius | JavaFX desktop | Turi bendrą sistemos prieigą, CRUD ir filtravimą vartotojams, užsakymams, patiekalams bei žinutėms |

Svarbu: klientas ir vairuotojas neturi naudotis desktop programa. Administratorius
ir restorano savininkas neturi naudotis Android programėle. Šį atskyrimą
nurodo pati kursinio užduotis.

## 3. Visa sistemos architektūra

Duomenų kelias yra toks:

1. Naudotojas paspaudžia mygtuką Android arba JavaFX formoje.
2. Sąsajos klasė surenka įvestus duomenis.
3. Android atveju duomenys JSON formatu siunčiami į Spring Boot REST API.
4. REST kontroleris priima HTTP užklausą.
5. Servisas patikrina verslo taisykles ir teises.
6. Repository per JPA/Hibernate skaito arba keičia MySQL duomenis.
7. Rezultatas grąžinamas atgal: objektas → DTO → JSON → Android ekranas.

Trumpai:

```text
Android XML ekranas
    → Activity
    → RestOperations / HTTP
    → Spring REST Controller
    → Service
    → Repository
    → Hibernate / JPA
    → MySQL
```

JavaFX desktop dalis gali naudoti tuos pačius servisus tiesiogiai, nes JavaFX ir
Hibernate yra tame pačiame Java projekte:

```text
FXML forma → JavaFX Controller → Service → Repository → Hibernate / JPA → MySQL
```

## 4. Kodėl Android nesijungia tiesiai prie MySQL

Android programėlėje negalima laikyti MySQL prisijungimo vardo ir slaptažodžio.
Jeigu jie būtų APK faile, juos būtų galima ištraukti ir prisijungti prie visos
duomenų bazės. Be to, tiesioginis DB ryšys nepatogus teisių, validacijos ir
verslo taisyklių valdymui.

Todėl Android bendrauja tik su REST API. API nusprendžia:

- ar naudotojas prisijungęs;
- kokia jo rolė;
- kokius duomenis jis gali matyti;
- ar įvesti duomenys tinkami;
- kokią DB operaciją leidžiama vykdyti.

Tai yra saugumo ir architektūros riba tarp mobilios programos ir DB.

## 5. Pagrindinės technologijos paprastai

### Java, JDK, Maven ir Gradle

Java yra programavimo kalba. JDK turi kompiliatorių ir paleidimo aplinką.
Backend naudoja Java 21. Maven ir `pom.xml` valdo backend bibliotekas bei testus.
Android turi atskirą `android-app` Gradle projektą; Gradle valdo Android SDK,
bibliotekas, kompiliavimą ir testus.

### JPA ir Hibernate

JPA yra taisyklių ir anotacijų specifikacija Java objektams susieti su DB.
Hibernate yra konkretus JPA įgyvendinimas.

- `@Entity` – klasė saugoma DB lentelėje;
- `@Id` – pirminis raktas;
- `@OneToMany` / `@ManyToOne` – objektų ir lentelių ryšiai;
- Hibernate Java veiksmus paverčia SQL užklausomis.

### Spring Boot REST ir JSON

Spring Boot paleidžia HTTP serverį. `@RestController` priima užklausas:

- `@GetMapping` – skaitymas;
- `@PostMapping` – kūrimas;
- `@PutMapping` – atnaujinimas;
- `@DeleteMapping` – šalinimas.

JSON yra tekstinis formatas, kuriuo Android ir backend apsikeičia duomenimis:

```json
{
  "username": "test_client",
  "password": "test_password"
}
```

### JavaFX, FXML ir SceneBuilder

JavaFX yra desktop GUI technologija. FXML aprašo lango elementus. SceneBuilder
leidžia FXML redaguoti vizualiai.

- FXML yra View – išvaizda;
- Controller klasė yra valdymo logika;
- `fx:id` susieja FXML elementą su kontrolerio `@FXML` lauku;
- `onAction` arba listener'is susieja mygtuką su metodu.

### Android XML Views ir Activity

Android XML faile aprašoma ekrano išvaizda. `Activity` yra Java klasė, valdanti
tą ekraną.

- `android:id="@+id/loginField"` sukuria elemento ID;
- `findViewById(R.id.loginField)` suranda elementą Java klasėje;
- `setOnClickListener` nurodo mygtuko veiksmą;
- `Intent` atidaro kitą Activity.

### Executor ir Handler

Android tinklo užklausos negalima vykdyti pagrindinėje UI gijoje:

- `ExecutorService` vykdo HTTP darbą foninėje gijoje;
- `Handler(Looper.getMainLooper())` grąžina rezultatą į UI giją;
- UI gijoje keičiami `TextView`, `Button` ir `ProgressBar`.

### Bearer tokenas

Po prisijungimo serveris išduoda atsitiktinį tokeną. Apsaugotose užklausose:

```http
Authorization: Bearer TOKENAS
```

Serveris pagal tokeną nustato naudotoją ir rolę. Slaptažodžio nereikia siųsti
su kiekviena užklausa.

## 6. Backend sluoksniai

### Model – `src/main/java/com/example/javakur/model`

- `User` – bendra naudotojo bazinė klasė;
- `Client`, `Driver`, `RestaurantOwner`, `Admin` – paveldėtojai;
- `Restaurant` – restoranas;
- `MenuItem` – patiekalas;
- `FoodOrder` – užsakymas;
- `OrderItem` – užsakymo eilutė ir kiekis;
- `OrderMessage` – žinutė;
- `Review` – įvertinimas.

Paveldėjimas mažina pasikartojimą: bendri naudotojų laukai yra `User`, o
specifiniai – konkrečioje rolės klasėje.

### Repository

Repository atsakingas už duomenų gavimą ir išsaugojimą. Spring Data
`JpaRepository` automatiškai suteikia bazines CRUD operacijas.

### Service

Servisas įgyvendina verslo taisykles: teises, validaciją, būsenų keitimą,
kainos perskaičiavimą ir ryšių tarp objektų kontrolę.

### REST Controller

Kontroleris gauna request, perduoda darbą servisui, rezultatą paverčia DTO ir
grąžina HTTP statusą su JSON. Kontroleris neturi pats rašyti SQL.

### DTO

DTO naudojamas, kad API negrąžintų slaptažodžio hash, vidinių laukų ir visos
entity ryšių struktūros. DTO tiksliai nurodo mobiliai programai leidžiamus laukus.

## 7. Android prisijungimas – M20B

Svarbiausi failai:

- `activity_main.xml`;
- `MainActivity.java`;
- `Constants.java`;
- `RestOperations.java`;
- `LoginResponse.java`;
- `SessionManager.java`;
- `MobileRole.java`.

### XML ir Java susiejimas

`activity_main.xml` turi `loginField`, `passwordField`, `loginButton`,
`registrationButton`, `loginErrorText` ir `loginProgress` ID.

```java
setContentView(R.layout.activity_main);
usernameField = findViewById(R.id.loginField);
loginButton.setOnClickListener(this::validateUser);
```

Pirma eilutė užkrauna XML. Antra suranda lauką. Trečia mygtuką susieja su
`validateUser` metodu.

### `validateUser` žingsniai

1. Paimami vartotojo vardas ir slaptažodis.
2. Patikrinama, ar jie netušti.
3. Gson suformuoja JSON.
4. Executor foninėje gijoje siunčia POST į `/api/auth/login`.
5. Atsakymas paverčiamas `LoginResponse`.
6. `MobileRole` priima tik CLIENT arba DRIVER.
7. `SessionManager` išsaugo tokeną, ID, username ir rolę.
8. `Intent` atidaro reikiamos rolės Activity.

### `Constants` ir telefono ryšys

URL laikomi vienoje vietoje. Fizinio telefono testui naudojamas
`http://127.0.0.1:8080/`, o komanda:

```bash
adb reverse tcp:8080 tcp:8080
```

nukreipia telefono 8080 portą į Mac 8080 portą.

### `RestOperations`

Dėstytojos minimalus pavyzdys sėkme laiko tik `200`. Mūsų API naudoja ir
`201 Created`, ir `204 No Content`, todėl `isSuccessful()` priima visus 2xx.
Klasė taip pat prideda Bearer tokeną, skaito klaidos body, turi timeout,
uždaro srautus ir atjungia `HttpURLConnection`.

### Sesijos saugumas

`SessionManager` su `SharedPreferences` saugo tokeną, ID, username ir rolę.
Slaptažodis telefone nesaugomas. Bandant mobiliame variante prisijungti adminui
ar savininkui, jo tokenas panaikinamas ir rolės langas neatidaromas.

## 8. Android registracija – M20B

`activity_registration.xml` ir `RegistrationActivity.java` leidžia pasirinkti
klientą arba vairuotoją. Klientui rodomas pristatymo adresas, vairuotojui –
transporto priemonės `Spinner`.

`createAccount`:

1. surenka laukus;
2. atlieka įvesties kontrolę;
3. suformuoja JSON;
4. klientui siunčia `POST /api/clients`;
5. vairuotojui siunčia `POST /api/drivers`;
6. priima `201 Created`;
7. parodo pranešimą ir grįžta į prisijungimą.

Serveris slaptažodį paverčia BCrypt hash. Android hash negauna.

## 9. Restoranų katalogo REST API – M21A

Endpoint'ai:

- `GET /api/catalog/restaurants`;
- `GET /api/catalog/restaurants/{restaurantId}/menu-items`.

Failai:

- `ClientCatalogController`;
- `ClientCatalogService`;
- `CatalogRestaurantRepository`;
- `CatalogMenuItemRepository`;
- `ClientCatalogDtos`.

### Užklausos kelias

1. `ApiTokenAuthenticationFilter` perskaito Bearer tokeną.
2. `@PreAuthorize` leidžia CLIENT arba ADMIN.
3. Controller kviečia servisą.
4. Servisas kviečia repository.
5. Hibernate gauna duomenis iš MySQL.
6. Entity paverčiama DTO.
7. Spring DTO paverčia JSON.

Patiekalų repository filtruoja pagal restorano ID ir `available = true`.

Statusai:

- be tokeno → `401 Unauthorized`;
- vairuotojui → `403 Forbidden`;
- nerastam restoranui → `404 Not Found`;
- sėkmingam sąrašui → `200 OK`.

`401` reiškia „nežinome, kas tu“. `403` – „žinome, bet tavo rolė negali“.

## 10. Kas jau padaryta

- [x] Modeliai ir naudotojų paveldėjimas.
- [x] JPA/Hibernate ir MySQL.
- [x] Bazinis CRUD ir repository sluoksnis.
- [x] BCrypt slaptažodžių hash.
- [x] Keturių rolių autentifikacija ir autorizacija.
- [x] Užsakymo, žinučių ir atsiliepimų servisų pagrindas.
- [x] Veikiantis konsolinis visų rolių variantas.
- [x] JavaFX prisijungimo ir admin vartotojų valdymo karkasas.
- [x] Spring Boot REST pagrindas.
- [x] Kliento ir vairuotojo profilio REST CRUD.
- [x] Bearer tokenai ir rolėmis saugomi endpoint'ai.
- [x] Android Java/XML login, registracija, rolės langas ir logout.
- [x] Restoranų ir prieinamų patiekalų REST skaitymas.
- [x] Maven ir Android GitHub Actions testai.

## 11. Kas liko – 8 pagrindiniai blokai

### 1. M21B – Android restoranų ir patiekalų UI

- restoranų sąrašas ir informacija;
- paspaudus restoraną – patiekalai;
- patiekalo pavadinimas, aprašymas ir kaina;
- loading, tuščio sąrašo ir klaidos būsenos.

### 2. M22 – kliento krepšelis ir užsakymo sukūrimas

- pridėti patiekalą ir kiekį;
- vieno restorano taisyklė;
- pašalinti ir perskaičiuoti sumą;
- REST užsakymo sukūrimas;
- galutinė kaina.

### 3. M23 – kliento savitarna

- savo užsakymai ir būsenos;
- profilio read/update/delete;
- žinutės ir atlikto užsakymo tik skaitymas;
- atsiliepimai restoranui ir vairuotojui.

### 4. M24 – vairuotojo Android funkcijos

- laisvi užsakymai;
- užsakymo pasiėmimas;
- pristatymo būsenos ir `COMPLETED`;
- istorija ir profilis;
- žinutės ir atsiliepimai apie klientą.

### 5. M25 – restorano savininko JavaFX

- restorano informacija ir patiekalų CRUD;
- savo užsakymų lentelė ir filtrai;
- užsakymo būsenos;
- patiekalo šalinimas ir kainos perskaičiavimas;
- žinutės ir kliento vertinimas.

### 6. M26 – administratoriaus JavaFX

- pilnas vartotojų, užsakymų, patiekalų ir žinučių CRUD;
- paieška bent pagal 4 vartotojo požymius;
- užsakymų filtravimas, įskaitant restoraną;
- lentelės, dinaminiai pasirinkimai, dialogai ir validacija.

### 7. M27 – specialios verslo taisyklės

- kainos modifikavimas pagal laiką;
- bonus taškai;
- galutinis teisių ir filtrų auditas;
- atlikto užsakymo susirašinėjimo blokavimas;
- CRUD proporcijų patikra visoms klasėms.

### 8. M28 – galutinis atsiskaitymo paruošimas

- Android → REST → MySQL ir JavaFX → MySQL E2E;
- klaidų ir tuščių būsenų patikra;
- UML ir panaudos atvejų diagramos;
- laboratorinių reikalavimų matrica ir ataskaita;
- paleidimo bei demonstravimo scenarijus;
- galimi dėstytojos klausimai.

Tai maždaug 12–16 mažų PR, nes dideli blokai skaidomi į atskirus API ir UI
checkpoint'us.

## 12. Laboratorinių reikalavimų būsena

### LAB1

| Reikalavimas | Būsena |
|---|---|
| Klasių diagrama ir klasės | Yra, galutinai atnaujinsime M28 |
| CRUD objektams | Backend pagrindas yra, visų objektų GUI – M25–M27 |
| Pagrindinis, papildomas langas, dialogas | JavaFX karkasas yra, galutinė patikra M25–M26 |
| Meniu arba tab'ai | JavaFX karkase yra |
| Standartiniai elementai | Yra |
| Lentelės ir dinaminiai pasirinkimai | Vartotojų lentelė yra, plėsime M25–M26 |
| Įvesties kontrolė | Yra servisų ir dalies GUI lygiu, bus audituojama |
| CRUD per GUI | Iš dalies; pagrindinis M25–M26 tikslas |

### LAB2

MySQL ir Hibernate ryšys veikia. Backend duomenų gavimas, kūrimas, atnaujinimas
ir šalinimas veikia. Likęs darbas – visas reikiamas operacijas pateikti JavaFX GUI.

### REST laboratorinis

- [x] Veikiantis web servisas.
- [x] JSON atsakymai.
- [x] Parametrų ir request body perdavimas.
- [x] Kliento ir vairuotojo profilio CRUD endpoint'ai.
- [~] Visas mobilus kursinio funkcionalumas per REST – M21–M24.

## 13. Kaip paaiškinti projektą per atsiskaitymą

Trumpa versija:

> Sistema turi keturias paveldėjimu paremtas roles. JavaFX desktop dalimi
> naudojasi administratorius ir restorano savininkas, Android – klientas ir
> vairuotojas. Android tiesiogiai prie MySQL nesijungia: JSON užklausomis
> kreipiasi į Spring Boot REST API. API autentifikuoja Bearer tokenu, tikrina
> rolę, kviečia service ir repository, o Hibernate dirba su MySQL.
> Slaptažodžiai saugomi tik BCrypt hash pavidalu.

Kodėl sluoksniai?

> Controller valdo HTTP, service – verslo taisykles, repository – duomenų
> prieigą, o View/Activity – UI. Taip lengviau testuoti ir tą pačią logiką
> naudoti keliose sąsajose.

Kodėl DTO?

> Kad API negrąžintų slaptažodžio hash, vidinių ryšių ar nereikalingų laukų.

Kaip apsaugotos rolės?

> Tokenas siunčiamas Authorization antraštėje. Filtras nustato naudotoją, o
> `@PreAuthorize` patikrina rolės teisę į endpoint'ą.

## 14. Kaip mokytis iš gido

Po kiekvieno etapo:

1. Perskaityti naujo etapo skyrių.
2. Atsidaryti minimus failus IDE.
3. Savo žodžiais papasakoti kelią nuo mygtuko iki DB ir atgal.
4. Paleisti rankinį scenarijų.
5. Neaiškų metodą pažymėti ir paklausti.

Nereikia mintinai mokėti kiekvienos Java eilutės. Reikia suprasti:

- kuri klasė už ką atsakinga;
- kas ką iškviečia;
- kokie duomenys perduodami;
- kur tikrinamos teisės ir validacija;
- kodėl pasirinktas toks sprendimas.

## 15. Tolimesnio pildymo taisyklė

Kiekviename PR bus atnaujintas šis failas: nauji ekranai ir ID, endpoint'ai,
request/response pavyzdžiai, duomenų kelias, verslo taisyklės, rankinis testas
ir galimi atsiskaitymo klausimai. Taip dokumentacija visada atitiks kodą.
