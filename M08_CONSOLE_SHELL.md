# M08 – Konsolės prisijungimas ir rolių meniu

## Tikslas

Paversti iki šiol sukurtą servisų sluoksnį paleidžiama konsolės programa su
prisijungimu, sesija, atsijungimu ir atskiru meniu kiekvienai rolei.

## Funkcionalumas

- `StartNoGui` paleidžia Hibernate ir konsolės valdiklį.
- Pradinis meniu leidžia prisijungti arba baigti darbą.
- Po sėkmingo prisijungimo parenkamas kliento, savininko, vairuotojo arba
  administratoriaus meniu.
- Atsijungus grįžtama į pradinį meniu.
- Neteisinga įvestis ir blogi prisijungimo duomenys neuždaro programos.
- Įvestis ir išvestis perduodamos per konstruktorių, todėl meniu galima testuoti
  be rankinio terminalo valdymo.

## Šio etapo riba

M08 sukuria navigacijos karkasą. Meniu punktai rodomi ir atpažįstami, o konkretūs
CRUD veiksmai bus jungiami M09, naudojant jau sukurtus servisus.

## Patikrinimas

- `ConsoleMenuTest` simuliuoja sėkmingą ir nesėkmingą prisijungimą.
- Testas patikrina, kad kiekviena iš keturių rolių turi atskirą meniu.
- GitHub Actions paleidžia visą Maven testų rinkinį.
