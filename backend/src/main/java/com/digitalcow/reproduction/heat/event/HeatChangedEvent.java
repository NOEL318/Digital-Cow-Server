package com.digitalcow.reproduction.heat.event;

/** Evento publicado tras crear/actualizar/eliminar Heat. */
public record HeatChangedEvent(Long accountId) { }
