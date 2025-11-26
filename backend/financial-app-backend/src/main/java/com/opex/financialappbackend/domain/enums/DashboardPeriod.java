package com.opex.financialappbackend.domain.enums;

public enum DashboardPeriod {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
    LAST_12_MONTHS(365);

    private final int days;

    DashboardPeriod(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}