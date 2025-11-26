package com.opex.financialappbackend.config;

import com.opex.financialappbackend.domain.Account;
import com.opex.financialappbackend.domain.Transaction;
import com.opex.financialappbackend.domain.User;
import com.opex.financialappbackend.domain.enums.AccountType; // <--- Importante
import com.opex.financialappbackend.domain.enums.TransactionCategory;
import com.opex.financialappbackend.domain.enums.TransactionStatus;
import com.opex.financialappbackend.domain.enums.TransactionType;
import com.opex.financialappbackend.repository.AccountRepository;
import com.opex.financialappbackend.repository.TransactionRepository;
import com.opex.financialappbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final String DEMO_USER_ID = "demo-user-123";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Se ci sono già conti, evitiamo di duplicare i dati ad ogni riavvio
        if (accountRepository.count() > 0) {
            return;
        }

        System.out.println("🌱 Seeding database with RICH mock financial data...");

        // 1. Crea o Recupera Utente Demo
        User user = userRepository.findById(DEMO_USER_ID)
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(DEMO_USER_ID)
                        .email("demo@opex.com")
                        .firstName("Demo User")
                        .birthDate(LocalDate.of(1995, 5, 20))
                        .build()));

        // 2. Crea Conto Principale
        Account account = Account.builder()
                .user(user)
                .name("Main Bank Account")
                .institutionName("Revolut")
                .currency("EUR")
                .balance(new BigDecimal("4250.50"))
                .type(AccountType.BANK_ACCOUNT) // <--- Settato il tipo conto
                .build();
        accountRepository.save(account);

        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random random = new Random();

        // --- A. MOVIMENTI RICORRENTI (Ultimi 6 Mesi) ---
        for (int i = 0; i < 6; i++) {
            LocalDate monthDate = today.minusMonths(i);

            // 1. Stipendio
            transactions.add(createTransaction(user, account,
                    monthDate.withDayOfMonth(27),
                    new BigDecimal("2600.00"), TransactionType.INCOME, TransactionCategory.SALARY, "Tech Corp Salary"));

            // 2. Affitto
            transactions.add(createTransaction(user, account,
                    monthDate.withDayOfMonth(1),
                    new BigDecimal("-850.00"), TransactionType.EXPENSE, TransactionCategory.HOUSING, "Landlord Rent"));

            // 3. Bollette
            transactions.add(createTransaction(user, account,
                    monthDate.withDayOfMonth(15),
                    new BigDecimal("-35.90"), TransactionType.EXPENSE, TransactionCategory.UTILITIES, "Vodafone Fiber"));

            // 4. Svago ricorrente
            transactions.add(createTransaction(user, account,
                    monthDate.withDayOfMonth(5),
                    new BigDecimal("-12.99"), TransactionType.EXPENSE, TransactionCategory.LEISURE, "Netflix Subscription"));
        }

        // --- B. ENTRATE EXTRA ---
        for (int i = 0; i < 3; i++) {
            LocalDate randomDate = today.minusDays(random.nextInt(150));
            transactions.add(createTransaction(user, account,
                    randomDate,
                    new BigDecimal("450.00"), TransactionType.INCOME, TransactionCategory.FREELANCE_INCOME, "Upwork Project"));
        }

        // --- C. SPESE VARIABILI (~150 transazioni) ---
        for (int i = 0; i < 150; i++) {
            int daysBack = random.nextInt(180);
            LocalDate date = today.minusDays(daysBack);

            if (date.isAfter(today)) date = today;

            TransactionCategory cat;
            String merchant;
            BigDecimal amount;
            int categoryPick = random.nextInt(100);

            if (categoryPick < 30) {
                cat = TransactionCategory.GROCERIES;
                String[] merchants = {"Esselunga", "Coop", "Lidl", "Carrefour"};
                merchant = merchants[random.nextInt(merchants.length)];
                amount = new BigDecimal("-" + (15 + random.nextInt(100)));
            } else if (categoryPick < 50) {
                cat = TransactionCategory.DINING_OUT;
                String[] merchants = {"Pizza Napoli", "Starbucks", "Sushi Wok", "Burger King", "Trattoria"};
                merchant = merchants[random.nextInt(merchants.length)];
                amount = new BigDecimal("-" + (5 + random.nextInt(60)));
            } else if (categoryPick < 65) {
                cat = TransactionCategory.TRANSPORT;
                String[] merchants = {"Uber", "Trenitalia", "Gas Station", "Bus Ticket"};
                merchant = merchants[random.nextInt(merchants.length)];
                amount = new BigDecimal("-" + (5 + random.nextInt(40)));
            } else if (categoryPick < 80) {
                cat = TransactionCategory.SHOPPING;
                String[] merchants = {"Amazon", "Zalando", "Nike", "IKEA"};
                merchant = merchants[random.nextInt(merchants.length)];
                amount = new BigDecimal("-" + (20 + random.nextInt(150)));
            } else if (categoryPick < 90) {
                cat = TransactionCategory.LEISURE;
                String[] merchants = {"Cinema City", "Steam Games", "Padel Club", "Concert Ticket"};
                merchant = merchants[random.nextInt(merchants.length)];
                amount = new BigDecimal("-" + (15 + random.nextInt(50)));
            } else {
                cat = TransactionCategory.UTILITIES;
                merchant = "Pharmacy / Generic";
                amount = new BigDecimal("-" + (10 + random.nextInt(30)));
            }

            transactions.add(createTransaction(user, account, date, amount, TransactionType.EXPENSE, cat, merchant));
        }

        // --- D. UNA TANTUM ---
        transactions.add(createTransaction(user, account,
                today.minusMonths(2).withDayOfMonth(10),
                new BigDecimal("-1200.00"), TransactionType.EXPENSE, TransactionCategory.LEISURE, "Airbnb Holiday"));

        transactionRepository.saveAll(transactions);
        System.out.println("✅ Database seeded with " + transactions.size() + " rich transactions.");
    }

    private Transaction createTransaction(User user, Account account, LocalDate date, BigDecimal amount,
                                          TransactionType type, TransactionCategory cat, String merchant) {

        // Logica Random per lo Status
        TransactionStatus status = TransactionStatus.COMPLETED;
        if (date.equals(LocalDate.now())) {
            // 30% chance di essere PENDING se è oggi
            status = new Random().nextInt(10) < 3 ? TransactionStatus.PENDING : TransactionStatus.COMPLETED;
        }

        return Transaction.builder()
                .user(user)
                .account(account)
                .bookingDate(date)
                .amount(amount)
                .type(type)
                .category(cat)
                .status(status) // <--- Status corretto
                .merchantName(merchant)
                .description(merchant + " Payment")
                .excluded(false)
                .build();
    }
}