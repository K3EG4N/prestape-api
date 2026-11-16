package com.prestape.dto.response;

import java.util.List;

import com.prestape.dto.loan.LoanLimit;
import com.prestape.dto.loan.LoanRules;

public record LoanRulesResponse(
        long toleranceMinutes,
        long maxDurationDays,
        long minAdvanceHours,
        long autoCancelHours,
        Integer maxEquipments,
        Integer maxActiveLoans,
        List<SettingsResponse.BusinessDayInfo> businessHours
) {
    public static LoanRulesResponse from(LoanRules rules, LoanLimit limit) {
        return new LoanRulesResponse(
                rules.tolerance().toMinutes(),
                rules.maxDuration().toDays(),
                rules.minAdvance().toHours(),
                rules.autoCancel().toHours(),
                limit != null ? limit.maxEquipments() : null,
                limit != null ? limit.maxActiveLoans() : null,
                rules.businessHours().stream().map(SettingsResponse.BusinessDayInfo::from).toList());
    }
}
