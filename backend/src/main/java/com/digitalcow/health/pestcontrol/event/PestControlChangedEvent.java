package com.digitalcow.health.pestcontrol.event;

/** Evento que dispara invalidacion de caches dashboard/alerts. */
public record PestControlChangedEvent(Long accountId) { }
