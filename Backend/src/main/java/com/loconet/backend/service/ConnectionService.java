// File: Backend/src/main/java/com/loconet/backend/service/ConnectionService.java
package com.loconet.backend.service;

import com.loconet.backend.dto.ConnectionRequestDTO;
import com.loconet.backend.dto.ConnectionResponseDTO;
import com.loconet.backend.dto.UserSummaryDTO;
import com.loconet.backend.entity.MatchStatus;
import com.loconet.backend.entity.User;
import com.loconet.backend.entity.UserConnection;
import com.loconet.backend.exception.ConnectionConflictException;
import com.loconet.backend.exception.InvalidConnectionRequestException;
import com.loconet.backend.exception.ResourceNotFoundException;
import com.loconet.backend.repository.UserConnectionRepository;
import com.loconet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final UserRepository userRepository;

    public ConnectionService(UserConnectionRepository userConnectionRepository,
                              UserRepository userRepository) {
        this.userConnectionRepository = userConnectionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConnectionResponseDTO sendRequest(ConnectionRequestDTO request) {
        UUID senderId = request.getSenderId();
        UUID receiverId = request.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new InvalidConnectionRequestException("A user cannot send a connection request to themselves");
        }

        if (userConnectionRepository.existsConnectionBetween(senderId, receiverId)) {
            throw new ConnectionConflictException("A connection already exists between these users");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found: " + receiverId));

        UserConnection connection = UserConnection.builder()
                .sender(sender)
                .receiver(receiver)
                .status(MatchStatus.PENDING)
                .build();

        UserConnection saved = userConnectionRepository.save(connection);
        return toDto(saved);
    }

    @Transactional
    public ConnectionResponseDTO respondToRequest(UUID connectionId, MatchStatus decision) {
        if (decision != MatchStatus.ACCEPTED && decision != MatchStatus.REJECTED) {
            throw new InvalidConnectionRequestException("status must be ACCEPTED or REJECTED");
        }

        UserConnection connection = userConnectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found: " + connectionId));

        if (connection.getStatus() != MatchStatus.PENDING) {
            throw new ConnectionConflictException(
                    "This request has already been " + connection.getStatus().name().toLowerCase());
        }

        connection.setStatus(decision);
        UserConnection updated = userConnectionRepository.save(connection);
        return toDto(updated);
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getPendingRequests(UUID userId) {
        return userConnectionRepository.findByReceiverIdAndStatus(userId, MatchStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ConnectionResponseDTO toDto(UserConnection connection) {
        return ConnectionResponseDTO.builder()
                .id(connection.getId())
                .sender(toSummary(connection.getSender()))
                .receiver(toSummary(connection.getReceiver()))
                .status(connection.getStatus())
                .createdAt(connection.getCreatedAt())
                .updatedAt(connection.getUpdatedAt())
                .build();
    }

    private UserSummaryDTO toSummary(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .userTier(user.getUserTier())
                .intent(user.getIntent())
                .build();
    }
}
