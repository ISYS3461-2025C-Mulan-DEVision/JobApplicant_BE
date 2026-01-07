package com.team.ja.user.dto.request;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    // Basic Fields
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String countryAbbreviation;
    private String address;
    private String city;
    private String objectiveSummary;
    private String avatarUrl;
    private boolean isPremium;

    // Nested Data (The entire profile)
    private List<UserSkillDto> skills;
    private List<UserEducationDto> education;
    private List<UserWorkExperienceDto> workExperience;
    private List<UserPortfolioItemDto> portfolioItems;
}
