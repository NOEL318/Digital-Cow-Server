package com.digitalcow.production.slaughter.event;

/** Evento publicado tras crear/actualizar/eliminar SlaughterResult. */
public record SlaughterResultChangedEvent(Long accountId) { }
