# M04 – Order lifecycle and driver assignment

## Tikslas

Valdyti užsakymą nuo sukūrimo iki pristatymo, neleidžiant praleisti būsenų ar
priskirti vairuotojo netinkamu metu.

## Būsenų seka

```text
CREATED → CONFIRMED → PREPARING → READY_FOR_PICKUP
       → driver assignment / PICKED_UP → DELIVERING → COMPLETED
```

`CREATED`, `CONFIRMED` ir `PREPARING` užsakymas gali būti atšauktas. `COMPLETED`
ir `CANCELLED` yra galutinės būsenos.

## Taisyklės

- Būsenos keičiamos tik apibrėžta seka.
- `PICKED_UP` negalima nustatyti bendru statuso keitimo metodu.
- Vairuotojas priskiriamas tik `READY_FOR_PICKUP` užsakymui.
- Užsakymą galima paimti tik vieną kartą.
- Priskyrus vairuotoją būsena tampa `PICKED_UP`.
- `completedAt` nustatomas tik pereinant į `COMPLETED`.
- Visi pakeitimai išsaugomi per `OrderRepository.update`.

## Patikrinimas

- `OrderLifecycleServiceTest` tikrina pilną seką ir neteisingus scenarijus.
- `MysqlOrderLifecycleSmoke` tikrina realų būsenų išsaugojimą MySQL.
- GitHub Actions automatiškai paleidžia visus H2 ir service testus.
