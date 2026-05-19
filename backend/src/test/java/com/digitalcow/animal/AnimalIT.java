package com.digitalcow.animal;

import com.digitalcow.AbstractIT;
import com.digitalcow.account.*;
import com.digitalcow.tenancy.TenantContext;
import com.digitalcow.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalIT extends AbstractIT {

    @Autowired AccountRepository accounts;
    @Autowired AppUserRepository users;
    @Autowired com.digitalcow.ranch.RanchRepository ranches;
    @Autowired AnimalRepository animals;

    @AfterEach void clear() { TenantContext.clear(); }

    @Test
    void shouldIsolateAnimalsAcrossAccounts() {
        Account a1 = newAccount("acc1");
        Account a2 = newAccount("acc2");

        TenantContext.set(a1.getId());
        Long r1 = newRanch();
        Long b1 = ((com.digitalcow.breed.BreedRepository) breedRepo()).findAll().get(0).getId();
        newAnimal(a1.getId(), r1, b1, "A1-TAG-1");

        TenantContext.set(a2.getId());
        Long r2 = newRanch();
        newAnimal(a2.getId(), r2, b1, "A2-TAG-1");

        TenantContext.set(a2.getId());
        assertThat(animals.findAll()).allMatch(an -> an.getAccountId().equals(a2.getId()));

        TenantContext.set(a1.getId());
        assertThat(animals.findAll()).allMatch(an -> an.getAccountId().equals(a1.getId()));
    }

    @Autowired com.digitalcow.breed.BreedRepository breedRepoBean;
    com.digitalcow.breed.BreedRepository breedRepo() { return breedRepoBean; }

    private Account newAccount(String slug) {
        Account a = new Account();
        a.setName(slug);
        a.setSlug(slug + "-" + System.nanoTime());
        return accounts.save(a);
    }

    private Long newRanch() {
        com.digitalcow.ranch.Ranch r = new com.digitalcow.ranch.Ranch();
        r.setName("R");
        return ranches.save(r).getId();
    }

    private Long newAnimal(Long accId, Long ranchId, Long breedId, String tag) {
        Animal an = new Animal();
        an.setAccountId(accId);
        an.setRanchId(ranchId);
        an.setBreedId(breedId);
        an.setInternalTag(tag);
        an.setSex(Sex.FEMALE);
        an.setPurpose(Purpose.BEEF);
        an.setStatus(AnimalStatus.ACTIVE);
        an.setCreatedByUserId(1L);
        return animals.save(an).getId();
    }
}
