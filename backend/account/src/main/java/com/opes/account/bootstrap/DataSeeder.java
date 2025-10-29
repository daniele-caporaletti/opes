// com/opes/account/bootstrap/DataSeeder.java
package com.opes.account.bootstrap;

import com.opes.account.domain.entity.AppUser;
import com.opes.account.domain.entity.account.Account;
import com.opes.account.domain.entity.account.AccountBalanceSnapshot;
import com.opes.account.domain.entity.taxonomy.Category;
import com.opes.account.domain.entity.taxonomy.Merchant;
import com.opes.account.domain.entity.taxonomy.Tag;
import com.opes.account.domain.entity.transaction.Transaction;
import com.opes.account.domain.enums.AccountProvider;
import com.opes.account.domain.enums.CategoryType;
import com.opes.account.domain.enums.TransactionSource;
import com.opes.account.domain.enums.onboarding.*;
import com.opes.account.repository.AppUserRepository;
import com.opes.account.repository.taxonomy.CategoryRepository;
import com.opes.account.repository.taxonomy.MerchantRepository;
import com.opes.account.repository.taxonomy.TagRepository;
import com.opes.account.repository.transaction.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;

@Component
@ConditionalOnProperty(name = "app.seed.enable", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final ZoneId ZONE = ZoneId.of("Europe/Rome");
    private static final String USER_ID = "demo-user-123";

    private final AppUserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final MerchantRepository merchantRepo;
    private final TagRepository tagRepo;
    private final TransactionRepository txRepo;

    @PersistenceContext
    private EntityManager em;

    public DataSeeder(AppUserRepository userRepo,
                      CategoryRepository categoryRepo,
                      MerchantRepository merchantRepo,
                      TagRepository tagRepo,
                      TransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.merchantRepo = merchantRepo;
        this.tagRepo = tagRepo;
        this.txRepo = txRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Evita di riseminare se esistono già transazioni dell'utente
        long existing = em.createQuery(
                        "select count(t) from Transaction t where t.user.id = :uid", Long.class)
                .setParameter("uid", USER_ID)
                .getSingleResult();
        if (existing > 0) return;

        // 1) Utente dev (Keycloak sub finto)
        AppUser user = userRepo.findById(USER_ID).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setId(USER_ID);
            u.setEmail("alex@example.com");
            u.setFirstName("Alex");
            u.setLastName("Rossi");
            u.setBirthDate(LocalDate.of(1994, 6, 12));
            u.setSparkSelfRecognition(SparkChoice.OK_SAVE_MORE);
            u.setEmotionalGoal(EmotionalGoal.TRAVEL);
            u.setEmotionalGoalOther(null);
            u.setCurrentSituation(CurrentSituation.FULL_TIME);
            u.setMonthlyIncome(MonthlyIncome.OVER_1500);
            return userRepo.save(u);
        });

        // 2) Account (EUR-only)
        Account conto = new Account();
        conto.setUser(user);
        conto.setProvider(AccountProvider.OPEN_FINANCE);
        conto.setProviderAccountId("bank_acc_demo_001");
        conto.setName("Conto Corrente");
        conto.setCurrencyCode("EUR");
        conto.setActive(true);
        em.persist(conto);

        Account wallet = new Account();
        wallet.setUser(user);
        wallet.setProvider(AccountProvider.MANUAL);
        wallet.setProviderAccountId(null);
        wallet.setName("Portafoglio");
        wallet.setCurrencyCode("EUR");
        wallet.setActive(true);
        em.persist(wallet);

        // 3) Categorie di sistema
        Map<String, Category> cats = ensureCategories();

        // 4) Merchant di sistema (user null)
        Map<String, Merchant> merchants = ensureMerchants();

        // 5) Tag dell'utente
        Map<String, Tag> tags = ensureTags(user);

        // 6) Genera transazioni su 3 mesi (mese corrente fino a ieri + due mesi precedenti)
        LocalDate today = LocalDate.now(ZONE);
        LocalDate mtdStart = today.withDayOfMonth(1);
        LocalDate mtdEnd = today.minusDays(1);
        if (mtdEnd.isBefore(mtdStart)) mtdEnd = mtdStart;

        LocalDate prevStart = mtdStart.minusMonths(1);
        LocalDate prevEnd = prevStart.plusDays(mtdEnd.toEpochDay() - mtdStart.toEpochDay());
        LocalDate prev2Start = mtdStart.minusMonths(2);
        LocalDate prev2End = prev2Start.plusDays(mtdEnd.toEpochDay() - mtdStart.toEpochDay());

        Random rnd = new Random(42L);

        // Per ogni mese: stipendio, affitto, abbonamenti fissi, spese random giornaliere, trasferimenti, rimborsi casuali
        seedMonth(conto, wallet, user, cats, merchants, tags, rnd, prev2Start, prev2End, 0.9);
        seedMonth(conto, wallet, user, cats, merchants, tags, rnd, prevStart, prevEnd, 1.0);
        seedMonth(conto, wallet, user, cats, merchants, tags, rnd, mtdStart, mtdEnd, 1.1); // leggera crescita

        // 7) Snapshot saldi (somma "realistica")
        snapshot(conto, new BigDecimal("1750.00"), LocalDateTime.of(today.getYear(), today.getMonth(), Math.max(1, mtdEnd.getDayOfMonth()), 18, 0));
        snapshot(wallet, new BigDecimal("120.00"), LocalDateTime.of(today.getYear(), today.getMonth(), Math.max(1, mtdEnd.getDayOfMonth()), 18, 0));
    }

    // ------------------------ helpers ------------------------

    private Map<String, Category> ensureCategories() {
        Map<String, Category> map = new LinkedHashMap<>();
        map.put("SALARY", persistCategory(null, "Salary", CategoryType.INCOME));
        map.put("RENT", persistCategory(null, "Rent", CategoryType.EXPENSE));
        map.put("GROCERIES", persistCategory(null, "Groceries", CategoryType.EXPENSE));
        map.put("RESTAURANTS", persistCategory(null, "Restaurants", CategoryType.EXPENSE));
        map.put("TRANSPORT", persistCategory(null, "Transport", CategoryType.EXPENSE));
        map.put("UTILITIES", persistCategory(null, "Utilities", CategoryType.EXPENSE));
        map.put("ENTERTAINMENT", persistCategory(null, "Entertainment", CategoryType.EXPENSE));
        map.put("HEALTHCARE", persistCategory(null, "Healthcare", CategoryType.EXPENSE));
        map.put("SUBSCRIPTIONS", persistCategory(null, "Subscriptions", CategoryType.EXPENSE));
        map.put("EDUCATION", persistCategory(null, "Education", CategoryType.EXPENSE));
        map.put("MISC", persistCategory(null, "Misc", CategoryType.EXPENSE));
        map.put("TRANSFER", persistCategory(null, "Transfer", CategoryType.TRANSFER));
        return map;
    }

    private Category persistCategory(AppUser user, String name, CategoryType type) {
        Category c = new Category();
        c.setUser(user); // null = di sistema
        c.setName(name);
        c.setType(type);
        em.persist(c);
        return c;
    }

    private Map<String, Merchant> ensureMerchants() {
        String[] names = {
                "Esselunga", "Carrefour", "Coop", "IKEA", "Decathlon", "MediaWorld", "Amazon",
                "Netflix", "Spotify", "Apple", "Google", "Uber", "Bolt", "ENEL Energia", "TIM",
                "Vodafone", "Trenitalia", "Italo", "Autostrade", "Just Eat", "Deliveroo",
                "Zara", "H&M", "Primark", "UniCredit", "Landlord", "Webflow", "Figma",
                "Adobe", "Steam", "Nintendo", "PlayStation", "Airbnb", "Booking", "Ryanair",
                "EasyJet", "Shell", "Eni", "Starbucks", "McDonald's"
        };
        Map<String, Merchant> map = new LinkedHashMap<>();
        for (String n : names) {
            Merchant m = new Merchant();
            m.setUser(null); // di sistema
            m.setName(n);
            em.persist(m);
            map.put(n, m);
        }
        return map;
    }

    private Map<String, Tag> ensureTags(AppUser user) {
        String[] names = {"Family", "Takeout", "Work", "Gym", "Gift", "Travel", "Health", "Utilities", "Subscriptions", "Entertainment", "Groceries"};
        Map<String, Tag> map = new LinkedHashMap<>();
        for (String n : names) {
            Tag t = new Tag();
            t.setUser(user);
            t.setName(n);
            em.persist(t);
            map.put(n, t);
        }
        return map;
    }

    private void seedMonth(Account conto, Account wallet, AppUser user,
                           Map<String, Category> cats,
                           Map<String, Merchant> merchants,
                           Map<String, Tag> tags,
                           Random rnd,
                           LocalDate start, LocalDate end,
                           double expenseMultiplier) {

        if (end.isBefore(start)) return;

        // Stipendio 1° del mese
        persistTx(user, conto, start.withDayOfMonth(1), pos(2500, 4000, rnd), "Stipendio", cats.get("SALARY"), merchants.get("UniCredit"), false, null, false, null, TransactionSource.OPEN_FINANCE, "salary-" + start);

        // Affitto 3 del mese
        persistTx(user, conto, clampDate(start, 3), neg(800, 1200, rnd).multiply(BigDecimal.valueOf(expenseMultiplier)), "Affitto", cats.get("RENT"), merchants.get("Landlord"), false, null, false, null, TransactionSource.OPEN_FINANCE, "rent-" + start);

        // Abbonamenti fissi (Netflix, Spotify, Webflow/Figma random)
        persistTx(user, conto, clampDate(start, 8), neg(7, 15, rnd), "Spotify", cats.get("SUBSCRIPTIONS"), merchants.get("Spotify"), false, null, false, null, TransactionSource.OPEN_FINANCE, null);
        persistTx(user, conto, clampDate(start, 10), neg(10, 20, rnd), "Netflix", cats.get("SUBSCRIPTIONS"), merchants.get("Netflix"), false, null, false, null, TransactionSource.OPEN_FINANCE, null);
        if (rnd.nextBoolean())
            persistTx(user, conto, clampDate(start, 12), neg(10, 30, rnd), "Webflow", cats.get("SUBSCRIPTIONS"), merchants.get("Webflow"), false, null, false, null, TransactionSource.OPEN_FINANCE, null);
        if (rnd.nextBoolean())
            persistTx(user, conto, clampDate(start, 14), neg(10, 30, rnd), "Figma", cats.get("SUBSCRIPTIONS"), merchants.get("Figma"), false, null, false, null, TransactionSource.OPEN_FINANCE, null);

        // Trasferimenti settimanali verso Portafoglio
        LocalDate d = start.with(DayOfWeek.FRIDAY);
        while (!d.isAfter(end)) {
            String gid = "TR-" + d.toString();
            persistTx(user, conto, d, new BigDecimal("-100.00"), "Trasferimento verso Portafoglio", cats.get("TRANSFER"), null, true, gid, false, null, TransactionSource.MANUAL, null);
            persistTx(user, wallet, d, new BigDecimal("100.00"), "Trasferimento da Conto", cats.get("TRANSFER"), null, true, gid, false, null, TransactionSource.MANUAL, null);
            d = d.plusWeeks(1);
        }

        // Spese giornaliere random
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            int count = rnd.nextInt(5); // 0..4 movimenti
            for (int i = 0; i < count; i++) {
                Category cat = pickExpenseCategory(cats, rnd);
                Merchant mch = pickMerchantForCategory(cat, merchants, rnd);
                BigDecimal amount = neg(5, 120, rnd).multiply(BigDecimal.valueOf(expenseMultiplier));
                String desc = buildDescription(cat, mch, rnd);
                Transaction t = persistTx(user, conto, day, amount, desc, cat, mch, false, null, false, null, TransactionSource.OPEN_FINANCE, null);
                // tag random 0..2
                assignRandomTags(t, tags, rnd);
                // rimborso casuale (5% delle volte) qualche giorno dopo
                if (!"TRANSFER".equals(cat.getName()) && rnd.nextDouble() < 0.05) {
                    LocalDate refDay = day.plusDays(rnd.nextInt(10) + 1);
                    if (!refDay.isAfter(end)) {
                        persistTx(user, conto, refDay, amount.abs().min(new BigDecimal("30.00")), "Rimborso " + desc, cat, mch, false, null, true, t, TransactionSource.OPEN_FINANCE, null);
                    }
                }
            }
            // Trasporti occasionali
            if (rnd.nextDouble() < 0.25) {
                Merchant carrier = rnd.nextBoolean() ? merchants.get("Trenitalia") : merchants.get("Italo");
                persistTx(user, conto, day, neg(10, 60, rnd), "Trasporto", cats.get("TRANSPORT"), carrier, false, null, false, null, TransactionSource.OPEN_FINANCE, null);
            }
        }
    }

    private void snapshot(Account account, BigDecimal balance, LocalDateTime asOf) {
        AccountBalanceSnapshot s = new AccountBalanceSnapshot();
        s.setAccount(account);
        s.setBalance(balance.setScale(2, RoundingMode.HALF_UP));
        s.setAsOf(asOf);
        em.persist(s);
    }

    private Category pickExpenseCategory(Map<String, Category> cats, Random rnd) {
        String[] keys = {"GROCERIES","RESTAURANTS","TRANSPORT","UTILITIES","ENTERTAINMENT","HEALTHCARE","EDUCATION","MISC"};
        return cats.get(keys[rnd.nextInt(keys.length)]);
    }

    private Merchant pickMerchantForCategory(Category cat, Map<String, Merchant> merchants, Random rnd) {
        String n = cat.getName();
        List<String> pool = switch (n) {
            case "Groceries" -> List.of("Esselunga","Carrefour","Coop");
            case "Restaurants" -> List.of("Just Eat","Deliveroo","McDonald's","Starbucks");
            case "Transport" -> List.of("Uber","Bolt","Trenitalia","Italo","Autostrade");
            case "Utilities" -> List.of("ENEL Energia","TIM","Vodafone");
            case "Entertainment" -> List.of("Steam","Nintendo","PlayStation","Netflix","Spotify");
            case "Healthcare" -> List.of("Amazon","IKEA"); // farmaci su Amazon ecc.
            case "Education" -> List.of("Adobe","Figma","Webflow");
            default -> List.of("Amazon","MediaWorld","Decathlon","IKEA","Zara","H&M");
        };
        return merchants.get(pool.get(rnd.nextInt(pool.size())));
    }

    private String buildDescription(Category cat, Merchant mch, Random rnd) {
        String base = switch (cat.getName()) {
            case "Groceries" -> "Spesa";
            case "Restaurants" -> "Takeaway";
            case "Transport" -> "Viaggio";
            case "Utilities" -> "Bolletta";
            case "Entertainment" -> "Svago";
            case "Healthcare" -> "Salute";
            case "Education" -> "Formazione";
            default -> "Acquisto";
        };
        return base + " " + mch.getName();
    }

    private void assignRandomTags(Transaction t, Map<String, Tag> tags, Random rnd) {
        if (rnd.nextDouble() < 0.3) t.getTags().add(tags.get("Groceries"));
        if (rnd.nextDouble() < 0.2) t.getTags().add(tags.get("Takeout"));
        if (rnd.nextDouble() < 0.15) t.getTags().add(tags.get("Entertainment"));
        if (rnd.nextDouble() < 0.1) t.getTags().add(tags.get("Utilities"));
        if (rnd.nextDouble() < 0.1) t.getTags().add(tags.get("Travel"));
        if (rnd.nextDouble() < 0.05) t.getTags().add(tags.get("Work"));
        em.merge(t);
    }

    private LocalDate clampDate(LocalDate monthStart, int dayOfMonth) {
        int dom = Math.min(dayOfMonth, monthStart.lengthOfMonth());
        return LocalDate.of(monthStart.getYear(), monthStart.getMonth(), dom);
    }

    private Transaction persistTx(AppUser user,
                                  Account account,
                                  LocalDate date,
                                  BigDecimal amount,
                                  String description,
                                  Category category,
                                  Merchant merchant,
                                  boolean transfer,
                                  String transferGroupId,
                                  boolean refund,
                                  Transaction original,
                                  TransactionSource source,
                                  String externalId) {
        Transaction t = new Transaction();
        t.setUser(user);
        t.setAccount(account);
        t.setBookingDate(date);
        t.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        t.setDescription(description);
        t.setCategory(category);
        t.setMerchant(merchant);
        t.setTransfer(transfer);
        t.setTransferGroupId(transferGroupId);
        t.setRefund(refund);
        t.setOriginalTransaction(original);
        t.setSource(source);
        t.setExternalId(externalId);
        em.persist(t);
        return t;
    }

    private BigDecimal neg(int min, int max, Random rnd) {
        int cents = (min * 100) + rnd.nextInt((max - min + 1) * 100);
        return BigDecimal.valueOf(-cents, 2);
    }

    private BigDecimal pos(int min, int max, Random rnd) {
        int cents = (min * 100) + rnd.nextInt((max - min + 1) * 100);
        return BigDecimal.valueOf(cents, 2);
    }
}
