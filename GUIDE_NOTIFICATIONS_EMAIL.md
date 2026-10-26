# Guide : ajouter des notifications par email dans un projet Spring Boot

Ce guide explique, étape par étape, comment les notifications par email ont été ajoutées au projet
**Gestion des prêts BCM**. Il est écrit pour être **réutilisé dans n'importe quel autre projet Spring Boot**.

## Ce que fait la fonctionnalité

| Quand ? | Qui reçoit ? | Message |
|---|---|---|
| Un prêt est créé | L'employé | « Le matériel X vous a été prêté, à rendre le JJ/MM/AAAA » |
| Un prêt est retourné | L'employé | « Nous confirmons le retour du matériel X » |
| Chaque matin à 8h (lundi à vendredi) | Chaque employé en retard | « RAPPEL : matériel à rendre (N jours de retard) » |
| Clic sur « Envoyer les rappels » | Chaque employé en retard | Même rappel, envoyé tout de suite |

## Vue d'ensemble : comment ça marche

```
PretService.creerPret()
   │  enregistre le prêt en base
   │  appelle notificationService.notifierPretCree(pret)
   ▼
NotificationService          ← rédige le message (QUOI envoyer, À QUI)
   │  attend que la base ait validé l'enregistrement (commit)
   ▼
EmailService (@Async)        ← envoie l'email en arrière-plan (COMMENT envoyer)
   │
   ▼
JavaMailSender (Spring)  ──►  serveur SMTP  ──►  boîte mail de l'employé
```

On sépare en **2 services** :
- `NotificationService` sait **quoi** dire (le texte des messages, le destinataire).
- `EmailService` sait **comment** envoyer (le protocole SMTP).

Si demain on veut envoyer des SMS, il suffit d'ajouter un `SmsService` : `NotificationService` ne change presque pas.

---

## Étape 1 — Ajouter la dépendance (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

Ce « starter » apporte **JavaMailSender**, l'outil de Spring pour envoyer des emails.
Spring Boot le crée automatiquement dès que `spring.mail.host` est défini (étape 2).

## Étape 2 — Configurer le serveur mail (application.properties)

```properties
spring.mail.host=${MAIL_HOST:localhost}
spring.mail.port=${MAIL_PORT:1025}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=${MAIL_SMTP_AUTH:false}
spring.mail.properties.mail.smtp.starttls.enable=${MAIL_STARTTLS:false}
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

app.notifications.enabled=${NOTIFICATIONS_ENABLED:true}
app.notifications.expediteur=${MAIL_FROM:gestion-prets@bcm.mr}
app.notifications.rappel-cron=0 0 8 * * MON-FRI
```

À comprendre :
- `${MAIL_HOST:localhost}` veut dire : « prends la variable d'environnement `MAIL_HOST`, sinon `localhost` ».
  On ne change donc pas le code entre le développement et la production.
- Les `timeout` évitent que l'application reste bloquée si le serveur mail ne répond pas.
- `app.notifications.*` sont **nos propres** réglages (le préfixe `app.` est une convention).
- **Format cron** : `seconde minute heure jour-du-mois mois jour-de-la-semaine`.
  `0 0 8 * * MON-FRI` = à 8h00 et 0 seconde, du lundi au vendredi.

### Quel serveur mail utiliser ?

| Situation | host | port | auth / starttls |
|---|---|---|---|
| Développement avec **MailDev** (recommandé) | `localhost` | `1025` | false / false |
| Gmail (pour un test réel) | `smtp.gmail.com` | `587` | true / true + **mot de passe d'application** Google |
| Serveur de l'entreprise | demander au service informatique | souvent `587` | souvent true / true |

⚠️ Ne jamais écrire un vrai mot de passe dans `application.properties` : utiliser la variable d'environnement `MAIL_PASSWORD`.

## Étape 3 — Activer l'asynchrone et les tâches planifiées

Sur la classe principale (celle qui a `@SpringBootApplication`) :

```java
@SpringBootApplication
@EnableAsync      // active @Async
@EnableScheduling // active @Scheduled
public class GestionPretsBcmApplication { ... }
```

Sans ces 2 annotations, `@Async` et `@Scheduled` sont **ignorés sans aucune erreur**. C'est un piège classique.

## Étape 4 — Le service d'envoi : EmailService

