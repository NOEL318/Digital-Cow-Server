package com.digitalcow.production.milking.event;

/** Evento publicado tras crear/actualizar/eliminar Milking. */
public record MilkingChangedEvent(Long accountId) { }
