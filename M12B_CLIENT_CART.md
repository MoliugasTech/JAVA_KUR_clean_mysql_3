# M12B – kelių patiekalų kliento krepšelis

## Tikslas

Pakeisti vieno patiekalo užsakymo konsolinį scenarijų į daugkartinai redaguojamą
krepšelį, neperkeliant verslo logikos į konsolės valdiklį.

## Įgyvendinta

- `OrderCart` saugo vieno restorano patiekalus ir jų kiekius.
- Tą patį patiekalą pasirinkus dar kartą, jo kiekis pakeičiamas.
- Patiekalą galima pašalinti, peržiūrėti eilučių sumas ir bendrą sumą.
- Tuščio krepšelio patvirtinti negalima.
- Viename krepšelyje negalima maišyti skirtingų restoranų patiekalų.
- Užsakymo kūrimas ir galutinis kainos perskaičiavimas lieka `OrderService` sluoksnyje.
- `OrderCart` nėra JPA esybė, todėl jį bus galima pakartotinai naudoti būsimoje GUI.

## Patikra

Automatiniai testai tikrina kelių patiekalų sumą, kiekio pakeitimą, pašalinimą,
neteisingus kiekius, neprieinamą patiekalą, skirtingus restoranus, tuščią
krepšelį ir pilną konsolinį užsakymo scenarijų.
