package com.concours.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(ApplicationConstants.DATE_FORMAT);

    private DateUtil() {
        // Classe utilitaire - constructeur privé
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date invalide: " + dateStr);
        }
    }

    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }

        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static boolean isConcoursOuvert(LocalDate dateOuverture, LocalDate dateCloture) {
        LocalDate maintenant = LocalDate.now();
        return isDateInRange(maintenant, dateOuverture, dateCloture);
    }
}