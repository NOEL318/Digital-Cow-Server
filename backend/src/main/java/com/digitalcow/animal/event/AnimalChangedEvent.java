package com.digitalcow.animal.event;

/** Evento publicado tras crear/actualizar/eliminar animal, para invalidar caches. */
public record AnimalChangedEvent(Long accountId) {}
