package com.digitalcow.health.diagnosis.event;

/** Evento que dispara invalidacion de caches dashboard/alerts. */
public record DiagnosisChangedEvent(Long accountId) { }
