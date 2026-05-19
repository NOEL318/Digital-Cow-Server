package com.digitalcow.account;

import com.digitalcow.account.dto.*;
import com.digitalcow.common.web.CurrentUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Endpoints del Account del tenant actual. */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountRepository repo;

    public AccountController(AccountRepository repo) { this.repo = repo; }

    /** Este metodo devuelve la cuenta. */
    @GetMapping
    public AccountResponse get() {
        var p = CurrentUser.require();
        Account a = repo.findById(p.accountId()).orElseThrow();
        return toDto(a);
    }

    /** Este metodo actualiza la cuenta. */
    @PatchMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @Transactional
    public AccountResponse update(@Valid @RequestBody UpdateAccountRequest req) {
        var p = CurrentUser.require();
        Account a = repo.findById(p.accountId()).orElseThrow();
        if (req.name() != null) a.setName(req.name());
        if (req.defaultLocale() != null) a.setDefaultLocale(req.defaultLocale());
        return toDto(a);
    }

    private AccountResponse toDto(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getSlug(),
            a.getStatus().name(), a.getPlan().name(), a.getDefaultLocale().name());
    }
}
