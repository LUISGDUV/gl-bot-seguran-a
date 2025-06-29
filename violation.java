package com.glsecurity.bot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "violations")
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private String serverId;

    @Column(name = "server_name", nullable = false)
    private String serverName;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "violation_type", nullable = false)
    private String violationType; // e.g., "PROFANE_WORD", "LINK", "INVITE"

    @Column(name = "reason", nullable = true, length = 500)
    private String reason; // Detalhes específicos da violação

    @Column(name = "message_content", nullable = true, length = 2000)
    private String messageContent; // Conteúdo da mensagem que causou a violação

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Construtor padrão exigido pelo JPA
    public Violation() {
    }

    // Construtor para criar uma nova violação
    public Violation(String serverId, String serverName, String userId, String userName, String violationType, String reason, String messageContent) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.userId = userId;
        this.userName = userName;
        this.violationType = violationType;
        this.reason = reason;
        this.messageContent = messageContent;
        this.timestamp = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Violation{" +
               "id=" + id +
               ", serverId='" + serverId + '\'' +
               ", serverName='" + serverName + '\'' +
               ", userId='" + userId + '\'' +
               ", userName='" + userName + '\'' +
               ", violationType='" + violationType + '\'' +
               ", reason='" + reason + '\'' +
               ", messageContent='" + messageContent + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
