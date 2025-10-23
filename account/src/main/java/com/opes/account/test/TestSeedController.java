package com.opes.account.test;

import com.opes.account.entity.*;
import com.opes.account.entity.Transaction.Kind;
import com.opes.account.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestSeedController {

    private final AccountRepository accRepo;
    private final FinancialAccountRepository faRepo;
    private final TransactionRepository txRepo;
    private final GoalRepository goalRepo;

    @PostMapping("/seed")
    public ResponseEntity<?> seed(@RequestParam Long accountId) {

        var account = accRepo.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        var fa1 = faRepo.save(FinancialAccount.builder()
                .account(account).name("Main Current").type("CURRENT")
                .balanceCents(5234_00).balanceUpdatedAt(Instant.now()).excludedFromTotal(false).build());

        var fa2 = faRepo.save(FinancialAccount.builder()
                .account(account).name("Savings").type("SAVINGS")
                .balanceCents(1278_00).balanceUpdatedAt(Instant.now()).excludedFromTotal(false).build());

        ZoneId tz = ZoneId.of("Europe/Zurich");
        var today = LocalDate.now(tz);
        var d = (java.util.function.IntFunction<Instant>) days ->
                today.minusDays(days).atStartOfDay(tz).toInstant();

        var txs = List.of(
                // INCOME
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(3))
                        .amountCents(2_500_00).kind(Kind.INCOME).categoryCode("SALARY").description("Monthly salary").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(18))
                        .amountCents(200_00).kind(Kind.INCOME).categoryCode("OTHER").description("Gift").build(),
                // EXPENSE (negativi)
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(1))
                        .amountCents(-12_99).kind(Kind.EXPENSE).categoryCode("FOOD_GROCERIES").merchantName("Coop").description("Groceries").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(2))
                        .amountCents(-34_50).kind(Kind.EXPENSE).categoryCode("TRANSPORT").merchantName("SBB").description("Train").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(6))
                        .amountCents(-980_00).kind(Kind.EXPENSE).categoryCode("RENT").merchantName("Landlord").description("Rent").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(10))
                        .amountCents(-45_00).kind(Kind.EXPENSE).categoryCode("RESTAURANT").merchantName("Pizzeria").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(12))
                        .amountCents(-60_00).kind(Kind.EXPENSE).categoryCode("UTILITIES").merchantName("Electric").build(),
                Transaction.builder().account(account).financialAccount(fa1).bookingTs(d.apply(20))
                        .amountCents(-120_00).kind(Kind.EXPENSE).categoryCode("SHOPPING").merchantName("Zara").build()
        );
        txRepo.saveAll(txs);

        goalRepo.save(Goal.builder()
                .account(account).title("Trip to Lisbon").targetCents(1_500_00).savedCents(350_00)
                .status(Goal.Status.ACTIVE).build());

        return ResponseEntity.ok("Seed OK for accountId=" + accountId);
    }
}