Fichier : `notification/EmailService.java`

```java
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean active;
    private final String expediteur;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.notifications.enabled}") boolean active,
            @Value("${app.notifications.expediteur}") String expediteur) { ... }

    @Async
    public void envoyer(String destinataire, String sujet, String texte) {
        if (!active) { log.info(...); return; }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(texte);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Échec de l'envoi ...", e.getMessage());
        }
    }
}
```

3 idées importantes :
1. **`@Async`** : l'email part dans un autre thread. L'utilisateur n'attend pas le serveur mail
   (qui peut mettre plusieurs secondes à répondre).
2. **`try / catch`** : si l'email échoue (serveur en panne, adresse invalide…), on l'écrit dans les logs,
   mais **le prêt reste enregistré**. Un email raté ne doit jamais faire échouer l'action principale.
3. **L'interrupteur `enabled`** : on peut couper les emails sans toucher au code (pratique pour les tests).

## Étape 5 — Le service qui rédige les messages : NotificationService

Fichier : `notification/NotificationService.java`

Il a une méthode par type de message : `notifierPretCree`, `notifierRetour`, `notifierRetard`.
Chacune construit le texte avec un **text block** Java (`"""...""".formatted(...)`) puis appelle :

```java
private boolean envoyerApresEnregistrement(Employe employe, String sujet, String texte) {
    if (employe.getEmail() == null || employe.getEmail().isBlank()) {
        return false;                       // pas d'adresse : on ne fait rien
    }
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {     // exécuté APRÈS la validation en base
                emailService.envoyer(destinataire, sujet, texte);
            }
        });
    } else {
        emailService.envoyer(destinataire, sujet, texte);
    }
    return true;
}
```

**Pourquoi `afterCommit` ?** La méthode `creerPret` est `@Transactional` : l'enregistrement en base n'est
définitif qu'**à la fin** de la méthode (le commit). Si on envoyait l'email tout de suite et que le commit
échouait ensuite, l'employé recevrait un email pour un prêt qui n'existe pas. Avec `afterCommit`,
l'email part **seulement si** l'enregistrement a réussi.

**Pourquoi préparer le texte avant `@Async` ?** Les entités JPA (Pret, Employe…) sont liées à la
transaction. Dans un autre thread, accéder à `pret.getEmploye()` peut provoquer une
`LazyInitializationException`. On prépare donc le texte (de simples `String`) **avant**, puis on passe
seulement des `String` à `EmailService`.

## Étape 6 — Brancher les notifications dans le service métier

Dans `PretService` : on injecte `NotificationService` (`private final` + `@RequiredArgsConstructor`)
et on l'appelle **après** l'enregistrement :

```java
Pret enregistre = pretRepository.save(pret);
notificationService.notifierPretCree(enregistre);
return enregistre;
```

Même chose dans `retournerPret` avec `notifierRetour`. On ajoute aussi une méthode qui envoie les rappels :

```java
public int envoyerRappelsRetard() {
    int envoyes = 0;
    for (Pret pret : findEnRetard()) {
        if (notificationService.notifierRetard(pret)) envoyes++;
    }
    return envoyes;
}
```

## Étape 7 — La tâche automatique : @Scheduled

Fichier : `notification/RappelRetardScheduler.java`

```java
@Component
@RequiredArgsConstructor
public class RappelRetardScheduler {
    private final PretService pretService;

    @Scheduled(cron = "${app.notifications.rappel-cron}")
    public void envoyerRappelsQuotidiens() {
        pretService.envoyerRappelsRetard();
    }
}
```

Spring appelle cette méthode **tout seul** à l'heure indiquée. Personne n'a besoin de cliquer.

⚠️ **Piège** : la méthode qui lit la base (`envoyerRappelsRetard`) est dans `PretService`, qui est `@Transactional`.
Si on la mettait dans le scheduler et qu'on l'appelait depuis une autre méthode de la **même classe**, le
`@Transactional` serait ignoré (Spring ne peut pas intercepter un appel interne à une classe). Résultat :
`LazyInitializationException`. Règle : **une méthode `@Transactional` doit être appelée depuis une autre classe**.

## Étape 8 — Un bouton pour envoyer les rappels tout de suite

Pour la démonstration, on n'attend pas 8h du matin :

