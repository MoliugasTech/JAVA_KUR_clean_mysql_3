# M16 – minimalus JavaFX laboratorinis MVP

M16 sąmoningai apribotas iki LAB1/LAB2 įrodomo vertikalaus pjūvio. Grafinė
sąsaja naudoja jau stabilų servisų ir Hibernate sluoksnį; konsoliniai meniu bei
duomenų bazės schema nekeičiami.

## Formos ir kontroleriai

| FXML forma | Kontroleris | Paskirtis |
|---|---|---|
| `login-form.fxml` | `LoginFormController` | Desktop autentifikacija |
| `main-form.fxml` | `MainFormController` | Pagrindinis langas ir `TabPane` |
| `user-form.fxml` | `UserFormController` | Papildomas administratoriaus CRUD langas |

Visi valdomi formų elementai turi `fx:id`, sutampantį su kontrolerio `@FXML`
lauku. Formos yra standartiniai FXML failai ir gali būti atidaromos Gluon Scene
Builder.

## Įgyvendinta

- Desktop prisijungti gali tik `ADMIN` ir `RESTAURANT_OWNER`.
- `CLIENT` ir `DRIVER` nukreipiami į būsimą mobiliąją programėlę.
- Pagrindiniame lange naudojamas `TabPane` ir rolėmis ribota navigacija.
- Administratoriui atidaromas papildomas vartotojų valdymo langas.
- `TableView` rodo visus vartotojus iš MySQL.
- Paieška tikrina keturis požymius: vartotojo vardą, vardą, pavardę ir el. paštą.
- Galima sukurti visų keturių rolių vartotojus.
- Klientui dinamiškai rodomas pristatymo adresas, vairuotojui – transporto tipas.
- Galima redaguoti vartotojo duomenis ir pasirinktinai pakeisti slaptažodį.
- Šalinimo semantika realizuota saugiu paskyros blokavimu / atblokavimu,
  išsaugant užsakymų istoriją.
- Validacijos klaidos ir sėkmingi veiksmai rodomi JavaFX `Alert` dialogais.

## Paleidimas

```bash
mvn clean test
mvn javafx:run
```

Prieš paleidžiant turi veikti MySQL ir vietiniame `db.properties` turi būti
teisingi prisijungimo duomenys.

## Rankinė priėmimo patikra

1. Prisijungti kaip administratorius.
2. Atidaryti **Vartotojų valdymą**.
3. Patikrinti paiešką pagal keturis požymius.
4. Sukurti testinį klientą ir vairuotoją.
5. Redaguoti testinio vartotojo vardą arba el. paštą.
6. Užblokuoti vartotoją ir patikrinti, kad juo prisijungti nebegalima.
7. Atblokuoti vartotoją.
8. Prisijungti kaip restorano savininkas ir patikrinti, kad administratoriaus
   vartotojų valdymo mygtukas nerodomas.
9. Pabandyti prisijungti klientu arba vairuotoju ir patikrinti mobiliosios
   programėlės pranešimą.
