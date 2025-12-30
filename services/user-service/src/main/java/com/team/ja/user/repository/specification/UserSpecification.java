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
        return (root, query, criteriaBuilder) -> {
            if (skills == null || skills.isEmpty()) {
                return criteriaBuilder.conjunction(); // always true
            }
            // Join User -> UserSkill -> Skill
            Join<User, UserSkill> userSkillJoin = root.join("userSkills");
            Join<UserSkill, Skill> skillJoin = userSkillJoin.join("skill");
            // Check if skill name is in the provided list
            return skillJoin.get("name").in(skills);
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
}
