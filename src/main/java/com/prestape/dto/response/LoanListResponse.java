package com.prestape.dto.response;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.prestape.entity.Loan;
import com.prestape.entity.LoanItem;
import com.prestape.entity.Person;
import com.prestape.entity.User;
import com.prestape.entity.enums.EquipmentCondition;

public record LoanListResponse(
        Long id,
        String code,
        RequesterInfo requester,
        List<ItemInfo> items,
        Instant scheduledPickupAt,
        Instant pickedUpAt,
        Instant scheduledReturnAt,
        Instant returnedAt,
        String deliveredBy,
        String receivedBy,
        String notes,
        String status,
        Instant createdAt
) {
    public record RequesterInfo(String fullName, String code) {
    }

    public record ItemInfo(
            Long id,
            String code,
            String name,
            EquipmentCondition currentCondition,
            EquipmentCondition pickupCondition,
            EquipmentCondition returnCondition) {
        static ItemInfo from(LoanItem item) {
            return new ItemInfo(
                    item.getEquipment().getId(),
                    item.getEquipment().getCode(),
                    item.getEquipment().getName(),
                    item.getEquipment().getCondition(),
                    item.getPickupCondition(),
                    item.getReturnCondition());
        }
    }

    public static LoanListResponse from(Loan loan, Map<Long, Person> peopleByUserId, Duration tolerance) {
        Person requester = peopleByUserId.get(loan.getRequester().getId());

        return new LoanListResponse(
                loan.getId(),
                loan.getCode(),
                new RequesterInfo(
                        requester != null ? requester.getFullName() : loan.getRequester().getEmail(),
                        requester != null ? requester.getCode() : null),
                loan.getItems().stream().map(ItemInfo::from).toList(),
                loan.getScheduledPickupAt(),
                loan.getPickedUpAt(),
                loan.getScheduledReturnAt(),
                loan.getReturnedAt(),
                shortName(loan.getDeliveredBy(), peopleByUserId),
                shortName(loan.getReceivedBy(), peopleByUserId),
                loan.getNotes(),
                displayStatus(loan, tolerance),
                loan.getCreatedAt());
    }

    private static String shortName(User user, Map<Long, Person> peopleByUserId) {
        if (user == null) return null;
        Person person = peopleByUserId.get(user.getId());
        return person != null ? person.getShortName() : user.getEmail();
    }

    private static String displayStatus(Loan loan, Duration tolerance) {
        if (loan.isOverdue(tolerance)) return "OVERDUE";
        if (loan.isReturnedLate()) return "RETURNED_LATE";
        return loan.getStatus().name();
    }
}
