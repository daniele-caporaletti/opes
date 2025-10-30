# Documentazione tecnica — Modello Dati (MVP EUR-only)

## Obiettivi e principi

* **Valuta**: MVP limitato a **EUR**. Gli importi sono salvati in EUR.
* **Importi**: `BigDecimal(18,2)`; segno: **≥ 0** = entrata, **< 0** = uscita.
* **Transfer**: transazioni interne marcate `is_transfer=true` → **escluse** da tutti i KPI.
* **Refund**: rimborsi marcati `is_refund=true` (restano entrate nell’MVP).
* **Auditing**: ogni entità estende `Auditable` con `created_at` / `updated_at`.

---

## Struttura pacchetti

```
com.opes.account
 └─ domain
     ├─ entity
     │   ├─ base/Auditable.java
     │   ├─ AppUser.java
     │   ├─ UserPreference.java
     │   ├─ account/
     │   │   ├─ Account.java
     │   │   └─ AccountBalanceSnapshot.java
     │   ├─ taxonomy/
     │   │   ├─ Category.java
     │   │   ├─ Merchant.java
     │   │   └─ Tag.java
     │   └─ transaction/Transaction.java
     └─ enums/
         ├─ AccountProvider.java
         ├─ CategoryType.java
         └─ TransactionSource.java
```

---

## ER (testuale)

```
AppUser (1) ────< Account (N)
   │                │
   │                └───< AccountBalanceSnapshot (N)
   │
   ├──< Transaction (N) >─── Account (1)
   │         │   │
   │         │   ├── Category (0..1)
   │         │   ├── Merchant (0..1)
   │         │   └──< Transaction_Tag >── Tag (N)
   │
   └──< UserPreference (N)

Category (0..N) può avere parent Category (0..1)
```

---

## Auditing

**`Auditable`** (MappedSuperclass)

* `created_at` (auto: `@CreationTimestamp`)
* `updated_at` (auto: `@UpdateTimestamp`)

Applicato a tutte le entity principali.

---

## Entity

### 1) `AppUser`

**Tabella:** `app_user`
**Scopo:** anagrafica minima utente (id Keycloak come PK).

| Campo      | Tipo           | Note                       |
| ---------- | -------------- | -------------------------- |
| id         | VARCHAR(64) PK | Keycloak `sub`, immutabile |
| email      | VARCHAR        | indicizzata                |
| first_name | VARCHAR        | —                          |
| last_name  | VARCHAR        | —                          |
| birth_date | DATE           | —                          |
| created_at | TIMESTAMP      | auditing                   |
| updated_at | TIMESTAMP      | auditing                   |

**Indici**: `idx_app_user_email (email)`

---

### 2) `Account`

**Tabella:** `account`
**Scopo:** conto collegato all’utente.

| Campo               | Tipo        | Note                       |
| ------------------- | ----------- | -------------------------- |
| id                  | UUID PK     | —                          |
| user_id             | FK→app_user | not null                   |
| provider            | ENUM        | `MANUAL`, `OPEN_FINANCE`   |
| provider_account_id | VARCHAR     | id sorgente (nullable)     |
| name                | VARCHAR     | not null                   |
| currency_code       | CHAR(3)     | **EUR** (check constraint) |
| is_active           | BOOLEAN     | default true               |
| created_at          | TIMESTAMP   | auditing                   |
| updated_at          | TIMESTAMP   | auditing                   |

**Vincoli**:

* `CHECK (currency_code = 'EUR')` (MVP)

**Indici**:

* `idx_account_user (user_id)`
* `idx_account_user_active (user_id, is_active)`

---

### 3) `AccountBalanceSnapshot`

**Tabella:** `account_balance_snapshot`
**Scopo:** storico saldi per calcolo **Total Balance** (ultimo snapshot per conto).

| Campo      | Tipo       | Note                           |
| ---------- | ---------- | ------------------------------ |
| id         | UUID PK    | —                              |
| account_id | FK→account | not null                       |
| as_of      | TIMESTAMP  | not null (timestamp del saldo) |
| balance    | DEC(18,2)  | not null                       |
| created_at | TIMESTAMP  | auditing                       |
| updated_at | TIMESTAMP  | auditing                       |

**Indici**:

* `idx_abs_account_asof (account_id, as_of)`

**Unique**:

* `uk_abs_account_asof (account_id, as_of)` per evitare duplicati sullo stesso istante.

---

### 4) `Transaction`

**Tabella:** `transaction`
**Scopo:** movimento contabile centrale per tutti i KPI.

