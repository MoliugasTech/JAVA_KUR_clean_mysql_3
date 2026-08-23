# M06 – Užsakymo žinutės

## Tikslas

Suteikti konkretaus užsakymo dalyviams uždarą pokalbį apie užsakymo ruošimą ir
pristatymą.

## Dalyviai

- Užsakymo klientas.
- Restorano, kuriam pateiktas užsakymas, savininkas.
- Užsakymui jau priskirtas vairuotojas.

## Taisyklės

- Pašalinis vartotojas negali nei rašyti, nei skaityti pokalbio.
- Vairuotojas dalyviu tampa tik po priskyrimo užsakymui.
- Tuščios ir ilgesnės nei 2000 simbolių žinutės atmetamos.
- Žinutės apkarpomos ir pateikiamos chronologine tvarka.
- Užbaigus arba atšaukus užsakymą naujų žinučių siųsti nebegalima.
- Seno pokalbio dalyviai skaityti nepraranda.

## Patikrinimas

- `OrderMessageServiceTest` tikrina dalyvius, turinį ir užsakymo būsenas.
- `MysqlOrderMessageSmoke` patikrina žinučių išsaugojimą realioje MySQL bazėje.
- GitHub Actions automatiškai paleidžia visus Maven testus.
