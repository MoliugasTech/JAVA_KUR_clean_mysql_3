# M18 – klientų ir vairuotojų REST CRUD

Šis etapas įgyvendina laboratorinio darbo REST dalį klientui ir vairuotojui. REST API naudoja esamas `Client`, `Driver`, Hibernate/JPA ir MySQL dalis. Android aplikacija bus jungiama kitame etape.

## Endpoint'ai

| Veiksmas | Klientai | Vairuotojai | Atsakymas |
|---|---|---|---|
| Sąrašas | `GET /api/clients` | `GET /api/drivers` | `200 OK` |
| Vienas įrašas | `GET /api/clients/{id}` | `GET /api/drivers/{id}` | `200 OK` arba `404` |
| Sukūrimas | `POST /api/clients` | `POST /api/drivers` | `201 Created` |
| Atnaujinimas | `PUT /api/clients/{id}` | `PUT /api/drivers/{id}` | `200 OK` |
| Šalinimas | `DELETE /api/clients/{id}` | `DELETE /api/drivers/{id}` | `204 No Content` |

`DELETE` išjungia paskyrą (`active=false`), bet fiziškai nepašalina istorinių duomenų. REST atsakymai niekada negrąžina slaptažodžio ar jo hash.

## Paleidimas

Projekto šaknyje turi būti ignoruojamas `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/food_delivery
db.user=root
db.password=
```

Paleisti:

```bash
mvn clean test
mvn spring-boot:run
```

## Rankinis kliento CRUD patikrinimas

Sukurti klientą:

```bash
curl -i -X POST http://localhost:8080/api/clients \
  -H 'Content-Type: application/json' \
  -d '{"username":"api_client_test","password":"slaptazodis123","name":"API","surname":"Klientas","email":"api.client@example.com","phoneNumber":"+37060000001","deliveryAddress":"Vilnius"}'
```

Iš atsakymo pasiimti sukurtą `id`, tada patikrinti:

```bash
curl http://localhost:8080/api/clients
curl http://localhost:8080/api/clients/<ID>

curl -i -X PUT http://localhost:8080/api/clients/<ID> \
  -H 'Content-Type: application/json' \
  -d '{"username":"api_client_test","password":null,"name":"API","surname":"Klientas","email":"api.client@example.com","phoneNumber":"+37060000001","deliveryAddress":"Kaunas"}'

curl -i -X DELETE http://localhost:8080/api/clients/<ID>
```

## Rankinis vairuotojo CRUD patikrinimas

Leidžiamos `vehicleType` reikšmės: `CAR`, `MOTORCYCLE`, `BICYCLE`, `SCOOTER`, `OTHER`.

```bash
curl -i -X POST http://localhost:8080/api/drivers \
  -H 'Content-Type: application/json' \
  -d '{"username":"api_driver_test","password":"slaptazodis123","name":"API","surname":"Vairuotojas","email":"api.driver@example.com","phoneNumber":"+37060000002","vehicleType":"CAR"}'

curl http://localhost:8080/api/drivers
curl http://localhost:8080/api/drivers/<ID>

curl -i -X PUT http://localhost:8080/api/drivers/<ID> \
  -H 'Content-Type: application/json' \
  -d '{"username":"api_driver_test","password":null,"name":"API","surname":"Vairuotojas","email":"api.driver@example.com","phoneNumber":"+37060000002","vehicleType":"BICYCLE"}'

curl -i -X DELETE http://localhost:8080/api/drivers/<ID>
```

Po `DELETE` pakartotinis `GET .../<ID>` turi grąžinti `404 Not Found`.
