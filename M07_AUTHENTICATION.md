# M07 – Prisijungimas ir rolių teisės

## Tikslas

Saugiai atpažinti vartotoją pagal prisijungimo duomenis ir perduoti konsolės
programai tik minimalią informaciją apie aktyvią sesiją.

## Funkcionalumas

- Prisijungimas pagal vartotojo vardą ir slaptažodį.
- Slaptažodis tikrinamas su BCrypt; atviras slaptažodis nesaugomas.
- Sesijoje laikomi tik vartotojo ID, vardas ir rolė.
- Palaikomos `ADMIN`, `CLIENT`, `DRIVER` ir `RESTAURANT_OWNER` rolės.
- Galima reikalauti vienos arba kelių leidžiamų rolių.

## Saugumo taisyklės

- Neegzistuojančiam vartotojui ir blogam slaptažodžiui grąžinama ta pati klaida.
- Neautentifikuota sesija atmetama.
- Netinkama rolė gauna prieigos klaidą.
- `db.properties` ir slaptažodžiai į GitHub nekeliami.

## Patikrinimas

- `AuthenticationServiceTest` tikrina visas keturias roles ir klaidingus scenarijus.
- `MysqlAuthenticationSmoke` patikrina BCrypt prisijungimą realioje MySQL bazėje.
- GitHub Actions automatiškai paleidžia visus Maven testus.
