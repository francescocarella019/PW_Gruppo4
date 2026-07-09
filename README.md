# Documento di Analisi Tecnica
## PW_Gruppo4 — Forum Agenzia Viaggi Puglia

---

> **Versione:** 1.0  
> **Data:** 20 giugno 2026  
> **Autore:** Gruppo 4  
> **Lingua:** Italiano  

---

## Indice

1. [Requisiti Funzionali](#1-requisiti-funzionali)
2. [Requisiti Non Funzionali](#2-requisiti-non-funzionali)
3. [Stack Tecnologico](#3-stack-tecnologico)
4. [Architettura e Implementazione](#4-architettura-e-implementazione)
5. [Configurazione Docker](#5-configurazione-docker)

---

## 1. Requisiti Funzionali

Il sistema implementa una piattaforma forum per un'agenzia di viaggi in Puglia, permettendo agli utenti di consultare esperienze turistiche, pubblicare recensioni, interagire attraverso commenti e porre domande. L'analisi dei controller (`LoginController`, `ForumController`, `DashboardProfiloController`, `StaffController`, `ContattiController`) ha portato all'identificazione dei seguenti requisiti funzionali, organizzati per macro-aree.

---

### 1.1 Gestione Utenti e Autenticazione

| Funzionalità | Endpoint | Metodo HTTP | Note |
|---|---|---|---|
| Visualizza pagina di login | `/login` | `GET` | Pagina pubblica |
| Effettua il login | `/login` | `POST` | Avvia sessione con `utenteId` e `isAdmin` |
| Registrazione nuovo utente | `/register` | `GET` / `POST` | La password viene cifrata con BCrypt prima del salvataggio |
| Logout | `/logout` | `GET` | Invalida la `HttpSession` corrente |
| Visualizza profilo personale | `/profilo` | `GET` | Richiede sessione attiva |
| Visualizza dashboard personale | `/dashboard` | `GET` | Mostra storico acquisti e recensioni dell'utente |

**Logica di autenticazione:** La verifica delle credenziali avviene direttamente nel `LoginController`, che recupera l'utente tramite `UtenteRepository.findByEmail()` e confronta la password fornita con l'hash BCrypt salvato, mediante `BCryptPasswordEncoder.matches()`. In caso di successo, l'ID utente (`utenteId`) e il flag `isAdmin` vengono salvati nella `HttpSession`.

---

### 1.2 Gestione Esperienze e Recensioni

| Funzionalità | Endpoint | Metodo HTTP | Restrizioni |
|---|---|---|---|
| Visualizza elenco esperienze/recensioni | `/` oppure `/reviews` | `GET` | Pubblico |
| Filtra per categoria | `/reviews?categoriaId={id}` | `GET` | Pubblico |
| Filtra per esperienza specifica | `/reviews?esperienzaId={id}` | `GET` | Pubblico |
| Visualizza singola recensione | `/reviews?recensioneId={id}` | `GET` | Pubblico |
| Pubblica nuova recensione | `/reviews/nuova` | `POST` | Solo utenti autenticati che hanno acquistato l'esperienza E la cui data è già trascorsa |
| Modifica propria recensione | `/dashboard/recensioni/{id}` | `POST` | Solo l'autore della recensione |

**Regola di business critica (eleggibilità alla recensione):** Un utente può pubblicare una recensione su un'esperienza solo se sono verificate **entrambe** le seguenti condizioni, controllate nel `ForumController`:
1. `AcquistoRepository.existsByUtenteIdAndEsperienzaId(utenteId, esperienzaId)` restituisce `true`.
2. La data dell'esperienza è già trascorsa: `!esperienza.getData().isAfter(LocalDate.now())`.

**Gestione allegati:** Il `ForumController` gestisce l'upload di immagini per le recensioni, salvando i file nella directory `uploads/reviews/` del filesystem e registrando i relativi percorsi nella tabella `immagini` tramite l'entità `Immagine`.

---

### 1.3 Sistema di Commenti e Interazioni

| Funzionalità | Endpoint | Metodo HTTP | Restrizioni |
|---|---|---|---|
| Aggiunge un commento a una recensione | `/recensioni/{id}/commenti` | `POST` | Utente autenticato |
| Risponde a un commento | `/commenti/{id}/risposte` | `POST` | Utente autenticato |
| Pone una domanda su un'esperienza | `/esperienze/domanda` | `POST` | Utente autenticato |

Le entità corrispondenti sono `Commento`, `RispostaCommento`, `Domanda` e `Risposta`. Il sistema supporta una struttura di commenti a **due livelli** (commento → risposta al commento).

---

### 1.4 Pagine Statiche e Contatti

| Funzionalità | Endpoint | Metodo HTTP | Restrizioni |
|---|---|---|---|
| Visualizza pagina "Chi Siamo" | `/chi-siamo` | `GET` | Pubblico |
| Visualizza pagina "Contattaci" | `/contattaci` | `GET` | Pubblico |
| Invia messaggio di contatto | `/contattaci` | `POST` | Pubblico; salva un'entità `Contatto` nel database |

---

### 1.5 Moderazione e Pannello Staff

| Funzionalità | Endpoint | Metodo HTTP | Restrizioni |
|---|---|---|---|
| Accede al pannello staff | `/staff` | `GET` | Solo utenti con ruolo amministratore |
| Visualizza tutti i messaggi di contatto | `/staff` | `GET` | Solo utenti con ruolo amministratore |

**Logica di autorizzazione per l'area Staff:** Il controllo del ruolo viene eseguito manualmente nello `StaffController` tramite un metodo helper `isAdmin(Utente)` che verifica se il nome del ruolo dell'utente contiene le stringhe `"admin"`, `"staff"` o `"amministratore"` (confronto case-insensitive). In assenza di tale ruolo, l'utente viene reindirizzato alla home page.

---

### 1.6 Riepilogo per Tipo di Utente

| Capacità | Guest (non autenticato) | Utente Registrato | Staff/Amministratore |
|---|:---:|:---:|:---:|
| Consultare esperienze e recensioni | ✅ | ✅ | ✅ |
| Filtrare per categoria/esperienza | ✅ | ✅ | ✅ |
| Registrarsi / Effettuare login | ✅ | — | — |
| Inviare messaggio di contatto | ✅ | ✅ | ✅ |
| Pubblicare recensione | ❌ | ✅ (solo post-acquisto) | ✅ |
| Modificare propria recensione | ❌ | ✅ (solo propria) | ✅ |
| Commentare e rispondere | ❌ | ✅ | ✅ |
| Porre domande su esperienze | ❌ | ✅ | ✅ |
| Accedere al pannello Staff | ❌ | ❌ | ✅ |

---

## 2. Requisiti Non Funzionali

### 2.1 Sicurezza

#### 2.1.1 Autenticazione

Il meccanismo di autenticazione è interamente **gestito a livello applicativo**, all'interno del `LoginController`, senza delegare il processo alla catena di filtri di Spring Security. La sessione HTTP (`HttpSession`) viene utilizzata come store per i dati dell'utente autenticato (`utenteId`, `isAdmin`).

**Conseguenza:** Spring Security non gestisce il ciclo di vita della sessione autenticata. I controlli di accesso alle risorse protette sono implementati manualmente nei singoli controller, con pattern del tipo:

```java
Long utenteId = (Long) session.getAttribute("utenteId");
if (utenteId == null) return "redirect:/login";
```

#### 2.1.2 Autorizzazione

L'autorizzazione è **role-based** ma implementata in modo imperativo. La configurazione in `SecurityConfig.java` è attualmente in uno stato completamente permissivo:

```java
http.csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
```

Ciò significa che **nessuna risorsa è protetta a livello di framework**. La protezione dipende interamente dai controlli manuali implementati nei controller. In particolare, `StaffController` verifica il ruolo dell'utente tramite il metodo helper privato `isAdmin()`.

#### 2.1.3 Cifratura delle Password

Le password degli utenti vengono cifrate con l'algoritmo **BCrypt** prima di essere persistite nel database. Il bean `BCryptPasswordEncoder` è dichiarato in `SecurityConfig.java` e utilizzato sia durante la registrazione (in `LoginController`) per l'hashing, sia durante il login per la verifica tramite `matches()`. BCrypt è un algoritmo di hashing adattivo, considerato sicuro per la memorizzazione di credenziali.

#### 2.1.4 Protezione CSRF

La protezione CSRF è **esplicitamente disabilitata** tramite `csrf -> csrf.disable()`. Questa scelta è comune in applicazioni che espongono API REST stateless, ma in un'applicazione con sessioni HTTP e form HTML, questa configurazione espone l'applicazione ad attacchi Cross-Site Request Forgery.

---

### 2.2 Persistenza e Integrità dei Dati

L'integrità dei dati è garantita principalmente attraverso i vincoli JPA e le relazioni tra le entità:

| Vincolo | Entità | Annotazione JPA | Dettaglio |
|---|---|---|---|
| Unicità email | `Utente` | `@Column(unique = true)` | Impedisce la registrazione di due utenti con la stessa email |
| Relazione N:1 | `Utente` → `Ruolo` | `@ManyToOne` | Ogni utente ha esattamente un ruolo |
| Relazione N:1 | `Utente` → `Luogo` | `@ManyToOne` | Ogni utente è associato a una località |
| Relazione N:1 | `Recensione` → `Utente` | `@ManyToOne` | Ogni recensione appartiene a un utente |
| Relazione N:1 | `Recensione` → `Esperienza` | `@ManyToOne` | Ogni recensione è relativa a un'esperienza |
| Relazione 1:N | `Recensione` → `Immagine` | `@OneToMany` | Una recensione può avere più immagini allegate |
| Relazione N:N | `Esperienza` ↔ `Tipologia` | Tabella ponte `TipologiaEsperienza` | Un'esperienza appartiene a più categorie |
| Relazione N:1 | `Acquisto` → `Utente` | `@ManyToOne` | Traccia gli acquisti per utente |
| Relazione N:1 | `Acquisto` → `Esperienza` | `@ManyToOne` | Traccia gli acquisti per esperienza |

La strategia `hibernate.ddl-auto=update` (nell'ambiente di produzione originale) istruisce Hibernate a modificare lo schema del database in modo incrementale ad ogni avvio, senza eliminare i dati esistenti. Nell'ambiente Replit è stata impostata a `create-drop` (con H2 in-memory).

---

### 2.3 Manutenibilità

L'applicazione adotta un'**architettura a strati** (Controller → Service → Repository) che favorisce la separazione delle responsabilità. L'uso di **Lombok** (`@Data`) riduce significativamente il codice boilerplate nei model (getter, setter, toString), migliorando la leggibilità. Si rileva tuttavia che `UtenteService.java` è attualmente una classe vuota: la logica di business è prevalentemente collocata nei controller, il che riduce la testabilità e la riusabilità del codice.

### 2.4 Portabilità

L'intera infrastruttura applicativa è descritta tramite **Docker** e **Docker Compose**. Questo garantisce che l'ambiente di esecuzione (JVM, MySQL, dipendenze di sistema) sia riproducibile in modo deterministico su qualsiasi host che supporti Docker, eliminando le dipendenze dall'ambiente host.

### 2.5 Scalabilità

Nell'architettura attuale, l'applicazione è un **monolite** (backend + frontend Thymeleaf in un unico processo JVM). La gestione della sessione tramite `HttpSession` lato server rende più complessa una futura scalabilità orizzontale (multi-istanza), che richiederebbe l'introduzione di un session store condiviso (es. Redis). L'architettura a strati e la separazione tramite repository JPA facilitano però una futura migrazione verso microservizi.

---

## 3. Stack Tecnologico

### 3.1 Backend

| Componente | Tecnologia | Versione |
|---|---|---|
| Linguaggio | Java | 17 (target di compilazione) |
| Framework applicativo | Spring Boot | 3.3.5 |
| Web layer | Spring Web MVC | (incluso in Boot 3.3.5) |
| ORM e persistenza | Spring Data JPA + Hibernate | 6.5.3.Final |
| Sicurezza | Spring Security | (incluso in Boot 3.3.5) |
| Template engine | Thymeleaf | (incluso in Boot 3.3.5) |
| Riduzione boilerplate | Lombok | 1.18.34 |
| Hot reload in sviluppo | Spring Boot DevTools | (opzionale) |

### 3.2 Database

| Componente | Tecnologia | Versione |
|---|---|---|
| Database produzione | MySQL | 8 (immagine Docker `mysql:8`) |
| Database sviluppo/Replit | H2 (in-memory) | (incluso in Boot) |
| Driver JDBC (produzione) | `mysql-connector-j` | (gestito da Spring Boot BOM) |
| Driver JDBC (sviluppo) | `org.h2.Driver` | — |

**Strategia di migrazione schema:** Non viene utilizzato alcun tool di migrazione dedicato (Flyway o Liquibase). Lo schema del database è interamente gestito da **Hibernate DDL auto**:
- Ambiente di produzione (MySQL): `spring.jpa.hibernate.ddl-auto=update` — Hibernate aggiorna lo schema esistente in modo incrementale.
- Ambiente Replit (H2): `spring.jpa.hibernate.ddl-auto=create-drop` — lo schema viene ricreato ad ogni avvio.

### 3.3 Frontend

Il frontend è implementato con un approccio **Server-Side Rendering (SSR)** tramite **Thymeleaf**, il template engine nativo di Spring Boot. Non è presente un framework JavaScript SPA separato.

| Componente | Tecnologia |
|---|---|
| Template engine | Thymeleaf (HTML5) |
| Styling | CSS3 (file statici in `static/css/`) |
| Scripting client-side | JavaScript vanilla (file statici in `static/js/` e `static/script.js`) |
| Immagini | JPEG (risorse statiche in `static/images/`) |

I template HTML si trovano in `src/main/resources/templates/` e includono: `index.html`, `authLogin.html`, `registerUser.html`, `reviews.html`, `esperienza-singola.html`, `dashboardProfilo.html`, `dashboard-staff.html`, `contattaci.html`, `chi-siamo.html`.

Le risorse statiche caricate tramite upload degli utenti (immagini delle recensioni) vengono servite dalla directory `uploads/` nella root del progetto, configurata come resource handler aggiuntivo in `UploadConfig.java`.

### 3.4 DevOps

| Componente | Tecnologia | Versione/Note |
|---|---|---|
| Containerizzazione | Docker | Multi-stage build |
| Orchestrazione locale | Docker Compose | Definisce 2 servizi: `app` e `db` |
| Build tool | Maven (Wrapper `mvnw`) | 3.8.6 |
| CI/CD | Non configurato | — |

---

## 4. Architettura e Implementazione

### 4.1 Pattern Architetturale

L'applicazione adotta una **Layered Architecture** (Architettura a Strati), il pattern standard per applicazioni Spring Boot monolitiche. I livelli sono:

```
┌─────────────────────────────────────────────┐
│           PRESENTATION LAYER                │
│  Thymeleaf Templates (HTML/CSS/JS)          │
│  src/main/resources/templates/              │
├─────────────────────────────────────────────┤
│           CONTROLLER LAYER                  │
│  @Controller — gestisce le richieste HTTP   │
│  ForumController, LoginController,          │
│  DashboardProfiloController,                │
│  StaffController, ContattiController        │
├─────────────────────────────────────────────┤
│           SERVICE LAYER                     │
│  @Service — logica di business              │
│  UtenteService (attualmente non popolata)   │
├─────────────────────────────────────────────┤
│           REPOSITORY LAYER                  │
│  @Repository — accesso ai dati via JPA      │
│  UtenteRepository, RecensioneRepository,    │
│  AcquistoRepository, CommentoRepository,    │
│  (e altri...)                               │
├─────────────────────────────────────────────┤
│           DATA LAYER                        │
│  JPA Entities — modello del dominio         │
│  Utente, Esperienza, Recensione,            │
│  Commento, Acquisto, Domanda, ...           │
├─────────────────────────────────────────────┤
│           DATABASE                          │
│  MySQL 8 (produzione) / H2 (sviluppo)       │
└─────────────────────────────────────────────┘
```

---

### 4.2 Flusso dei Dati: Caso d'Uso — Pubblicazione di una Recensione

Il seguente diagramma descrive il flusso completo che si attiva quando un utente autenticato invia il form di creazione di una nuova recensione (`POST /reviews/nuova`):

```
Browser (Client)
    │
    │  POST /reviews/nuova
    │  (utenteId in sessione, dati form + immagini)
    ▼
ForumController.nuovaRecensione()
    │
    ├─► [1] Verifica sessione: session.getAttribute("utenteId") != null
    │
    ├─► [2] Carica Utente: utenteRepository.findById(utenteId)
    │
    ├─► [3] Carica Esperienza: esperienzaRepository.findById(esperienzaId)
    │
    ├─► [4] Verifica eleggibilità:
    │       acquistoRepository.existsByUtenteIdAndEsperienzaId(...)
    │       && !esperienza.getData().isAfter(LocalDate.now())
    │
    ├─► [5] Costruisce entità Recensione e la popola
    │
    ├─► [6] Persiste la recensione: recensioneRepository.save(recensione)
    │         └─► Hibernate genera SQL:
    │             INSERT INTO recensioni (...) VALUES (...)
    │
    ├─► [7] Gestisce upload immagini:
    │       Per ogni file allegato:
    │         - Salva file in uploads/reviews/{filename}
    │         - Crea entità Immagine con path
    │         - immagineRepository.save(immagine)
    │
    └─► [8] Redirect: "redirect:/reviews"
    │
    ▼
Browser riceve HTTP 302 → GET /reviews
```

---

### 4.3 Struttura delle Cartelle

```
PW_Gruppo4/
│
├── src/
│   ├── main/
│   │   ├── java/com/gruppo4/pw/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java       # Spring Security: encoder BCrypt, FilterChain
│   │   │   │   └── UploadConfig.java         # Configura resource handler per /uploads/**
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── ForumController.java       # Core del forum: esperienze, recensioni, commenti
│   │   │   │   ├── LoginController.java       # Autenticazione, registrazione, logout
│   │   │   │   ├── DashboardProfiloController.java # Profilo utente e storico
│   │   │   │   ├── StaffController.java       # Pannello amministrativo
│   │   │   │   └── ContattiController.java    # Form di contatto
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Utente.java                # Entità utente (@Entity, @Table("utenti"))
│   │   │   │   ├── Ruolo.java                 # Ruoli utente (es. "user", "admin")
│   │   │   │   ├── Luogo.java                 # Località geografiche
│   │   │   │   ├── Esperienza.java            # Esperienze turistiche
│   │   │   │   ├── Acquisto.java              # Acquisti effettuati (utente + esperienza)
│   │   │   │   ├── Recensione.java            # Recensioni delle esperienze
│   │   │   │   ├── Immagine.java              # Immagini allegate alle recensioni
│   │   │   │   ├── Commento.java              # Commenti alle recensioni
│   │   │   │   ├── RispostaCommento.java      # Risposte ai commenti (2° livello)
│   │   │   │   ├── Domanda.java               # Domande FAQ sulle esperienze
│   │   │   │   ├── Risposta.java              # Risposte alle domande FAQ
│   │   │   │   ├── Tipologia.java             # Categorie delle esperienze
│   │   │   │   ├── TipologiaEsperienza.java   # Tabella ponte N:N (Esperienza ↔ Tipologia)
│   │   │   │   └── Contatto.java              # Messaggi dal form di contatto
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UtenteRepository.java      # findByEmail, existsByEmail
│   │   │   │   ├── RecensioneRepository.java  # findByUtenteId, findByEsperienzaId
│   │   │   │   ├── AcquistoRepository.java    # existsByUtenteIdAndEsperienzaId
│   │   │   │   ├── CommentoRepository.java
│   │   │   │   ├── EsperienzaRepository.java
│   │   │   │   └── (altri repository...)
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── UtenteService.java         # (attualmente vuota — logica nei controller)
│   │   │   │
│   │   │   └── PwApplication.java             # Entry point (@SpringBootApplication)
│   │   │
│   │   └── resources/
│   │       ├── templates/                     # Template Thymeleaf (HTML5)
│   │       │   ├── index.html
│   │       │   ├── reviews.html
│   │       │   ├── esperienza-singola.html
│   │       │   ├── authLogin.html
│   │       │   ├── registerUser.html
│   │       │   ├── dashboardProfilo.html
│   │       │   ├── dashboard-staff.html
│   │       │   ├── contattaci.html
│   │       │   └── chi-siamo.html
│   │       │
│   │       ├── static/                        # Asset statici
│   │       │   ├── css/                       # Fogli di stile CSS3
│   │       │   ├── js/                        # Script JavaScript vanilla
│   │       │   └── images/                    # Immagini delle esperienze pugliesi
│   │       │
│   │       └── application.properties         # Configurazione DB, porta server, sessione
│   │
│   └── test/
│       └── java/com/gruppo4/pw/
│           └── PwApplicationTests.java        # Test di contesto Spring
│
├── uploads/                                   # Directory runtime per upload immagini recensioni
├── pom.xml                                    # Dipendenze Maven e configurazione build
├── Dockerfile                                 # Build multi-stage (Maven build + JRE runtime)
├── docker-compose.yml                         # Orchestrazione: servizi app + db MySQL
└── mvnw / mvnw.cmd                            # Maven Wrapper
```

---

## 5. Configurazione Docker

### 5.1 Analisi del Dockerfile

Il `Dockerfile` implementa una **build multi-stage**, una best practice che ottimizza le dimensioni dell'immagine finale separando il processo di compilazione dall'immagine di runtime.

```dockerfile
# ── STADIO 1: BUILD ────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ── STADIO 2: RUNTIME ──────────────────────────────────────────────
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Analisi dettagliata degli stadi:**

**Stadio 1 — `build`:**
- **Immagine base:** `maven:3.9-eclipse-temurin-21` — contiene Maven 3.9 e JDK 21 (Temurin, distribuzione OpenJDK di Eclipse Foundation).
- **Ottimizzazione della cache Docker:** Il `pom.xml` viene copiato e `mvn dependency:go-offline -B` viene eseguito **separatamente** prima di copiare il codice sorgente. Questo sfrutta il layer caching di Docker: se il codice sorgente cambia ma le dipendenze nel `pom.xml` rimangono invariate, Docker riutilizza il layer delle dipendenze senza riscaricarle.
- **Compilazione:** `mvn clean package -DskipTests` produce il "fat JAR" eseguibile nella directory `target/`.

**Stadio 2 — Runtime:**
- **Immagine base:** `eclipse-temurin:21-jre` — contiene solo il Java Runtime Environment (JRE), senza JDK né Maven. Questo riduce significativamente le dimensioni dell'immagine finale rispetto all'utilizzo della stessa immagine di build.
- **Artefatto copiato:** Solo il file `.jar` prodotto dallo stadio precedente viene copiato tramite `COPY --from=build /app/target/*.jar app.jar`.
- **Porta esposta:** `EXPOSE 8080` — documenta che il container ascolta sulla porta 8080 (la porta di default di Spring Boot Tomcat embedded).
- **Comando di avvio:** `ENTRYPOINT ["java", "-jar", "app.jar"]` — avvia la JVM con il fat JAR.

> **Nota:** La porta esposta nel Dockerfile è `8080`, coerente con il mapping `"8080:8080"` definito nel `docker-compose.yml`. Nell'ambiente Replit la porta è stata configurata a `5000` tramite `server.port=5000` in `application.properties`.

---

### 5.2 Analisi del docker-compose.yml

Il file `docker-compose.yml` definisce l'infrastruttura locale completa dell'applicazione attraverso **2 servizi** distinti.

```yaml
services:
  db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: forum_pugliamare
      MYSQL_PASSWORD: 1234
      MYSQL_ROOT_PASSWORD: 1234
    ports:
      - "3307:3306"
    volumes:
      - db_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 3s
      retries: 10

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/forum_pugliamare
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 1234
    depends_on:
      db:
        condition: service_healthy

volumes:
  db_data:
```

#### Servizio `db` (MySQL 8)

| Parametro | Valore | Descrizione |
|---|---|---|
| `image` | `mysql:8` | Immagine ufficiale MySQL versione 8 |
| `MYSQL_DATABASE` | `forum_pugliamare` | Nome del database creato all'inizializzazione |
| `MYSQL_ROOT_PASSWORD` | `1234` | Password dell'utente `root` (da rafforzare in produzione) |
| `ports` | `"3307:3306"` | Mappa la porta host `3307` alla porta interna del container `3306`. Evita conflitti con eventuali istanze MySQL locali |
| `volumes` | `db_data:/var/lib/mysql` | Monta un **volume nominato Docker** sulla directory dati di MySQL. Garantisce la **persistenza dei dati** tra i riavvii del container |
| `healthcheck` | `mysqladmin ping` ogni 5s | Verifica che MySQL sia pronto ad accettare connessioni prima che il servizio `app` venga avviato |

#### Servizio `app` (Spring Boot)

| Parametro | Valore | Descrizione |
|---|---|---|
| `build` | `.` | Costruisce l'immagine usando il `Dockerfile` nella directory corrente |
| `ports` | `"8080:8080"` | Espone la porta 8080 del container sull'host |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://db:3306/...` | La URL usa `db` come hostname — Docker Compose risolve automaticamente questo nome al container del servizio `db` tramite la **rete interna Docker** (default bridge network). La porta usata è `3306` (porta interna), non `3307` (porta host) |
| `depends_on` | `db: condition: service_healthy` | Il servizio `app` viene avviato **solo dopo** che l'healthcheck del servizio `db` ha restituito successo, evitando errori di connessione al database durante la fase di startup di Hibernate |

#### Volumi

Il volume nominato `db_data` è dichiarato a livello radice del file `docker-compose.yml`. Docker gestisce il lifecycle di questo volume in modo indipendente dai container: i dati MySQL vengono preservati anche in caso di `docker-compose down`, e vengono eliminati solo con il comando esplicito `docker-compose down -v`.

#### Rete

Docker Compose crea automaticamente una **rete bridge dedicata** per tutti i servizi definiti nel file. Questa rete isolata permette ai container di comunicare tra loro tramite il nome del servizio come hostname (es. `db`), senza necessità di configurazioni di rete aggiuntive.

---
