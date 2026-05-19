package com.digitalcow.admin;

import com.digitalcow.account.*;
import com.digitalcow.admin.dto.*;
import com.digitalcow.auth.AuthService;
import com.digitalcow.auth.dto.AuthTokensResponse;
import com.digitalcow.auth.dto.LoginRequest;
import com.digitalcow.tenancy.SkipTenancy;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints super-admin (rol global SUPERADMIN). */
@RestController
@RequestMapping("/api/v1/admin")
@SkipTenancy
public class AdminController {

    private final AccountRepository accounts;
    private final AuthService auth;

    public AdminController(AccountRepository accounts, AuthService auth) {
        this.accounts = accounts;
        this.auth = auth;
    }

    /** Este metodo inicia sesion como super administrador. */
    @PostMapping("/login")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** Este metodo lista todas las cuentas del sistema. */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<AdminAccountDto> list() {
        return accounts.findAll().stream()
            .map(a -> new AdminAccountDto(a.getId(), a.getName(), a.getSlug(),
                a.getStatus().name(), a.getPlan().name()))
            .toList();
    }

    /** Este metodo actualiza el estado o plan de una cuenta del sistema. */
    @PatchMapping("/accounts/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Transactional
    public AdminAccountDto update(@PathVariable Long id, @RequestBody UpdateAdminAccountRequest req) {
        Account a = accounts.findById(id).orElseThrow();
        if (req.status() != null) a.setStatus(req.status());
        if (req.plan() != null) a.setPlan(req.plan());
        return new AdminAccountDto(a.getId(), a.getName(), a.getSlug(),
            a.getStatus().name(), a.getPlan().name());
    }
}