- Backend : `POST /api/prets/rappels-retard` dans `PretController`, qui renvoie `{"emailsEnvoyes": 2}`.
  Grâce aux règles de `SecurityConfig`, seuls ADMIN et GESTIONNAIRE peuvent l'appeler.
- Frontend : un bouton « Envoyer les rappels par email » sur le tableau de bord, au-dessus des prêts en retard.

## Étape 9 — Les tests

| Fichier de test | Ce qu'il vérifie |
|---|---|
| `EmailServiceTest` | Le message a le bon expéditeur, destinataire, sujet et texte. Rien n'est envoyé si `enabled=false`. Aucune erreur n'est lancée si le serveur mail est en panne. |
| `NotificationServiceTest` | Le texte contient le nom, le matériel et les dates. Pas d'email si l'employé n'a pas d'adresse. **L'email part seulement après le commit.** |
| `PretServiceTest` | Créer ou retourner un prêt déclenche une notification. Un prêt refusé n'en déclenche **pas**. Le compteur des rappels est juste. |

Dans les tests, `JavaMailSender` et `EmailService` sont des **mocks** (Mockito) : aucun vrai email n'est envoyé.

## Étape 10 — Tester en vrai avec MailDev

**MailDev** est un faux serveur mail : il reçoit les emails mais ne les envoie à personne.
On les lit dans une page web. C'est parfait pour le développement et pour une démonstration.

MailDev est un outil Node.js. Il faut donc avoir **Node.js** (version 20 ou plus, vérifier avec `node --version`).

Installation (une seule fois) :

```bash
npm install -g maildev@3.0.0
```

`-g` = installation « globale » : la commande `maildev` devient disponible partout, pour tous les projets.
(Ça marche aussi sous Windows et Linux, avec la même commande.)

Démarrage, à chaque fois qu'on veut tester les emails :

```bash
maildev
```

- Il reçoit les emails sur le port **1025** (c'est notre `spring.mail.port`, rien à changer dans Spring).
- Boîte de réception : **http://localhost:1080**.
- Pour l'arrêter : `Ctrl + C` dans le terminal.
- Les emails sont gardés en mémoire : ils disparaissent quand on arrête MailDev.

Ensuite : démarrer le backend, créer un prêt pour un employé **qui a une adresse email**,
et ouvrir http://localhost:1080. L'email est là.

> **Autre possibilité : Mailpit.** C'est un outil équivalent (boîte de réception sur le port 8025).
> Il n'a pas été retenu ici car `brew install mailpit` ne fonctionne pas sur macOS 13.
> Pour passer de l'un à l'autre, il n'y a **rien à changer** dans Spring : les deux reçoivent sur le port 1025.

---

## Problèmes fréquents

| Symptôme | Cause probable | Solution |
|---|---|---|
| Aucun email, et dans les logs `Connection refused` | Le serveur mail n'est pas démarré | Lancer `maildev` (ou vérifier `spring.mail.host` et `spring.mail.port`) |
| Aucun email, et dans les logs `Pas d'email pour l'employé` | L'employé n'a pas d'adresse email | Ajouter son email dans la page Employés |
| Aucun email, et dans les logs `Notifications désactivées` | `app.notifications.enabled=false` | Le mettre à `true` |
| Aucun email, et rien dans les logs | `@EnableAsync` ou `@EnableScheduling` manquant | Les ajouter sur la classe principale |
| `LazyInitializationException` dans les logs | Une entité JPA est lue dans un autre thread ou hors transaction | Préparer le texte avant `@Async`, et appeler la méthode `@Transactional` depuis une autre classe |
| Gmail : `Authentication failed` | Le mot de passe normal du compte ne marche pas | Créer un **mot de passe d'application** dans le compte Google (validation en 2 étapes obligatoire) |
| L'application démarre avec l'erreur `JavaMailSender` introuvable | `spring.mail.host` n'est pas défini | L'ajouter dans `application.properties` |

## Pour aller plus loin

- **Emails en HTML** (logo, couleurs) : utiliser `MimeMessageHelper` à la place de `SimpleMailMessage`,
  avec un modèle Thymeleaf (`spring-boot-starter-thymeleaf`).
- **Éviter de renvoyer le même rappel** : enregistrer la date du dernier rappel dans le prêt.
- **SMS** : même principe, avec un `SmsService` qui appelle l'API d'un fournisseur de SMS.
