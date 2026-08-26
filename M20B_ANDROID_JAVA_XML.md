# M20B – Android Java ir XML Views

Šis checkpoint'as nekeičia Spring Boot, REST ar MySQL logikos. Jis pakeičia tik
Android pateikimo sluoksnį, kad projekto struktūra atitiktų dėstytojos
laboratorinę medžiagą.

| Dėstytojos medžiaga | Projekto realizacija |
|---|---|
| Java | \`MainActivity.java\`, registracijos ir rolių Activity |
| XML formos | \`res/layout/activity_*.xml\` |
| ConstraintLayout | kiekvienos formos šakninis arba tiesioginis konteineris |
| Activity ir Intent | login → registracija / kliento langas / vairuotojo langas |
| Executor ir Handler | HTTP vykdomas ne pagrindinėje gijoje, UI atnaujinamas pagrindinėje |
| RestOperations | saugesnis \`HttpURLConnection\` variantas su 2xx ir Bearer |
| Constants | vienoje vietoje laikomi API URL |
| Gson | request ir response JSON |
| strings.xml | naudotojui rodomi tekstai nėra hardcode'inti |
| network security | leidžiamas lokalus HTTP į \`127.0.0.1\` |

Formas galima atidaryti Android Studio **Design** arba **Split** režimu ir
toliau koreguoti drag-and-drop principu. Svarbu nepervadinti esamų ID
nepakeitus atitinkamo \`findViewById\` Java klasėje.
