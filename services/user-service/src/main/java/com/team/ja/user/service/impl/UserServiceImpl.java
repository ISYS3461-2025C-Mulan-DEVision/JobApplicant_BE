package com.team.ja.user.service.impl;

import com.team.ja.common.dto.PageResponse;
import com.team.ja.common.enumeration.EducationLevel;
import com.team.ja.common.enumeration.EmploymentType;
import com.team.ja.common.event.KafkaTopics;
import com.team.ja.common.event.UserMigrationEvent;
import com.team.ja.common.event.UserProfileUpdatedEvent;
import com.team.ja.common.event.UserSearchProfileUpdateEvent;
import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.S3FileService;
import com.team.ja.user.config.sharding.ShardContext;
import com.team.ja.user.config.sharding.ShardingProperties;
import com.team.ja.user.dto.request.ChangePasswordRequest;
import com.team.ja.user.dto.request.CreateUserRequest;
import com.team.ja.user.dto.request.UpdateUserRequest;
import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.dto.response.UserProfileResponse;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.dto.response.UserWorkExperienceResponse;
import com.team.ja.user.kafka.UserProfileUpdatedProducer;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.mapper.SkillMapper;
import com.team.ja.user.mapper.UserEducationMapper;
import com.team.ja.user.mapper.UserMapper;
import com.team.ja.user.mapper.UserWorkExperienceMapper;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSearchProfile;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.model.Country;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSearchProfileEmploymentRepository;
import com.team.ja.user.repository.UserSearchProfileJobTitleRepository;
import com.team.ja.user.repository.UserSearchProfileRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.repository.UserWorkExperienceRepository;
import com.team.ja.user.repository.specification.UserSpecification;
import com.team.ja.user.service.AuthServiceClient;
import com.team.ja.user.service.UserService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of UserService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEducationRepository userEducationRepository;
    private final UserWorkExperienceRepository userWorkExperienceRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final CountryRepository countryRepository;
    private final S3FileService s3FileService;
    private final UserProfileUpdatedProducer profileUpdatedProducer;

    private final UserMapper userMapper;
    private final UserEducationMapper userEducationMapper;
    private final UserWorkExperienceMapper userWorkExperienceMapper;
    private final SkillMapper skillMapper;
    private final CountryMapper countryMapper;
    private final ShardingProperties shardingProperties;
    private final ShardLookupService shardLookupService;
    private final AuthServiceClient authServiceClient;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    private final KafkaTemplate<String, UserMigrationEvent> userMigrationEventKafkaTemplate;
    private final KafkaTemplate<String, UserSearchProfileUpdateEvent> userSearchProfileUpdateKafkaTemplate;

    // For getting Kafka for JM
    private final UserSearchProfileEmploymentRepository userSearchProfileEmploymentRepository;
    private final UserSearchProfileRepository userSearchProfileRepository;
    private final UserSearchProfileJobTitleRepository userSearchProfileJobTitleRepository;

    private static final int AVATAR_SIZE = 256;
    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif");

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    "User with email " + request.getEmail() + " already exists");
        }

        if (request.getCountryId() != null) {
            countryRepository
                    .findById(request.getCountryId())
                    .orElseThrow(() -> new NotFoundException(
                            "Country",
                            "id",
                            request.getCountryId().toString()));
        }

        String country = request.getCountryId().toString();
        String shardkey = ShardingProperties.resolveShard(country);
        ShardContext.setShardKey(shardkey);

        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .countryId(request.getCountryId())
                    .objectiveSummary(request.getObjectiveSummary())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Created user with ID: {}", savedUser.getId());

            shardLookupService.cachedUserIdShard(user.getId(), shardkey);

            return mapUserWithCountry(savedUser);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                    .findById(userId)
                    .filter(User::isActive)
                    .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

            UUID oldCountryId = user.getCountryId();

            // Update fields if provided
            if (request.getFirstName() != null)
                user.setFirstName(
                        request.getFirstName());
            if (request.getLastName() != null)
                user.setLastName(
                        request.getLastName());
            if (request.getPhone() != null)
                user.setPhone(request.getPhone());
            if (request.getCountryAbbreviation() != null) {
                Country country = countryRepository
                        .findByAbbreviationIgnoreCaseAndIsActiveTrue(request.getCountryAbbreviation())
                        .orElseThrow(() -> new NotFoundException(
                                "Country",
                                "abbreviation",
                                request.getCountryAbbreviation()));

                // user.setCountryId(request.getCountryId());
                // Perform moving to new shard in background after commit
                String targetShard = ShardingProperties.resolveShard(country.getAbbreviation());
                if (targetShard.equals(ShardContext.getShardKey())) {
                    log.info("User {} country updated to same shard {}, no migration needed.", userId, targetShard);
                } else {
                    log.info("User {} country updated, scheduling migration from shard {} to {}.", userId,
                            ShardContext.getShardKey(), targetShard);

                    UserMigrationEvent migrationEvent = UserMigrationEvent.builder()
                            .userId(userId)
                            .sourceShardId(shardKey)
                            .targetShardId(targetShard)
                            .newCountryAbbreviation(request.getCountryAbbreviation())
                            .build();

                    userMigrationEventKafkaTemplate.send(KafkaTopics.USER_MIGRATION, migrationEvent)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.info("Sent UserMigrationEvent for user {} [partition: {}, offset: {}]", 
                                            userId,
                                            result.getRecordMetadata().partition(),
                                            result.getRecordMetadata().offset());
                                } else {
                                    log.error("Failed to send UserMigrationEvent for user {}", userId, ex);
                                }
                            });

                    log.info("Published user migration event for user {} to shard {}.", userId, targetShard);

                    String countryAbbreviation = null;
                    if (user != null && user.getCountryId() != null) {
                        countryAbbreviation = countryRepository.findById(user.getCountryId())
                                .map(c -> c.getAbbreviation())
                                .orElse(null);
                    }
                    List<UserEducation> educationLevel = userEducationRepository
                            .findByUserIdOrderByEducationLevelRankDesc(userId);

                    Optional<UserSearchProfile> userSearchProfile = userSearchProfileRepository.findByUserId(userId);

                    List<EmploymentType> employmentTypes = userSearchProfileEmploymentRepository
                            .findByUserSearchProfileIdAndIsActiveTrue(
                                    userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                            .stream()
                            .map(ute -> ute.getEmploymentType())
                            .collect(Collectors.toList());

                    List<String> jobTitles = userSearchProfileJobTitleRepository
                            .findByUserSearchProfileIdAndIsActiveTrue(
                                    userSearchProfile.isPresent() ? userSearchProfile.get().getId() : null)
                            .stream()
                            .map(utj -> utj.getJobTitle())
                            .collect(Collectors.toList());

                    List<UserSkill> allUserSkillIds = userSkillRepository.findByUserIdAndIsActiveTrue(userId);

                    UserSearchProfileUpdateEvent searchProfileEvent = UserSearchProfileUpdateEvent.builder()
                            .userId(userId)
                            .countryAbbreviation(countryAbbreviation)
                            .educationLevel(educationLevel.isEmpty() ? null
                                    : educationLevel.get(0).getEducationLevel().name())
                            .employmentTypes(employmentTypes.stream()
                                    .map(EmploymentType::name)
                                    .collect(Collectors.toList()))
                            .minSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMin() : null)
                            .maxSalary(userSearchProfile.isPresent() ? userSearchProfile.get().getSalaryMax() : null)
                            .jobTitles(jobTitles)
                            .skillIds(allUserSkillIds.stream()
                                    .map(UserSkill::getSkillId)
                                    .collect(Collectors.toList()))
                            .build();
                    userSearchProfileUpdateKafkaTemplate.send(KafkaTopics.USER_PROFILE_UPDATE, searchProfileEvent)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.info("Sent UserSearchProfileUpdateEvent for user {} [partition: {}, offset: {}]", 
                                            userId,
                                            result.getRecordMetadata().partition(),
                                            result.getRecordMetadata().offset());
                                } else {
                                    log.error("Failed to send UserSearchProfileUpdateEvent for user {}", userId, ex);
                                }
                            });
                }

                Country newCountry = countryRepository
                        .findByAbbreviationIgnoreCaseAndIsActiveTrue(request.getCountryAbbreviation())
                        .orElseThrow(() -> new NotFoundException(
                                "Country",
                                "abbreviation",
                                request.getCountryAbbreviation()));

                user.setCountryId(newCountry.getId());
            }
            if (request.getAddress() != null)
                user.setAddress(request.getAddress());
            if (request.getCity() != null)
                user.setCity(request.getCity());
            if (request.getObjectiveSummary() != null)
                user.setObjectiveSummary(
                        request.getObjectiveSummary());

            user.markProfileUpdated();
            User savedUser = userRepository.save(user);

            // Check if country has changed and publish event
            if (!Objects.equals(oldCountryId, savedUser.getCountryId())) {
                log.info(
                        "User {} country changed from {} to {}. Publishing event.",
                        userId,
                        oldCountryId,
                        savedUser.getCountryId());
                UserProfileUpdatedEvent event = UserProfileUpdatedEvent.builder()
                        .userId(userId)
                        .updateType(UserProfileUpdatedEvent.UpdateType.COUNTRY)
                        .countryId(savedUser.getCountryId())
                        .build();
                profileUpdatedProducer.sendProfileUpdatedEvent(event);
            }

            log.info("Updated user with ID: {}", savedUser.getId());
            return mapUserWithCountry(savedUser);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file) {
        log.info("Uploading avatar for user {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        User user = userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        if (file.isEmpty())
            throw new BadRequestException(
                    "File cannot be empty.");
        if (!SUPPORTED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException(
                    "Unsupported image type. Please upload a JPEG, PNG, or GIF.");
        }

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = Scalr.resize(
                    originalImage,
                    Scalr.Method.QUALITY,
                    Scalr.Mode.AUTOMATIC,
                    AVATAR_SIZE,
                    AVATAR_SIZE,
                    Scalr.OP_ANTIALIAS);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            String format = file.getContentType().split("/")[1];
            ImageIO.write(resizedImage, format, os);
            byte[] imageBytes = os.toByteArray();

            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                s3FileService.deleteFile(user.getAvatarUrl());
            }

            String avatarUrl = s3FileService.uploadFile(
                    imageBytes,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    "avatars");

            user.setAvatarUrl(avatarUrl);
            user.markProfileUpdated();
            User savedUser = userRepository.save(user);

            log.info(
                    "Successfully uploaded avatar for user {}. URL: {}",
                    userId,
                    avatarUrl);
            return mapUserWithCountry(savedUser);
        } catch (IOException e) {
            log.error("Failed to process image for user {}", userId, e);
            throw new BadRequestException("Could not process image file.");
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user by ID: {}", id);

        String shardKey = shardLookupService.findShardIdByUserId(id);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                    .findById(id)
                    .filter(User::isActive)
                    .orElseThrow(() -> new NotFoundException("User", "id", id.toString()));
            return mapUserWithCountry(user);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        String shardKey = shardLookupService.findShardByUserEmail(email);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                    .findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> new NotFoundException("User", "email", email));
            return mapUserWithCountry(user);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public UserProfileResponse getUserProfile(UUID userId) {
        log.info("Fetching complete profile for user: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                    .findById(userId)
                    .filter(User::isActive)
                    .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

            List<UserEducation> education = userEducationRepository.findByUserIdAndIsActiveTrueOrderByStartAtDesc(
                    userId);
            List<UserWorkExperience> workExperience = userWorkExperienceRepository
                    .findByUserIdAndIsActiveTrueOrderByStartAtDesc(
                            userId);
            List<UserSkill> userSkills = userSkillRepository.findByUserIdAndIsActiveTrue(userId);
            List<UUID> skillIds = userSkills
                    .stream()
                    .map(UserSkill::getSkillId)
                    .toList();
            List<Skill> skills = skillIds.isEmpty()
                    ? List.of()
                    : skillRepository.findByIdInAndIsActiveTrue(skillIds);

            List<UserEducationResponse> educationResponses = education
                    .stream()
                    .map(userEducationMapper::toResponse)
                    .toList();
            List<UserWorkExperienceResponse> workExpResponses = workExperience
                    .stream()
                    .map(this::mapWorkExperienceWithCountry)
                    .toList();
            List<SkillResponse> skillResponses = skillMapper.toResponseList(skills);

            return UserProfileResponse.builder()
                    .user(mapUserWithCountry(user))
                    .education(educationResponses)
                    .workExperience(workExpResponses)
                    .skills(skillResponses)
                    .build();
        } finally {
            ShardContext.clear();
        }
    }

    /**
     * Get all active users. From all shards.
     */
    @Override
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all active users");
        List<UserResponse> allUsers = new ArrayList<>();
        for (String shardKey : shardingProperties.getShards().keySet()) {
            ShardContext.setShardKey(shardKey);
            try {
                userRepository
                        .findAll()
                        .stream()
                        .filter(User::isActive)
                        .map(this::mapUserWithCountry)
                        .forEach(allUsers::add);
            } finally {
                ShardContext.clear();
            }
        }
        return allUsers;
    }

    @Override
    public PageResponse<UserResponse> getAllUsersPaged(int page, int size) {
        log.info("Fetching users (paged), page [{}], size [{}]", page, size);

        long globalTotal = 0;
        List<UserResponse> pagedResponses = new ArrayList<>();
        long reqStart = (long) page * size;
        long reqEnd = reqStart + size;

        // Get shards in a deterministic order
        List<String> shardKeys = new ArrayList<>(shardingProperties.getShards().keySet());
        Collections.sort(shardKeys);

        for (String shardKey : shardKeys) {
            ShardContext.setShardKey(shardKey);
            try {
                long shardCount = userRepository.count();
                
                long shardStart = globalTotal;
                long shardEnd = globalTotal + shardCount;

                // Check for overlap: [shardStart, shardEnd) vs [reqStart, reqEnd)
                if (Math.max(shardStart, reqStart) < Math.min(shardEnd, reqEnd)) {
                    long localOffset = Math.max(0, reqStart - shardStart);
                    long itemsToTake = Math.min(shardEnd, reqEnd) - Math.max(shardStart, reqStart);
                    
                    // Fetch logic: standard JPA pages are page-aligned (0, 20), (1, 20).
                    // We need offset-based. We will fetch page(s) covering our range and trim.
                    int fetchPage = (int) (localOffset / size);
                    // Fetch slightly more to ensure coverage if localOffset isn't aligned
                    int fetchSize = size + (int)(localOffset % size); 
                    
                    // Actually, simpler: just use PageRequest with offset if we could, 
                    // but since we can't easily, we'll request a page that definitely starts before our target.
                    // Or, since we're refactoring for performance, we accept that 'page' here is relative to 
                    // the shard's data stream.
                    
                    // Robust approach:
                    // Calculate absolute index in shard: localOffset
                    // We need 'itemsToTake' rows starting at 'localOffset'.
                    
                    // To do this via standard JPA findAll(Pageable):
                    // Page number = localOffset / size
                    // We might need data from Page N and Page N+1 if the slice crosses a boundary.
                    
                    int startPage = (int) (localOffset / size);
                    int endPage = (int) ((localOffset + itemsToTake - 1) / size);
                    
                    List<User> shardUsers = new ArrayList<>();
                    for (int p = startPage; p <= endPage; p++) {
                        shardUsers.addAll(userRepository.findAll(PageRequest.of(p, size)).getContent());
                    }
                    
                    // Trim the results
                    // The first element of shardUsers is at index: startPage * size
                    long listStartIndex = (long) startPage * size;
                    int subListStart = (int) (localOffset - listStartIndex);
                    int subListEnd = Math.min(shardUsers.size(), subListStart + (int) itemsToTake);
                    
                    if (subListStart < shardUsers.size() && subListStart < subListEnd) {
                        List<User> relevantUsers = shardUsers.subList(subListStart, subListEnd);
                        pagedResponses.addAll(relevantUsers.stream()
                                .filter(User::isActive)
                                .map(this::mapUserWithCountry)
                                .toList());
                    }
                }
                
                globalTotal += shardCount;
            } catch (Exception e) {
                log.error("Error processing shard {}", shardKey, e);
            } finally {
                ShardContext.clear();
            }
        }

        Page<UserResponse> pageImpl = new PageImpl<>(pagedResponses, PageRequest.of(page, size), globalTotal);
        return PageResponse.of(pagedResponses, pageImpl);
    }

    /**
     * Non-paginated search. Kept for backward compatibility.
     * Now enforces isActive and combines FTS with filters by intersecting IDs.
     */
    @Override
    public List<UserResponse> searchUsers(
            String skills,
            String country,
            String city,
            String education,
            String workExperience,
            String employmentTypes,
            String username) {
        log.info(
                "Searching for users with skills [{}], country [{}], city [{}], education [{}], workExperience [{}], employmentTypes [{}], and username [{}]",
                skills,
                country,
                city,
                education,
                workExperience,
                employmentTypes,
                username);

        try {
            if (country != null && !country.isBlank()) {
                country = country.trim();
                String shardKey = ShardingProperties.resolveShard(country);
                ShardContext.setShardKey(shardKey);
            } else {
                ShardContext.setShardKey("user_shard_vn");
            }

            List<String> skillList = (skills != null && !skills.isEmpty())
                    ? Arrays.stream(skills.split(","))
                            .map(s -> s.toLowerCase().trim())
                            .filter(s -> !s.isEmpty())
                            .toList()
                    : Collections.emptyList();

            // Parse city (prioritize city over country when both provided)
            String cityFilter = (city != null && !city.isBlank())
                    ? city.trim()
                    : null;

            // Parse country (only used when city not provided)
            UUID countryFilterId = null;
            if ((cityFilter == null) && country != null && !country.isBlank()) {
                countryFilterId = countryRepository
                        .findByAbbreviationIgnoreCase(country.trim())
                        .map(Country::getId)
                        .orElse(null);
            }

            // Parse education level
            EducationLevel educationLevel = null;
            if (education != null && !education.isBlank()) {
                try {
                    educationLevel = EducationLevel.valueOf(
                            education.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    educationLevel = null;
                }
            }

            // Parse work experience keywords (CSV)
            List<String> workExpKeywords = (workExperience != null &&
                    !workExperience.isBlank())
                            ? Arrays.stream(workExperience.split(","))
                                    .map(s -> s.toLowerCase().trim())
                                    .filter(s -> !s.isEmpty())
                                    .toList()
                            : Collections.emptyList();

            // Parse employment types (CSV)
            List<EmploymentType> empTypes = (employmentTypes != null
                    && !employmentTypes.isBlank())
                            ? Arrays.stream(employmentTypes.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .map(String::toUpperCase)
                                    .map(typeStr -> {
                                        try {
                                            return EmploymentType.valueOf(
                                                    typeStr);
                                        } catch (IllegalArgumentException ex) {
                                            return null;
                                        }
                                    })
                                    .filter(java.util.Objects::nonNull)
                                    .toList()
                            : java.util.Collections.emptyList();

            Specification<User> spec = Specification.where(
                    UserSpecification.isActive())
                    .and(UserSpecification.hasSkills(skillList))
                    // Location: prioritize city over country
                    .and(UserSpecification.hasCity(cityFilter))
                    .and(UserSpecification.hasCountry(countryFilterId))
                    // Education level
                    .and(UserSpecification.hasEducationLevel(educationLevel))
                    // Work experience keywords
                    .and(UserSpecification.hasWorkExperienceKeywords(workExpKeywords))
                    // Employment types
                    .and(UserSpecification.hasEmploymentTypes(empTypes));

            if (username != null && !username.isEmpty()) {
                String un = username.trim();

                // Case-insensitive LIKE across username fields (firstName, lastName)
                spec = spec.and(UserSpecification.hasUsername(un));

                // Apply FTS when available (AND only when there are matches)
                List<User> ftsCandidates = userRepository.findByFts(un);
                if (!ftsCandidates.isEmpty()) {
                    List<UUID> ftsIds = ftsCandidates
                            .stream()
                            .map(User::getId)
                            .toList();
                    spec = spec.and(UserSpecification.idIn(ftsIds));
                }

                // Do not apply country filtering from username text here
            }

            return userRepository
                    .findAll(spec)
                    .stream()
                    .map(this::mapUserWithCountry)
                    .toList();
        } finally {
            ShardContext.clear();
        }
    }

    /**
     * Paginated search combining FTS with filters and enforcing isActive.
     */
    public PageResponse<UserResponse> searchUsersPaged(
            String skills,
            String country,
            String city,
            String education,
            String workExperience,
            String employmentTypes,
            String username,
            int page,
            int size) {
        log.info(
                "Searching for users (paged) with skills [{}], country [{}], city [{}], education [{}], workExperience [{}], employmentTypes [{}], username [{}], page [{}], size [{}]",
                skills,
                country,
                city,
                education,
                workExperience,
                employmentTypes,
                username,
                page,
                size);

        if (country != null && !country.isBlank()) {
            country = country.trim();
            String shardKey = ShardingProperties.resolveShard(country);
            ShardContext.setShardKey(shardKey);
        } else {
            ShardContext.setShardKey("user_shard_vn");
        }

        try {
            List<String> skillList = (skills != null && !skills.isEmpty())
                    ? Arrays.stream(skills.split(","))
                            .map(s -> s.toLowerCase().trim())
                            .filter(s -> !s.isEmpty())
                            .toList()
                    : Collections.emptyList();

            // Parse city (prioritize city over country when both provided)
            String cityFilter = (city != null && !city.isBlank())
                    ? city.trim()
                    : null;

            // Parse country (only used when city not provided)
            UUID countryFilterId = null;
            if ((cityFilter == null) && country != null && !country.isBlank()) {
                countryFilterId = countryRepository
                        .findByAbbreviationIgnoreCase(country.trim())
                        .map(Country::getId)
                        .orElse(null);
            }

            // Parse education level
            com.team.ja.common.enumeration.EducationLevel educationLevel = null;
            if (education != null && !education.isBlank()) {
                try {
                    educationLevel = com.team.ja.common.enumeration.EducationLevel.valueOf(
                            education.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    educationLevel = null;
                }
            }

            // Parse work experience keywords (CSV)
            List<String> workExpKeywords = (workExperience != null &&
                    !workExperience.isBlank())
                            ? Arrays.stream(workExperience.split(","))
                                    .map(s -> s.toLowerCase().trim())
                                    .filter(s -> !s.isEmpty())
                                    .toList()
                            : Collections.emptyList();

            // Parse employment types (CSV)
            List<com.team.ja.common.enumeration.EmploymentType> empTypes = (employmentTypes != null
                    && !employmentTypes.isBlank())
                            ? Arrays.stream(employmentTypes.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .map(String::toUpperCase)
                                    .map(typeStr -> {
                                        try {
                                            return com.team.ja.common.enumeration.EmploymentType.valueOf(
                                                    typeStr);
                                        } catch (IllegalArgumentException ex) {
                                            return null;
                                        }
                                    })
                                    .filter(java.util.Objects::nonNull)
                                    .toList()
                            : java.util.Collections.emptyList();

            Specification<User> spec = Specification.where(
                    UserSpecification.isActive())
                    .and(UserSpecification.hasSkills(skillList))
                    // Location: prioritize city over country
                    .and(UserSpecification.hasCity(cityFilter))
                    .and(UserSpecification.hasCountry(countryFilterId))
                    // Education level
                    .and(UserSpecification.hasEducationLevel(educationLevel))
                    // Work experience keywords
                    .and(UserSpecification.hasWorkExperienceKeywords(workExpKeywords))
                    // Employment types
                    .and(UserSpecification.hasEmploymentTypes(empTypes));

            if (username != null && !username.isEmpty()) {
                String un = username.trim();

                // Case-insensitive LIKE across username fields (firstName, lastName)
                spec = spec.and(UserSpecification.hasUsername(un));

                // Apply FTS when available (AND only when there are matches)
                List<User> ftsCandidates = userRepository.findByFts(un);
                if (!ftsCandidates.isEmpty()) {
                    List<UUID> ftsIds = ftsCandidates
                            .stream()
                            .map(User::getId)
                            .toList();
                    spec = spec.and(UserSpecification.idIn(ftsIds));
                }

                // Do not apply country filtering from username text here
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<User> result = userRepository.findAll(spec, pageable);

            List<UserResponse> content = result
                    .getContent()
                    .stream()
                    .map(this::mapUserWithCountry)
                    .toList();

            return PageResponse.of(content, result);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    public void deactivateUser(UUID userId) {
        log.info("Deactivating user with ID: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                User user = userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));
                user.deactivate();
                userRepository.save(user);
            });
            log.info("Deactivated user with ID: {}", userId);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public UserResponse reactivateUser(UUID userId) {
        log.info("Reactivating user with ID: {}", userId);

        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

            if (user.isActive()) {
                log.warn("User {} is already active", userId);
                return mapUserWithCountry(user);
            }

            user.activate();
            User savedUser = userRepository.save(user);

            log.info("Reactivated user with ID: {}", userId);
            return mapUserWithCountry(savedUser);
        } finally {
            ShardContext.clear();
        }

    }

    @Override
    public boolean existsByEmail(String email) {

        String shardKey = shardLookupService.findShardByUserEmail(email);
        ShardContext.setShardKey(shardKey);

        try {
            log.info("email: '{}' exist in db", email);
            return userRepository.existsByEmail(email);
        } finally {
            ShardContext.clear();
        }
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        // Get user to retrieve email
        String shardKey = shardLookupService.findShardIdByUserId(userId);
        ShardContext.setShardKey(shardKey);

        try {
            User user = userRepository
                .findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

            // Call auth-service to change password
            authServiceClient.changePassword(user.getEmail(), request);

            log.info("Password changed successfully for user: {}", userId);
        } finally {
            ShardContext.clear();
        }
    }

    private UserResponse mapUserWithCountry(User user) {
        UserResponse response = userMapper.toResponse(user);
        if (user.getCountryId() != null) {
            countryRepository
                    .findById(user.getCountryId())
                    .map(countryMapper::toResponse)
                    .ifPresent(response::setCountry);
        }
        return response;
    }

    private UserWorkExperienceResponse mapWorkExperienceWithCountry(
            UserWorkExperience workExp) {
        UserWorkExperienceResponse response = userWorkExperienceMapper.toResponse(workExp);
        if (workExp.getCountryId() != null) {
            countryRepository
                    .findById(workExp.getCountryId())
                    .map(countryMapper::toResponse)
                    .ifPresent(response::setCountry);
        }
        return response;
    }
}
