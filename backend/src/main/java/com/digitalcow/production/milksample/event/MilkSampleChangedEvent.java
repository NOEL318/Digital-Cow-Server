package com.digitalcow.production.milksample.event;

/** Evento publicado tras crear/actualizar/eliminar MilkSample. */
public record MilkSampleChangedEvent(Long accountId) { }
