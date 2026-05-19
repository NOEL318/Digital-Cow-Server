package com.digitalcow.health.treatment.event;

/** Evento que dispara invalidacion de caches dashboard/alerts. */
public record TreatmentChangedEvent(Long accountId) { }
