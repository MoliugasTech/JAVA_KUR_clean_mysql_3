# M20 – Android aplikacijos pagrindas

Android aplikacija skirta tik klientams ir vairuotojams. Ji yra atskirame
\`android-app\` Gradle projekte, tačiau laikoma tame pačiame GitHub repozitoriume.

## Dabartinė struktūra

M20B aplikacija perkelta į dėstytojos laboratorinėje medžiagoje naudojamą
struktūrą:

- Java;
- XML Views ir \`ConstraintLayout\`;
- atskiros \`Activity\` klasės ir \`Intent\` navigacija;
- \`findViewById\`;
- \`ExecutorService\` ir pagrindinės gijos \`Handler\`;
- \`HttpURLConnection\` klasėje \`RestOperations\`;
- Gson JSON duomenims;
- tekstai \`strings.xml\`;
- \`network_security_config.xml\`.

\`RestOperations\` priima visus sėkmingus 2xx kodus, palaiko Bearer tokeną,
nuskaito serverio klaidų tekstą ir uždaro srautus. Tai būtina, nes registracija
grąžina \`201 Created\`, o šalinimas – \`204 No Content\`.

## Įgyvendinta

- realus \`POST /api/auth/login\`;
- kliento ir vairuotojo rolės atpažinimas;
- Bearer tokeno sesija;
- atskiros kliento ir vairuotojo pradžios \`Activity\`;
- realus \`POST /api/auth/logout\`;
- kliento ir vairuotojo registracija per \`POST /api/clients\` ir
  \`POST /api/drivers\`;
- įvedimo kontrolė ir klaidų pranešimai.

## Atidarymas ir paleidimas

Android Studio atidaryti aplanką \`JAVA_KUR_M07/android-app\`. Backend'ą
paleisti projekto šaknyje:

\`\`\`bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
\`\`\`

Prijungus Redmi telefoną USB laidu:

\`\`\`bash
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
\`\`\`

Aplikacija naudoja \`http://127.0.0.1:8080/\`, todėl lokalaus Wi-Fi IP įrašyti
nereikia.

## Patikrinimas

1. Atidaryti prisijungimo formą.
2. Atidaryti registracijos formą ir sukurti testinį klientą arba vairuotoją.
3. Prisijungti sukurta paskyra.
4. Patikrinti, kad atsidaro teisingos rolės langas.
5. Atsijungti ir patikrinti grįžimą į prisijungimą.

Automatiniai testai:

\`\`\`bash
cd android-app
./gradlew testDebugUnitTest
\`\`\`
