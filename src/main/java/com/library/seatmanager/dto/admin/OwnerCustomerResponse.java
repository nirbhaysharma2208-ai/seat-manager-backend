
package com.library.seatmanager.dto.admin;

import com.library.seatmanager.dto.AccountStatus;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Library;

import java.time.LocalDateTime;
import java.util.List;

public record OwnerCustomerResponse(
        Long id,
        String name,
        String phone,
        AccountStatus status,
        LocalDateTime createdAt,
        int libraryCount,
        int totalSeats,
        List<OwnerLibraryResponse> libraries
) {
    public static OwnerCustomerResponse from(Admin admin) {
        List<OwnerLibraryResponse> libraries =
                admin.getLibraries() == null
                        ? List.of()
                        : admin.getLibraries().stream()
                        .map(OwnerLibraryResponse::from)
                        .toList();

        int totalSeats = libraries.stream()
                .mapToInt(OwnerLibraryResponse::totalSeats)
                .sum();

        return new OwnerCustomerResponse(
                admin.getId(),
                admin.getName(),
                admin.getPhone(),
                admin.getStatus(),
                admin.getCreatedAt(),
                libraries.size(),
                totalSeats,
                libraries
        );
    }
}

record OwnerLibraryResponse(
        Long id,
        String libraryName,
        int totalSeats,
        String logoUrl
) {
    static OwnerLibraryResponse from(Library library) {
        return new OwnerLibraryResponse(
                library.getId(),
                library.getLibraryName(),
                library.getTotalSeats(),
                library.getLogoUrl()
        );
    }
}
