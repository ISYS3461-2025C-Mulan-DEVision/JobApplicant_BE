package com.team.ja.subscription.dto.response;

import java.util.UUID;
import com.team.ja.common.enumeration.EmploymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchProfileEmployment {
    private UUID id;
    private UUID userId;
    private EmploymentType employmentType;
}
