# M12A – Konsolės architektūros checkpointas

Prieš galutinį kliento krepšelį monolitinis `MenuControl` padalintas į atskirus
kliento, savininko, vairuotojo ir administratoriaus valdiklius.

`MenuControl` dabar atsakingas tik už registraciją, prisijungimą, sesiją ir
nukreipimą pagal rolę. Verslo logika ir toliau lieka service sluoksnyje.

Tai sumažina būsimo GUI kūrimo kainą: GUI naudos tuos pačius servisus, o ne
kopijuos konsolės navigacijos kodą.
