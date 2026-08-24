# M14 – administratoriaus funkcionalumas

## Įgyvendinta

- Visų naudotojų ir jų rolių bei paskyros būsenų peržiūra.
- Naudotojo blokavimas ir atblokavimas, išsaugant jo istorinę informaciją.
- Apsauga nuo administratoriaus bandymo užblokuoti savo paskyrą.
- Užblokuotos paskyros prisijungimo atmetimas.
- Visų restoranų su savininkais peržiūra.
- Visų užsakymų su klientu, restoranu ir vairuotoju peržiūra.
- Visų atsiliepimų su autoriumi peržiūra.
- Naudotojui matomi autentifikacijos ir pasikartojančio vertinimo pranešimai
  pateikiami lietuviškai.

## Duomenų bazės migracija

Esamai MySQL schemai vieną kartą vykdomas `database/V2__add_user_active.sql`.
Naujai kuriamoje schemoje stulpelis jau įtrauktas į V1 failą.
