package com.kei.backend.account;

import com.kei.backend.account.AccountService.DataExport;
import com.kei.backend.security.CurrentUserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Tai toan bo du lieu cua minh dang file JSON. */
    @GetMapping("/export")
    public ResponseEntity<DataExport> export(@CurrentUserId Long userId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("du-lieu-cua-toi.json").build().toString())
                .body(accountService.export(userId));
    }

    /** Xoa tai khoan. Dung POST (khong phai DELETE) vi can gui kem mat khau trong body. */
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUserId Long userId, @Valid @RequestBody DeleteRequest request) {
        accountService.delete(userId, request.password());
    }

    public record DeleteRequest(@NotBlank(message = "Nhập mật khẩu để xác nhận") String password) {
    }
}
