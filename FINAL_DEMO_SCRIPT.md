# Galutinis demonstravimo scenarijus

Šis scenarijus parodo pagrindinius reikalavimus per maždaug 10–15 minučių.

## 1. Techninė būsena

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 24)
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean test
```

Parodyti `BUILD SUCCESS`, tada paleisti REST API ir JavaFX atskiruose
terminaluose.

## 2. Administratorius – JavaFX

1. Prisijungti administratoriumi.
2. „Vartotojų valdymas“: parodyti paiešką pagal keturis laukus ir blokavimą.
3. „Sistemos duomenys“: filtruoti užsakymus pagal restoraną ir būseną.
4. Parodyti visų restoranų patiekalus, žinutes ir atsiliepimus.
5. Paaiškinti, kad aktyvių užsakymų bei istoriją turinčių naudotojų fiziškai
   šalinti neleidžiama.

## 3. Restorano savininkas – JavaFX

1. „Restoranai ir meniu“: sukurti laikiną patiekalą, pakeisti kainą ir paslėpti.
2. „Restorano užsakymai“: pasirinkti naują užsakymą.
3. Priimti, pradėti ruošti ir pažymėti paruoštu.
4. Parodyti vairuotojo paėmimo blokavimą.
5. Parašyti klientui žinutę.
6. Kitą naują užsakymą atmesti su privaloma priežastimi.

## 4. Klientas – Android

1. Prisijungti klientu, pasirinkti restoraną ir sudėti krepšelį.
2. Parodyti bendrą sumą bei sukurti užsakymą.
3. „Mano užsakymai“ parodyti detales ir pokalbį.
4. Atmestame užsakyme parodyti priežastį ir tik skaitymo režimą.
5. Užbaigtame užsakyme parodyti restorano bei vairuotojo vertinimus.
6. Profilyje parodyti sukauptus bonuso taškus.

## 5. Vairuotojas – Android

1. Parodyti, kad užblokuoto užsakymo nėra laisvų pristatymų sąraše.
2. Savininkui leidus paėmimą, atnaujinti sąrašą ir pasiimti užsakymą.
3. Pereiti `PICKED_UP → DELIVERING → COMPLETED`.
4. Parodyti žinutes ir tai, kad po `COMPLETED` jos tik skaitomos.
5. Pateikti kliento įvertinimą.

## 6. Baigiamasis paaiškinimas

Pabrėžti, kad JavaFX ir Android naudoja tą pačią MySQL duomenų bazę, tačiau
Android prie jos jungiasi tik per autorizuotą Spring Boot REST API. Visos
kritinės kainodaros, būsenų, nuosavybės ir dublikatų taisyklės tikrinamos
serverio pusėje.
