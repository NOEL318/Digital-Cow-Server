package com.digitalcow.breed;

import org.springframework.data.jpa.repository.JpaRepository;

/** Este repositorio consulta las razas registradas en el sistema. */
public interface BreedRepository extends JpaRepository<Breed, Long> {}
