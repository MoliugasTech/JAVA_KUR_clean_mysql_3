# M09 – Pradinė kliento konsolės integracija

## Tikslas

Prijungti pirmą pilną vartotojo scenarijų prie realių servisų ir MySQL duomenų:
nuo registracijos iki sukurto užsakymo peržiūros.

## Funkcionalumas

- Pradiniame meniu klientas gali užsiregistruoti.
- Prisijungęs klientas mato restoranų sąrašą.
- Pasirinkus restoraną rodomi tik prieinami meniu patiekalai.
- Restoranas ir patiekalas pasirenkami pagal matomą sąrašo numerį, ne DB ID.
- Klientas gali sukurti vieno patiekalo užsakymą, nurodyti kiekį ir jį patvirtinti.
- Iš pasirinkimo galima saugiai grįžti neišsaugant užsakymo.
- Klientas mato savo užsakymų ID, restoraną, būseną ir galutinę kainą.
- Paieška pagal sesijos vartotojo ID neleidžia matyti kito kliento užsakymų.

## Etapo riba

M09 patikrina pirmą vertikalią kliento integraciją. Kelių patiekalų krepšelis ir
galutinis užsakymo UX bus užbaigti po savininko bei vairuotojo konsolės srautų.

## Patikrinimas

- `ClientConsoleServiceTest` tikrina katalogą, užsakymo sukūrimą ir nuosavybę.
- `ConsoleMenuTest` toliau tikrina prisijungimo navigaciją.
- Realus scenarijus tikrinamas paleidus `StartNoGui` su MySQL.
- GitHub Actions paleidžia visą Maven testų rinkinį.
