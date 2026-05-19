package com.digitalcow.reproduction.dryoff.event;

/** Evento publicado tras crear/actualizar/eliminar DryOff. */
public record DryOffChangedEvent(Long accountId) { }
