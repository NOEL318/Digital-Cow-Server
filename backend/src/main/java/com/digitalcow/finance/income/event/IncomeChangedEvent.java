package com.digitalcow.finance.income.event;

/** Evento publicado tras crear/actualizar/eliminar Income. */
public record IncomeChangedEvent(Long accountId) { }
