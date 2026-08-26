# M28 – restorano savininko užsakymų valdymas

Šiame etape JavaFX programoje restorano savininkas gali:

- pasirinkti savo restoraną ir matyti tik jam priklausančius užsakymus;
- filtruoti pagal būseną, datų intervalą ir klientą;
- vykdyti būsenų seką `CREATED → CONFIRMED → PREPARING → READY_FOR_PICKUP`;
- atmesti neužbaigtą užsakymą tik įrašęs priežastį;
- pašalinti pasirinktą patiekalą iki atsiėmimo ir automatiškai perskaičiuoti sumą;
- laikinai neleisti vairuotojams paimti užsakymo.

Klientas atšaukto užsakymo detalėse Android programėlėje mato restorano įrašytą
atmetimo priežastį.

## Duomenų bazės pakeitimas

Prieš paleidžiant šią šaką vieną kartą MySQL Workbench reikia įvykdyti:

```sql
USE food_delivery;
SOURCE database/V3__add_order_management_fields.sql;
```

Jeigu Workbench `SOURCE` komandos nenaudoja, galima atsidaryti
`database/V3__add_order_management_fields.sql` ir įvykdyti dvi jame esančias
`ALTER TABLE` dalis.

## Saugos taisyklės

Visi veiksmai serveryje dar kartą tikrina restorano nuosavybę, leistiną būseną ir
vairuotojo priskyrimą. Vairuotojo užsakymo paėmimas naudoja duomenų bazės užraktą
ir po užrakinimo pakartotinai patikrina, ar restoranas vis dar leidžia paėmimą.

## Automatinė patikra

PR vykdo atskirus Maven ir Android testų workflow. Rankinis testas atliekamas tik
abiem patikroms pasibaigus sėkmingai.
