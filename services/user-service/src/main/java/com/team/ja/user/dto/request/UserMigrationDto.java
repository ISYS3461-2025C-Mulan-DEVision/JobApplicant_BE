package com.team.ja.user.dto.request;

import java.util.List;
import java.util.Set;

import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserPortfolioItem;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSearchProfileEmploymentStatus;
import com.team.ja.user.model.UserSearchProfileJobTitle;
import com.team.ja.user.model.UserSearchProfileSkill;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMigrationDto {

    private User user;
    private List<UserEducation> education;
    private List<UserWorkExperience> workExperience;
    private List<UserSkill> skills;
    private List<UserPortfolioItem> portfolioItems;
    private UserSearchProfile searchProfile;
    private List<UserSearchProfileSkill> searchProfileSkills;
    private List<UserSearchProfileJobTitle> searchProfileJobTitles;
    private List<UserSearchProfileEmploymentStatus> searchProfileEmployments;

}
