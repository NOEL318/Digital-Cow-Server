package com.digitalcow.admin.dto;

import com.digitalcow.account.AccountPlan;
import com.digitalcow.account.AccountStatus;

public record UpdateAdminAccountRequest(AccountStatus status, AccountPlan plan) {}
