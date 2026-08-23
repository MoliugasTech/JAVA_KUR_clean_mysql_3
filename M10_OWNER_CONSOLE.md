# M10 – Restorano savininko konsolės scenarijus

## Tikslas

Leisti restorano savininkui per konsolę paruošti klientui realų restoraną ir
meniu bei valdyti restorano užsakymo dalį.

## Funkcionalumas

- Savininko registracija ir prisijungimas.
- Restorano sukūrimas su tipu, adresu ir darbo laiku.
- Tik savininkui priklausančių restoranų sąrašas.
- Patiekalo pridėjimas, kainos ir prieinamumo keitimas.
- Tik pasirinktam restoranui priklausančio meniu valdymas.
- Restorano užsakymų peržiūra.
- Būsenos `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP` ir `CANCELLED`.

## Saugumo taisyklės

- Savininkas negali matyti ar keisti kito savininko restorano.
- Kito restorano patiekalas negali būti redaguojamas.
- Savininkas negali nustatyti vairuotojo būsenų `PICKED_UP`, `DELIVERING` ar
  `COMPLETED`.
- Visi pasirinkimai konsolėje atliekami pagal matomą sąrašo numerį.

## Patikrinimas

- `OwnerConsoleServiceTest` tikrina nuosavybę, meniu ir būsenų teises.
- `StartNoGui` leidžia scenarijų patikrinti su realia MySQL baze.
- GitHub Actions paleidžia visą Maven testų rinkinį.
