# M05 – Atsiliepimai po užsakymo

## Tikslas

Leisti užsakymo dalyviams palikti patikimus atsiliepimus tik tada, kai pristatymas
tikrai baigtas.

## Kas ką gali vertinti

- Užsakymo klientas vertina restoraną.
- Užsakymo klientas vertina priskirtą vairuotoją.
- Priskirtas vairuotojas vertina užsakymo klientą.

## Taisyklės

- Vertinti galima tik `COMPLETED` užsakymą.
- Vertintojas privalo būti konkretaus užsakymo dalyvis.
- Balas privalo būti nuo 1 iki 5.
- Komentaras neprivalomas, jo riba – 2000 simbolių.
- Tas pats autorius tam pačiam užsakymui gali pateikti tik vieną konkretaus tipo
  atsiliepimą.
- Restorano atsiliepime užpildomas `restaurant`, o vairuotojo arba kliento
  atsiliepime – `reviewedUser`.

## Patikrinimas

- `ReviewServiceTest` tikrina leidžiamus ir draudžiamus scenarijus.
- `MysqlReviewSmoke` patikrina trijų tipų atsiliepimų išsaugojimą realioje MySQL bazėje.
- GitHub Actions automatiškai paleidžia visus Maven testus.
