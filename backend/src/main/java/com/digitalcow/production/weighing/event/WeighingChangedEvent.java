package com.digitalcow.production.weighing.event;

/** Evento publicado tras crear/actualizar/eliminar Weighing. */
public record WeighingChangedEvent(Long accountId) { }
