package com.digitalcow.reproduction.semen.event;

/** Evento que dispara invalidacion de caches reproductivos al cambiar una pajilla. */
public record SemenStrawChangedEvent(Long accountId) { }
