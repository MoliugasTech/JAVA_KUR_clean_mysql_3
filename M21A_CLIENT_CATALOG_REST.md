# M21A – kliento restoranų katalogo REST API

Šis etapas prideda tik skaitymo operacijas, reikalingas būsimam Android kliento
restoranų ir patiekalų ekranui.

## Endpoint'ai

- `GET /api/catalog/restaurants` – restoranų sąrašas su aprašymu, adresu,
  tipu ir darbo laiku;
- `GET /api/catalog/restaurants/{restaurantId}/menu-items` – tik pasirinkto
  restorano šiuo metu prieinami patiekalai su aprašymu ir kaina.

Abiem endpoint'ams reikalingas `Bearer` tokenas. Prieigą turi klientas ir
administratorius. Vairuotojui grąžinamas `403 Forbidden`, o neprisijungusiam
naudotojui – `401 Unauthorized`.

Android ekranai, krepšelis ir užsakymo kūrimas į M21A neįtraukti.
