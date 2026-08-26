# M20 – Android aplikacijos pagrindas

Android aplikacija skirta tik klientams ir vairuotojams. Ji yra atskirame
`android-app` Gradle projekte, tačiau laikoma tame pačiame GitHub repozitoriume,
kad kursinį būtų paprasta pateikti ir versijuoti.

## M20 apimtis

- Kotlin ir Jetpack Compose Android projektas;
- realus `POST /api/auth/login` ryšys su Spring Boot backend'u;
- `Bearer` tokeno sesija programėlės veikimo metu;
- kliento ir vairuotojo rolės atpažinimas;
- atskiras rolės pradinis ekranas;
- realus `POST /api/auth/logout`;
- prisijungimo būsenų ir klaidų atvaizdavimas;
- ViewModel automatiniai testai ir atskiras Android CI.

Restoranų, krepšelio, užsakymų ir pristatymų ekranai bus realizuojami kituose
etapuose. M20 tikslas – patikrinti visą grandinę `Android → REST → MySQL`.

## Atidarymas

1. Įdiegti naujausią stabilią Android Studio versiją.
2. Pasirinkti **Open**.
3. Atidaryti ne visą Maven projektą, o aplanką `JAVA_KUR_M07/android-app`.
4. Palaukti, kol baigsis Gradle Sync.
5. Device Manager sukurti Android API 36 emuliatorių.

## Backend'o paleidimas

Kitame terminale, pagrindiniame projekto aplanke:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

Android emuliatorius Mac kompiuterį pasiekia adresu `10.0.2.2`, todėl debug
konfigūracijoje naudojamas `http://10.0.2.2:8080/`.

Naudojant fizinį telefoną reikės pakeisti `API_BASE_URL` į Mac kompiuterio
lokalaus tinklo IP adresą ir leisti ryšį macOS ugniasienėje.

## Patikrinimas

1. Paleisti Spring Boot backend'ą.
2. Android Studio paleisti `app` API 36 emuliatoriuje.
3. Prisijungti aktyvia kliento arba vairuotojo paskyra.
4. Patikrinti, kad atsidaro teisingos rolės pradinis ekranas.
5. Paspausti **Atsijungti** ir patikrinti grįžimą į prisijungimą.

Automatiniai Android testai:

```bash
cd android-app
./gradlew testDebugUnitTest
```

Naudojamas AGP 9.3, Gradle 9.5, API 36 ir Compose BOM `2026.08.00`.
