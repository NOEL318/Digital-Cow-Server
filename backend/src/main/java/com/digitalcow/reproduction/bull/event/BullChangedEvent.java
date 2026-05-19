package com.digitalcow.reproduction.bull.event;

/** Evento que dispara invalidacion de caches reproductivos al cambiar un Bull. */
public record BullChangedEvent(Long accountId) { }
