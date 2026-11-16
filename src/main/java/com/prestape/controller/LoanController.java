package com.prestape.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.common.SelectOption;
import com.prestape.dto.request.loan.create.CreateLoanRequest;
import com.prestape.dto.request.loan.list.LoanFilterRequest;
import com.prestape.dto.request.loan.pickup.RegisterPickupRequest;
import com.prestape.dto.request.loan.returns.RegisterReturnRequest;
import com.prestape.dto.response.LoanEquipmentOption;
import com.prestape.dto.response.LoanKitOption;
import com.prestape.dto.response.LoanListResponse;
import com.prestape.report.LoanExcelReport;
import com.prestape.dto.response.LoanRulesResponse;
import com.prestape.service.LoanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanExcelReport loanExcelReport;

    public LoanController(LoanService loanService, LoanExcelReport loanExcelReport) {
        this.loanService = loanService;
        this.loanExcelReport = loanExcelReport;
    }

    @GetMapping
    public ApiResponse<PageResponse<LoanListResponse>> list(
            @Valid @ModelAttribute PaginationRequest pagination,
            @Valid @ModelAttribute LoanFilterRequest filter,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.ok(loanService.list(pagination, filter, currentUser));
    }


    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String search, @Valid @ModelAttribute LoanFilterRequest filter) {
        return loanExcelReport.generate(loanService.listForExport(search, filter)).toResponse();
    }
      
    @GetMapping("/rules")
    public ApiResponse<LoanRulesResponse> rules(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ApiResponse.ok(loanService.rules(currentUser));
    }

    @GetMapping("/options/requesters")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<List<SelectOption>> requesterOptions() {
        return ApiResponse.ok(loanService.requesterOptions());
    }

    @GetMapping("/options/equipments")
    public ApiResponse<List<LoanEquipmentOption>> equipmentOptions(@RequestParam(required = false) String search) {
        return ApiResponse.ok(loanService.equipmentOptions(search));
    }

    @GetMapping("/options/kits")
    public ApiResponse<List<LoanKitOption>> kitOptions() {
        return ApiResponse.ok(loanService.kitOptions());
    }

    @PostMapping
    public ApiResponse<Void> create(
            @Valid @RequestBody CreateLoanRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        loanService.create(request, currentUser);
        return ApiResponse.success("Préstamo registrado correctamente");
    }

    @PutMapping("/{id}/pickup")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> registerPickup(
            @PathVariable Long id,
            @Valid @RequestBody RegisterPickupRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        loanService.registerPickup(id, request, currentUser);
        return ApiResponse.success("Recojo registrado correctamente");
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> registerReturn(
            @PathVariable Long id,
            @Valid @RequestBody RegisterReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        loanService.registerReturn(id, request, currentUser);
        return ApiResponse.success("Devolución registrada correctamente");
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        loanService.cancel(id, currentUser);
        return ApiResponse.success("Préstamo cancelado correctamente");
    }
}
