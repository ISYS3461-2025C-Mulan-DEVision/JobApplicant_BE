package com.team.ja.user.service.impl;

import com.team.ja.common.exception.BadRequestException;
import com.team.ja.common.exception.ConflictException;
import com.team.ja.common.exception.NotFoundException;
import com.team.ja.user.config.S3FileService;
import com.team.ja.user.dto.request.CreateUserRequest;
import com.team.ja.user.dto.request.UpdateUserRequest;
import com.team.ja.user.dto.response.SkillResponse;
import com.team.ja.user.dto.response.UserEducationResponse;
import com.team.ja.user.dto.response.UserProfileResponse;
import com.team.ja.user.dto.response.UserResponse;
import com.team.ja.user.dto.response.UserWorkExperienceResponse;
import com.team.ja.user.mapper.CountryMapper;
import com.team.ja.user.mapper.SkillMapper;
import com.team.ja.user.mapper.UserEducationMapper;
import com.team.ja.user.mapper.UserMapper;
import com.team.ja.user.mapper.UserWorkExperienceMapper;
import com.team.ja.user.model.Skill;
import com.team.ja.user.model.User;
import com.team.ja.user.model.UserEducation;
import com.team.ja.user.model.UserSkill;
import com.team.ja.user.model.UserWorkExperience;
import com.team.ja.user.repository.CountryRepository;
import com.team.ja.user.repository.SkillRepository;
import com.team.ja.user.repository.UserEducationRepository;
import com.team.ja.user.repository.UserRepository;
import com.team.ja.user.repository.UserSkillRepository;
import com.team.ja.user.repository.UserWorkExperienceRepository;
import com.team.ja.user.service.UserService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
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

    private final UserMapper userMapper;
    private final UserEducationMapper userEducationMapper;
    private final UserWorkExperienceMapper userWorkExperienceMapper;
    private final SkillMapper skillMapper;
    private final CountryMapper countryMapper;

    private static final int AVATAR_SIZE = 256;
    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of(
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                "User with email " + request.getEmail() + " already exists"
            );
        }

        // Validate country if provided
        if (request.getCountryId() != null) {
            countryRepository
                .findById(request.getCountryId())
                .orElseThrow(() ->
                    new NotFoundException(
                        "Country",
                        "id",
                        request.getCountryId().toString()
                    )
                );
        }

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

        return mapUserWithCountry(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository
            .findById(userId)
            .filter(User::isActive)
            .orElseThrow(() ->
                new NotFoundException("User", "id", userId.toString())
            );

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getCountryId() != null) {
            // Validate country exists
            countryRepository
                .findById(request.getCountryId())
                .orElseThrow(() ->
                    new NotFoundException(
                        "Country",
                        "id",
                        request.getCountryId().toString()
                    )
                );
            user.setCountryId(request.getCountryId());
        }
        if (request.getObjectiveSummary() != null) {
            user.setObjectiveSummary(request.getObjectiveSummary());
        }

        user.markProfileUpdated();
        User savedUser = userRepository.save(user);

        log.info("Updated user with ID: {}", savedUser.getId());
        return mapUserWithCountry(savedUser);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(UUID userId, MultipartFile file) {
        log.info("Uploading avatar for user {}", userId);

        User user = userRepository
            .findById(userId)
            .filter(User::isActive)
            .orElseThrow(() ->
                new NotFoundException("User", "id", userId.toString())
            );

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty.");
        }
        if (!SUPPORTED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException(
                "Unsupported image type. Please upload a JPEG, PNG, or GIF."
            );
        }

        try {
            // Resize image
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC,
                AVATAR_SIZE,
                AVATAR_SIZE,
                Scalr.OP_ANTIALIAS
            );

            // Convert back to input stream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            String format = file.getContentType().split("/")[1]; // "jpeg" or "png"
            ImageIO.write(resizedImage, format, os);
            byte[] imageBytes = os.toByteArray();
            String contentType = "image/" + format.toLowerCase();

            // Delete old avatar if it exists
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                s3FileService.deleteFile(user.getAvatarUrl());
                log.info("Deleted old avatar for user {}", userId);
            }

            // Upload new avatar
            String avatarUrl = s3FileService.uploadFile(
                imageBytes,
                file.getOriginalFilename(),
                contentType,
                "avatars"
            );

            // Update user
            user.setAvatarUrl(avatarUrl);
            user.markProfileUpdated();
            User savedUser = userRepository.save(user);

            log.info(
                "Successfully uploaded avatar for user {}. URL: {}",
                userId,
                avatarUrl
            );
            return mapUserWithCountry(savedUser);
        } catch (IOException e) {
            log.error("Failed to process image for user {}", userId, e);
            throw new BadRequestException("Could not process image file.");
        }
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository
            .findById(id)
            .filter(User::isActive)
            .orElseThrow(() ->
                new NotFoundException("User", "id", id.toString())
            );
        return mapUserWithCountry(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository
            .findByEmailAndIsActiveTrue(email)
            .orElseThrow(() -> new NotFoundException("User", "email", email));
        return mapUserWithCountry(user);
    }

    @Override
    public UserProfileResponse getUserProfile(UUID userId) {
        log.info("Fetching complete profile for user: {}", userId);

        // Fetch user
        User user = userRepository
            .findById(userId)
            .filter(User::isActive)
            .orElseThrow(() ->
                new NotFoundException("User", "id", userId.toString())
            );

        // Fetch related data
        List<UserEducation> education =
            userEducationRepository.findByUserIdAndIsActiveTrueOrderByStartAtDesc(
                userId
            );

        List<UserWorkExperience> workExperience =
            userWorkExperienceRepository.findByUserIdAndIsActiveTrueOrderByStartAtDesc(
                userId
            );

        List<UserSkill> userSkills =
            userSkillRepository.findByUserIdAndIsActiveTrue(userId);
        List<UUID> skillIds = userSkills
            .stream()
            .map(UserSkill::getSkillId)
            .toList();
        List<Skill> skills = skillIds.isEmpty()
            ? List.of()
            : skillRepository.findByIdInAndIsActiveTrue(skillIds);

        // Map education with country
        List<UserEducationResponse> educationResponses = education
            .stream()
            .map(userEducationMapper::toResponse)
            .toList();

        // Map work experience with country
        List<UserWorkExperienceResponse> workExpResponses = workExperience
            .stream()
            .map(this::mapWorkExperienceWithCountry)
            .toList();

        // Map skills
        List<SkillResponse> skillResponses = skillMapper.toResponseList(skills);

        return UserProfileResponse.builder()
            .user(mapUserWithCountry(user))
            .education(educationResponses)
            .workExperience(workExpResponses)
            .skills(skillResponses)
            .build();
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all active users");
        List<User> users = userRepository.findAll();
        return users
            .stream()
            .filter(User::isActive)
            .map(this::mapUserWithCountry)
            .toList();
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        log.info("Deactivating user with ID: {}", userId);

        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new NotFoundException("User", "id", userId.toString())
            );

        user.deactivate();
        userRepository.save(user);

        log.info("Deactivated user with ID: {}", userId);
    }

    @Override
    @Transactional
    public UserResponse reactivateUser(UUID userId) {
        log.info("Reactivating user with ID: {}", userId);

        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new NotFoundException("User", "id", userId.toString())
            );

        if (user.isActive()) {
            log.warn("User {} is already active", userId);
            return mapUserWithCountry(user);
        }

        user.activate();
        User savedUser = userRepository.save(user);

        log.info("Reactivated user with ID: {}", userId);
        return mapUserWithCountry(savedUser);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Map User entity to UserResponse with nested Country.
     */
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

    /**
     * Map UserWorkExperience to response with nested Country.
     */
    private UserWorkExperienceResponse mapWorkExperienceWithCountry(
        UserWorkExperience workExp
    ) {
        UserWorkExperienceResponse response =
            userWorkExperienceMapper.toResponse(workExp);

        if (workExp.getCountryId() != null) {
            countryRepository
                .findById(workExp.getCountryId())
                .map(countryMapper::toResponse)
                .ifPresent(response::setCountry);
        }

        return response;
    }
}
