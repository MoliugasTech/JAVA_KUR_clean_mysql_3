# M16 – JavaFX GUI pagrindas

- JavaFX prijungtas per Maven, nekeičiant Java 21 ir servisų sluoksnio.
- Sukurtas pirmasis vizualus prisijungimo ekranas.
- Prisijungimas naudoja esamą `AuthenticationService`, todėl galioja BCrypt ir
  paskyros blokavimo taisyklės.
- Po prisijungimo rodoma saugi `UserSession` informacija ir rolės pavadinimas.
- Veikia atsijungimas ir Hibernate išteklių uždarymas užveriant programą.

Paleidimas:

```bash
mvn clean javafx:run
```
