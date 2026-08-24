# M13 – užsakymų žinutės ir atsiliepimai konsolėje

## Funkcionalumas

- Klientas, restorano savininkas ir priskirtas vairuotojas gali matyti konkretaus
  užsakymo pokalbį ir siųsti žinutes, kol užsakymas nebaigtas.
- Visi užsakymo dalyviai gali matyti su juo susijusius atsiliepimus.
- Klientas po užsakymo užbaigimo gali atskirai įvertinti restoraną ir vairuotoją.
- Priskirtas vairuotojas po užsakymo užbaigimo gali įvertinti klientą.
- Restorano savininkas negali kurti atsiliepimo, nes tokio vertinimo tipo nėra
  kursinio darbo modelyje.

## Architektūra

`OrderInteractionService` yra bendras panaudojimo atvejis būsimai GUI ir REST
sąsajai. `OrderInteractionMenu` atsakingas tik už konsolinį dialogą. Dalyvio,
rolės, užsakymo būsenos, pasikartojančio vertinimo ir teksto validacijos lieka
servisų bei repozitorijų sluoksniuose.
