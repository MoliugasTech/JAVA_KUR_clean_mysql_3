# M30 – galutinė kursinio versija

M30 užbaigia projektą po M29 desktop valdymo centro.

## Funkciniai užbaigimai

- restorano savininko JavaFX užsakymo lange pridėta pokalbio istorija;
- savininkas gali siųsti žinutę klientui ar vairuotojui;
- užbaigtas ir atmestas pokalbis automatiškai tampa tik skaitomas;
- administratorius gali pataisyti arba pašalinti netinkamą žinutę;
- pridėti tiksliniai savininko ir administratoriaus žinučių testai.

## Pateikimo paketas

- perrašytas pagrindinis `README.md`;
- pridėtas reikalavimų kontrolinis sąrašas;
- pridėtas 10–15 minučių demonstravimo scenarijus;
- galutiniai testai fiksuoja keturias roles ir būsenų minimumą.

## Galutinė patikra

```bash
mvn clean test
cd android-app
./gradlew --no-daemon :app:testDebugUnitTest
```

Po automatinių testų atliekama viena JavaFX savininko žinutės ir viena Android
gavėjo žinutės rankinė patikra. Naujos duomenų bazės migracijos M30 nereikia.
