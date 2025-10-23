package com.opes.account.home;

import com.opes.account.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService svc;

    @GetMapping("/total-balance")
    public ResponseEntity<?> totalBalance(@RequestParam Long accountId) {
        return ResponseEntity.ok(
                java.util.Map.of("currency","EUR","totalCents", svc.totalBalanceCents(accountId))
        );
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<?> weeklySummary(@RequestParam Long accountId) {
        return ResponseEntity.ok(svc.weeklySummary(accountId));
    }

    @GetMapping("/total-income")
    public ResponseEntity<?> totalIncome(@RequestParam Long accountId,
                                         @RequestParam(defaultValue = "MONTH") HomeService.Period period) {
        return ResponseEntity.ok(
                java.util.Map.of("currency","EUR","period",period,
                        "totalCents", svc.totalByKindCents(accountId, Transaction.Kind.INCOME, period))
        );
    }

    @GetMapping("/total-expenses")
    public ResponseEntity<?> totalExpenses(@RequestParam Long accountId,
                                           @RequestParam(defaultValue = "MONTH") HomeService.Period period) {
        return ResponseEntity.ok(
                java.util.Map.of("currency","EUR","period",period,
                        "totalCents", svc.totalByKindCents(accountId, Transaction.Kind.EXPENSE, period))
        );
    }

    @GetMapping("/spending-snapshot")
    public ResponseEntity<?> spendingSnapshot(@RequestParam Long accountId) {
        return ResponseEntity.ok(svc.spendingSnapshot(accountId));
    }

    @GetMapping("/goals")
    public ResponseEntity<?> goals(@RequestParam Long accountId) {
        return ResponseEntity.ok(svc.activeGoals(accountId));
    }
}
