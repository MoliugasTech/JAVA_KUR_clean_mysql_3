# M11 – Vairuotojo konsolės scenarijus

## Funkcionalumas

- Vairuotojo registracija su transporto priemonės tipu.
- Nepriskirtų `READY_FOR_PICKUP` užsakymų sąrašas.
- Užsakymo paėmimas tik vieną kartą.
- Tik vairuotojui priskirtų aktyvių pristatymų sąrašas.
- Būsenos `PICKED_UP → DELIVERING → COMPLETED`.
- `completedAt` automatiškai nustatomas užbaigus pristatymą.

## Saugumo taisyklės

- Vairuotojas negali paimti neparuošto arba jau priskirto užsakymo.
- Vairuotojas negali keisti svetimo pristatymo.
- Vairuotojas negali nustatyti restorano ruošimo būsenų.
- Konsolėje užsakymas pasirenkamas pagal matomą sąrašo numerį.

## Patikrinimas

- `DriverConsoleServiceTest` tikrina paėmimą, nuosavybę ir būsenų seką.
- GitHub Actions paleidžia visą Maven testų rinkinį.
