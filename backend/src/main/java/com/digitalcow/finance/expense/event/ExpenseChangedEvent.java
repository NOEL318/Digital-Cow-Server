package com.digitalcow.finance.expense.event;

/** Evento publicado tras crear/actualizar/eliminar Expense. */
public record ExpenseChangedEvent(Long accountId) { }
