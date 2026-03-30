package com.techstore.chat.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.techstore.chat.client.UserServiceClient;
import com.techstore.chat.constant.UserType;
import com.techstore.chat.dto.request.ConversationRequest;
import com.techstore.chat.dto.response.ApiResponse;
import com.techstore.chat.dto.response.ConversationResponse;
import com.techstore.chat.entity.Conversation;
import com.techstore.chat.entity.ParticipantInfo;
import com.techstore.chat.exception.AppException;
import com.techstore.chat.exception.ErrorCode;
import com.techstore.chat.mapper.ConversationMapper;
import com.techstore.chat.repository.ConversationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    UserServiceClient profileClient;

    ConversationMapper conversationMapper;

    public List<ConversationResponse> myConversations() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Conversation> conversations = conversationRepository.findAllByParticipantIdsContains(userId);

        return conversations.stream().map(this::toConversationResponse).toList();
    }

    public ConversationResponse create(ConversationRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserId = jwt.getSubject();

        if (request.getParticipantIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String targetUserId = request.getParticipantIds().getFirst();
        UserType userCurrentType = UserType.valueOf(request.getUserCurrentType());
        UserType userTargetType = UserType.valueOf(request.getUserTargetType());

        ParticipantInfo currentUser = getParticipantInfo(currentUserId, userCurrentType);
        currentUser.setUserType(userCurrentType.name());

        ParticipantInfo targetUser = getParticipantInfo(targetUserId, userTargetType);
        targetUser.setUserType(userTargetType.name());

        List<String> sortedIds = Stream.of(
                        currentUser.getUserId() + "_" + currentUser.getUserType(),
                        targetUser.getUserId() + "_" + targetUser.getUserType())
                .sorted()
                .toList();

        String participantsHash = generateParticipantHash(sortedIds);

        Conversation conversation = conversationRepository
                .findByParticipantsHash(participantsHash)
                .orElseGet(() -> {
                    Conversation newConversation = Conversation.builder()
                            .type(request.getType())
                            .participantsHash(participantsHash)
                            .createdDate(Instant.now())
                            .modifiedDate(Instant.now())
                            .participants(List.of(currentUser, targetUser))
                            .build();

                    return conversationRepository.save(newConversation);
                });

        return toConversationResponse(conversation);
    }

    private ParticipantInfo getParticipantInfo(String userId, UserType userType) {
        log.info(userId);
        log.info(userType.name());
        try {
            switch (userType) {
                case INTERNAL -> {
                    try {
                        var response = profileClient.getStaffById(Long.parseLong(userId));
                        var staff = Optional.ofNullable(response)
                                .map(ApiResponse::getResult)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                        return ParticipantInfo.builder()
                                .userId(String.valueOf(staff.getId()))
                                .username(staff.getFullName())
                                .avatar("") // staff không có avatar
                                .userType(UserType.INTERNAL.name())
                                .build();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new AppException(ErrorCode.INVALID_USER_ID);
                    }
                }

                case EXTERNAL -> {
                    try {
                        var response = profileClient.getCustomerById(Long.parseLong(userId));
                        var customer = Optional.ofNullable(response)
                                .map(ApiResponse::getResult)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                        return ParticipantInfo.builder()
                                .userId(String.valueOf(customer.getId()))
                                .username(customer.getFullName())
                                .avatar(customer.getAvatarUrl())
                                .userType(UserType.EXTERNAL.name())
                                .build();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new AppException(ErrorCode.INVALID_USER_ID);
                    }
                }

                default -> throw new AppException(ErrorCode.INVALID_USER_TYPE);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_USER_ID);
        }
    }

    private String generateParticipantHash(List<String> ids) {
        StringJoiner stringJoiner = new StringJoiner("_");
        ids.forEach(stringJoiner::add);

        // SHA 256

        return stringJoiner.toString();
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();

        ConversationResponse conversationResponse = conversationMapper.toConversationResponse(conversation);

        conversation.getParticipants().stream()
                .filter(participantInfo -> !participantInfo.getUserId().equals(currentUserId))
                .findFirst()
                .ifPresent(participantInfo -> {
                    conversationResponse.setConversationName(participantInfo.getUsername());
                    conversationResponse.setConversationAvatar(participantInfo.getAvatar());
                });

        return conversationResponse;
    }
}
