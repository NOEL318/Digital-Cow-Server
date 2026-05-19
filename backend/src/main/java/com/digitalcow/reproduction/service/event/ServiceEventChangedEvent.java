package com.digitalcow.reproduction.service.event;

/** Evento publicado tras crear/actualizar/eliminar ServiceEvent. */
public record ServiceEventChangedEvent(Long accountId) { }
