# M19 – REST autentifikacija ir rolių apsauga

M19 prideda prisijungimą prie REST API prieš pradedant Android aplikaciją.
Slaptažodis tikrinamas su esamu BCrypt hash, o sėkmingai prisijungus išduodamas
kriptografiškai atsitiktinis `Bearer` tokenas, galiojantis 8 valandas.

## Vieši endpoint'ai

- `GET /api/health`
- `POST /api/auth/login`
- `POST /api/clients` – kliento registracija
- `POST /api/drivers` – vairuotojo registracija

## Apsaugoti endpoint'ai

- Klientas gali skaityti, atnaujinti ir išjungti tik savo `/api/clients/{id}`.
- Vairuotojas gali skaityti, atnaujinti ir išjungti tik savo `/api/drivers/{id}`.
- Administratorius gali pasiekti visų klientų ir vairuotojų sąrašus bei įrašus.
- Svetimas arba ne tai rolei skirtas įrašas grąžina `403 Forbidden`.
- Užklausa be galiojančio tokeno grąžina `401 Unauthorized`.

## Prisijungimas

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"JUSU_VARTOTOJAS","password":"JUSU_SLAPTAZODIS"}'
```

Atsakyme grąžinamas `token`, `expiresAt`, `userId`, `username` ir `role`, bet
negrąžinamas slaptažodis ar jo hash.

Nukopijuoti tokeną ir naudoti apsaugotoje užklausoje:

```bash
TOKEN='CIA_NUKOPIJUOTAS_TOKENAS'
curl -i http://localhost:8080/api/clients/JUSU_ID \
  -H "Authorization: Bearer $TOKEN"
```

## Atsijungimas

```bash
curl -i -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

Po atsijungimo tas pats tokenas nebegalioja. Tokenai laikomi tik veikiančio
serverio atmintyje, todėl taip pat panaikinami perkrovus REST serverį. Tai saugus
ir pakankamas kursinio prototipo sprendimas; produkcinėje sistemoje tokenų
saugykla būtų perkeliama į bendrą serverio sesijų saugyklą.
