package com.digitalcow.health.vaccination.event;

/** Evento que dispara invalidacion de caches dashboard/alerts. */
public record VaccinationChangedEvent(Long accountId) { }
