package com.team.ja.user.repository.specification;

import com.team.ja.common.enumeration.EducationLevel;
import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<User> hasSkills(List<String> skills) {
        return (root, query, cb) -> {
            if (skills == null || skills.isEmpty()) {
                return cb.conjunction(); // always true
            }
            // Normalize input to lowercase
            List<String> lowered = skills
                .stream()
                .filter(StringUtils::hasText)
                .map(s -> s.toLowerCase().trim())
                .toList();

            // Join User -> UserSkill -> Skill
            Join<User, UserSkill> userSkillJoin = root.join("userSkills");
            Join<UserSkill, Skill> skillJoin = userSkillJoin.join("skill");

            // Compare lowercase skill names to lowercase inputs (case-insensitive)
            Expression<String> skillNameLower = cb.lower(skillJoin.get("name"));
            CriteriaBuilder.In<String> inClause = cb.in(skillNameLower);
            lowered.forEach(inClause::value);
            return inClause;
        };
    }

    public static Specification<User> hasCountry(UUID countryId) {
        return (root, query, criteriaBuilder) -> {
            if (countryId == null) {
                return criteriaBuilder.conjunction(); // always true
            }
            return criteriaBuilder.equal(root.get("countryId"), countryId);
        };
    }

    public static Specification<User> idIn(List<UUID> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                // Return a predicate that is always false when there are no IDs
                return cb.disjunction();
            }
            return root.get("id").in(ids);
        };
    }

    /**
     * Case-insensitive country match by name or abbreviation, using a subquery.
     * Useful when the caller only has a country string (e.g., from keyword) instead of a UUID.
     */
    public static Specification<User> hasCountryByText(String countryText) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(countryText)) {
                return cb.conjunction();
            }
            String lowered = countryText.trim().toLowerCase();

            // Subquery to fetch country IDs that match by name or abbreviation (case-insensitive)
            Subquery<UUID> countryIdSubquery = query.subquery(UUID.class);
            Root<Country> countryRoot = countryIdSubquery.from(Country.class);
            Expression<String> nameLower = cb.lower(countryRoot.get("name"));
            Expression<String> abbrLower = cb.lower(
                countryRoot.get("abbreviation")
            );

            countryIdSubquery
                .select(countryRoot.get("id"))
                .where(
                    cb.or(
                        cb.equal(nameLower, lowered),
                        cb.equal(abbrLower, lowered)
                    )
                );

            // Match user's countryId against the subquery results
            return root.get("countryId").in(countryIdSubquery);
        };
    }

    /**
     * Case-insensitive city match on User.city.
     */
    public static Specification<User> hasCity(String city) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(city)) {
                return cb.conjunction();
            }
            String lowered = city.trim().toLowerCase();
            return cb.like(cb.lower(root.get("city")), "%" + lowered + "%");
        };
    }

    /**
     * Case-insensitive work experience keyword match across jobTitle, description, and companyName.
     * Accepts CSV of keywords. Matches if any keyword is found in any work experience of the user.
     */
    public static Specification<User> hasWorkExperienceKeywords(
        List<String> keywords
    ) {
        return (root, query, cb) -> {
            if (keywords == null || keywords.isEmpty()) {
                return cb.conjunction();
            }
            // Build an EXISTS subquery on UserWorkExperience(userId = user.id AND keyword matches)
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<UserWorkExperience> uwe = sub.from(UserWorkExperience.class);

            Expression<String> titleLower = cb.lower(uwe.get("jobTitle"));
            Expression<String> descLower = cb.lower(uwe.get("description"));
            Expression<String> companyLower = cb.lower(uwe.get("companyName"));

            Predicate keywordOr = cb.disjunction();
            for (String kw : keywords) {
                if (!StringUtils.hasText(kw)) continue;
                String t = kw.trim().toLowerCase();
                String pattern = "%" + t + "%";
                keywordOr = cb.or(
                    keywordOr,
                    cb.like(titleLower, pattern),
                    cb.like(descLower, pattern),
                    cb.like(companyLower, pattern)
                );
            }

            sub
                .select(uwe.get("userId"))
                .where(
                    cb.and(
                        cb.equal(uwe.get("userId"), root.get("id")),
                        keywordOr
                    )
                );
            return cb.exists(sub);
        };
    }

    /**
     * Filter users that have at least one work experience with employmentType in given list.
     * Accepts multiple employment types.
     */
    public static Specification<User> hasEmploymentTypes(
        List<EmploymentType> types
    ) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) {
                return cb.conjunction();
            }
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<UserWorkExperience> uwe = sub.from(UserWorkExperience.class);
            CriteriaBuilder.In<EmploymentType> inClause = cb.in(
                uwe.get("employmentType")
            );
            types.forEach(inClause::value);

            sub
                .select(uwe.get("userId"))
                .where(
                    cb.and(
                        cb.equal(uwe.get("userId"), root.get("id")),
                        inClause
                    )
                );
            return cb.exists(sub);
        };
    }

    /**
     * Filter users that have at least one education with the specified level.
     */
    public static Specification<User> hasEducationLevel(EducationLevel level) {
        return (root, query, cb) -> {
            if (level == null) {
                return cb.conjunction();
            }
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<UserEducation> ue = sub.from(UserEducation.class);

            sub
                .select(ue.get("userId"))
                .where(
                    cb.and(
                        cb.equal(ue.get("userId"), root.get("id")),
                        cb.equal(ue.get("educationLevel"), level)
                    )
                );
            return cb.exists(sub);
        };
    }

    /**
     * Case-insensitive username match across firstName and lastName.
     * Uses SQL LIKE with lowercased values to perform contains matches.
     */
    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(username)) {
                return cb.conjunction();
            }
            // Split CSV terms, trim and lowercase each
            String[] terms = username.split(",");
            Predicate combined = cb.disjunction();

            Expression<String> firstNameLower = cb.lower(root.get("firstName"));
            Expression<String> lastNameLower = cb.lower(root.get("lastName"));
            // Build a lowercased fullName expression: lower(concat(concat(firstName, ' '), lastName))
            Expression<String> fullNameLower = cb.lower(
                cb.concat(
                    cb.concat(root.get("firstName"), cb.literal(" ")),
                    root.get("lastName")
                )
            );

            for (String term : terms) {
                if (!StringUtils.hasText(term)) continue;
                String t = term.trim().toLowerCase();
                String pattern = "%" + t + "%";
                combined = cb.or(
                    combined,
                    cb.like(firstNameLower, pattern),
                    cb.like(lastNameLower, pattern),
                    cb.like(fullNameLower, pattern)
                );
            }
            return combined;
        };
    }
}
