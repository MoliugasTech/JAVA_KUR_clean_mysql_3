# M09 – Kliento konsolės scenarijus

## Tikslas

Prijungti pirmą pilną vartotojo scenarijų prie realių servisų ir MySQL duomenų:
nuo registracijos iki sukurto užsakymo peržiūros.

## Funkcionalumas

- Pradiniame meniu klientas gali užsiregistruoti.
- Prisijungęs klientas mato restoranų sąrašą.
- Pasirinkus restoraną rodomi tik prieinami meniu patiekalai.
- Klientas gali sukurti vieno patiekalo užsakymą ir nurodyti kiekį.
- Klientas mato savo užsakymų ID, restoraną, būseną ir galutinę kainą.
- Paieška pagal sesijos vartotojo ID neleidžia matyti kito kliento užsakymų.

## Etapo riba

M09 užbaigia kliento pagrindinį kelią. M10 prijungs restorano savininko,
vairuotojo ir administratoriaus veiksmus.

## Patikrinimas

- `ClientConsoleServiceTest` tikrina katalogą, užsakymo sukūrimą ir nuosavybę.
- `ConsoleMenuTest` toliau tikrina prisijungimo navigaciją.
- Realus scenarijus tikrinamas paleidus `StartNoGui` su MySQL.
- GitHub Actions paleidžia visą Maven testų rinkinį.