| Campo             | Tipo           | Note                                          |
| ----------------- | -------------- | --------------------------------------------- |
| id                | UUID PK        | —                                             |
| user_id           | FK→app_user    | not null                                      |
| account_id        | FK→account     | not null                                      |
| booking_date      | DATE           | not null                                      |
| amount            | DEC(18,2)      | not null; **signed** (≥0 entrata, <0 uscita)  |
| description       | TEXT           | opzionale                                     |
| category_id       | FK→category    | opzionale                                     |
| merchant_id       | FK→merchant    | opzionale                                     |
| is_transfer       | BOOLEAN        | default false (escludere dai KPI)             |
| transfer_group_id | VARCHAR        | correlazione A→B                              |
| is_refund         | BOOLEAN        | default false                                 |
| original_tx_id    | FK→transaction | opzionale (riferimento alla spesa originaria) |
| source            | ENUM           | `MANUAL`, `OPEN_FINANCE`                      |
| external_id       | VARCHAR        | id esterno libero (nullable)                  |
| created_at        | TIMESTAMP      | auditing                                      |
| updated_at        | TIMESTAMP      | auditing                                      |

**Indici** (ottimizzati per le viste MVP):

* `idx_tx_user_date (user_id, booking_date)`
* `idx_tx_user_cat_date (user_id, category_id, booking_date)`
* `idx_tx_user_merchant_date (user_id, merchant_id, booking_date)`
* `idx_tx_user_transfer (user_id, is_transfer)`
* `idx_tx_account_date (account_id, booking_date)`

**Relazione Tag**: tabella ponte `transaction_tag` (unique su `(transaction_id, tag_id)`).

---

### 5) `Category`

**Tabella:** `category`
**Scopo:** tassonomia di spesa/entrata; categorie di sistema (user=null) o custom (user valorizzato).

| Campo      | Tipo        | Note                                       |
| ---------- | ----------- | ------------------------------------------ |
| id         | UUID PK     | —                                          |
| user_id    | FK→app_user | **nullable** (null = categoria di sistema) |
| name       | VARCHAR     | not null                                   |
| type       | ENUM        | `INCOME`, `EXPENSE`, `TRANSFER`            |
| parent_id  | FK→category | opzionale (gerarchie)                      |
| created_at | TIMESTAMP   | auditing                                   |
| updated_at | TIMESTAMP   | auditing                                   |

**Indice**: `idx_category_user_type_name (user_id, type, name)`

---

### 6) `Merchant`

**Tabella:** `merchant`
**Scopo:** controparte (sistema o personalizzata).

| Campo      | Tipo        | Note                             |
| ---------- | ----------- | -------------------------------- |
| id         | UUID PK     | —                                |
| user_id    | FK→app_user | **nullable** (null = di sistema) |
| name       | VARCHAR     | not null                         |
| created_at | TIMESTAMP   | auditing                         |
| updated_at | TIMESTAMP   | auditing                         |

**Indice**: `idx_merchant_user_name (user_id, name)`

---

### 7) `Tag`

**Tabella:** `tag`
**Scopo:** etichette libere per transazioni (per filtri/insight).

| Campo      | Tipo        | Note     |
| ---------- | ----------- | -------- |
| id         | UUID PK     | —        |
| user_id    | FK→app_user | not null |
| name       | VARCHAR     | not null |
| created_at | TIMESTAMP   | auditing |
| updated_at | TIMESTAMP   | auditing |

**Unique**: `uk_tag_user_name (user_id, name)`

**Tabella ponte `transaction_tag`**

* `transaction_id` FK→`transaction`
* `tag_id` FK→`tag`
* **Unique**: `(transaction_id, tag_id)`

---

### 8) `UserPreference`

**Tabella:** `user_preference`
**Scopo:** preferenze chiave/valore per utente (es. scelte UI, default periodo).

| Campo      | Tipo        | Note     |
| ---------- | ----------- | -------- |
| id         | UUID PK     | —        |
| user_id    | FK→app_user | not null |
| pref_key   | VARCHAR(64) | not null |
| pref_value | VARCHAR(64) | not null |
| created_at | TIMESTAMP   | auditing |
| updated_at | TIMESTAMP   | auditing |

**Unique**: `uk_userpref_user_key (user_id, pref_key)`
**Indice**: `idx_userpref_user (user_id)`

---

## Enum

* **`AccountProvider`**: `MANUAL`, `OPEN_FINANCE`
* **`CategoryType`**: `INCOME`, `EXPENSE`, `TRANSFER`
* **`TransactionSource`**: `MANUAL`, `OPEN_FINANCE`

---

## Vincoli chiave per gli analytics

* **EUR-only**: `CHECK (currency_code = 'EUR')` su `account`.
* **Esclusione transfer**: campo boolean `is_transfer`, indicizzato per filtri veloci.
* **Snapshot saldo**: `UNIQUE (account_id, as_of)` → l’ultimo per conto determina il **Total Balance**.
* **Query frequenti**: indici su combinazioni `user_id + booking_date`, `category/merchant`, `account`.

