# M03 – Order creation

## Tikslas

Įgyvendinti pirmą pilną užsakymo sukūrimo scenarijų:

```text
Client → FoodOrder → OrderItem → price calculation → MySQL
```

## Verslo taisyklės

- Klientas, restoranas ir meniu elementai turi būti išsaugoti DB.
- Užsakymas privalo turėti bent vieną poziciją.
- Kiekis turi būti didesnis už nulį.
- Meniu elementas turi būti prieinamas ir turėti teigiamą kainą.
- Visi meniu elementai turi priklausyti pasirinktam restoranui.
- `priceAtPurchase` išsaugo užsakymo metu galiojusią kainą.
- `subtotal` yra pozicijų kainų ir kiekių suma.
- `priceMultiplier` turi būti teigiamas.
- `totalPrice = subtotal × priceMultiplier`, apvalinant iki 2 skaičių po kablelio.
- Naujo užsakymo būsena yra `CREATED`.

## Patikrinimas

- `OrderServiceTest` tikrina kainų skaičiavimą, tuščią užsakymą ir kito restorano poziciją.
- `PersistenceFoundationTest` toliau tikrina JPA/H2 pagrindą.
- `MysqlOrderSmoke` rankiniu būdu patikrina realų MySQL užsakymo įrašymą ir nuskaitymą.
- GitHub Actions automatiškai vykdo `mvn clean test` kiekviename pull request'e.
