package com.team.ja.user.repository.specification;

import com.team.ja.user.model.Country;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserSkill;
import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.UUID; // <--- Add this import
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
}
