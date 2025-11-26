package com.opex.financialappbackend.domain.enums;

public enum TransactionCategory {
    SALARY,
    FREELANCE_INCOME,
    HOUSING,        // Affitto, mutuo
    GROCERIES,      // Spesa
    TRANSPORT,      // Benzina, Treni
    DINING_OUT,     // Ristoranti
    LEISURE,        // Svago, Netflix
    SHOPPING,
    TAXES,
    UTILITIES,      // Bollette
    TRANSFER        // Giroconti (da escludere dai calcoli di spesa)
}