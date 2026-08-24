# M15 – galutinė integracinė patikra

Šis etapas užšaldo konsolinę sąsają. Tolimesnė GUI ar REST sąsaja turi naudoti
esamus servisus, nekeisdama patikrintų verslo taisyklių.

## Automatinė patikra

```bash
mvn clean test
```

Priėmimo kriterijus: `Failures: 0`, `Errors: 0` ir `BUILD SUCCESS`.

## Rankinis E2E scenarijus

1. Užregistruoti klientą, savininką ir vairuotoją.
2. Savininkui sukurti restoraną ir bent du prieinamus patiekalus.
3. Klientui sukurti vieną užsakymą su abiem patiekalais, pakeisti vieno kiekį.
4. Savininkui pakeisti būsenas į `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`.
5. Vairuotojui pasiimti užsakymą ir patikrinti, kad jis dingsta iš laisvų sąrašo.
6. Klientui, savininkui ir vairuotojui apsikeisti žinutėmis.
7. Vairuotojui nustatyti `DELIVERING`, po to `COMPLETED`.
8. Patikrinti, kad baigtam užsakymui naujos žinutės nebeleidžiamos.
9. Klientui įvertinti restoraną ir vairuotoją, vairuotojui – klientą.
10. Patikrinti, kad pakartotinis to paties tipo įvertinimas atmetamas.
11. Administratoriui peržiūrėti visus keturis sistemos sąrašus.
12. Administratoriui užblokuoti testinį naudotoją ir patikrinti prisijungimo atmetimą.
13. Naudotoją atblokuoti ir patikrinti sėkmingą prisijungimą.

## Duomenų bazės patikra

```sql
SELECT id, username, role, active FROM users ORDER BY id;
SELECT id, status, client_id, restaurant_id, driver_id FROM food_orders ORDER BY id;
SELECT order_id, sender_id, content, sent_at FROM order_messages ORDER BY sent_at;
SELECT order_id, author_id, review_type, rating FROM reviews ORDER BY created_at;
```

## Galutinis sprendimas

Po šio etapo `No GUI` laikoma laboratoriniams darbams ir integracinei diagnostikai
skirta stabilia sąsaja. Naujas naudotojo funkcionalumas kuriamas GUI sluoksnyje,
o bendros taisyklės lieka `service` sluoksnyje.
