# Kursinio reikalavimų kontrolinis sąrašas

| Reikalavimas | Įgyvendinimas | Pagrindinė vieta |
|---|---|---|
| 4 naudotojų tipai ir paveldėjimas | `Admin`, `RestaurantOwner`, `Client`, `Driver` paveldi `User` | `model/` |
| Prisijungimas ir BCrypt | Rolės tikrinamos sesijoje ir REST tokenuose, slaptažodžiai maišomi | `AuthenticationService`, `PasswordUtil` |
| Platformų atskyrimas | JavaFX priima ADMIN/OWNER, Android REST – CLIENT/DRIVER | GUI ir REST autorizacijos testai |
| Administratoriaus vartotojų CRUD | Kūrimas, peržiūra, redagavimas ir loginis šalinimas blokuojant | `UserFormController` |
| Administratoriaus sistemos prieiga | Užsakymai, patiekalai, žinutės, atsiliepimai | `AdminSystemFormController` |
| 4 naudotojų paieškos kriterijai | Username, vardas, pavardė, el. paštas | `UserFormController` |
| Užsakymų filtrai | Būsena, data, klientas; adminui ir restoranas | Owner/Admin JavaFX kontroleriai |
| Savininko restoranai ir meniu | Restorano kūrimas/redagavimas, patiekalų CRUD ir paslėpimas | `OwnerCatalogFormController` |
| Savininko užsakymų valdymas | Priėmimas, ruošimas, atmetimas su priežastimi, patiekalo šalinimas | `OwnerOrdersFormController` |
| Vairuotojo ribojimas | Savininkas blokuoja paėmimą; DB užraktas saugo nuo lenktynių | `DriverOrderRestService`, `OrderService` |
| Savininko žinutės | Peržiūra ir siuntimas, be redagavimo | M30 Owner Orders pokalbis |
| Kliento Android katalogas | Restoranai, aprašymai, vieno restorano krepšelis ir suma | Android Client Activities |
| Kliento užsakymai | Istorija, detalės, žinutės, atmetimo priežastis | Android Client Order Activities |
| Vairuotojo Android srautas | Laisvi užsakymai, paėmimas, būsenos ir žinutės | Android Driver Activities |
| Atsiliepimai | Klientas vertina restoraną/vairuotoją, vairuotojas klientą, be dublikatų | Review REST ir dialogai |
| Bent 5 būsenos | 7 būsenos nuo `CREATED` iki `COMPLETED` ir `CANCELLED` | `OrderStatus`, `OrderService` |
| Dinaminė kaina | 1,20 piko metu, 0,90 kitu metu | `TimeBasedPricingService` |
| Lojalumo sistema | 1 taškas už pilną galutinės kainos eurą, skiriamas vieną kartą | Užbaigimo servisas |
| Validacija | Tuščias/mišrus krepšelis, kiekiai, kainos, būsenos ir prieigos ribojamos serveryje | Servisų ir REST testai |
| MySQL schema | Versijuotos V1–V3 SQL migracijos, Hibernate `validate` | `database/` |

## Sąmoningi domeno sprendimai

- Istoriją turintis naudotojas „šalinamas“ jį blokuojant, kad nelūžtų užsakymų
  ir atsiliepimų ryšiai.
- Istorijoje panaudotas patiekalas paslepiamas su `available=false`; fiziškai
  šalinami tik nenaudoti patiekalai.
- Aktyvaus užsakymo administratorius nešalina. Fiziškai pašalinti galima tik
  `COMPLETED` arba `CANCELLED` užsakymą kartu su priklausomais duomenimis.
- Kainodaros ir prieigos taisyklės laikomos serveryje, ne Android programėlėje.
