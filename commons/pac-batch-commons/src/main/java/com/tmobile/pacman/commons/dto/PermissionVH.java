package com.tmobile.pacman.commons.dto;

public class PermissionVH {
    ErrorVH errorVH;
    String accountNumber;

    public ErrorVH getErrorVH() {
        return errorVH;
    }

    public void setErrorVH(ErrorVH errorVH) {
        this.errorVH = errorVH;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
