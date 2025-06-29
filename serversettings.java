package com.glsecurity.bot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "server_settings")
public class ServerSettings {

    @Id
    @Column(name = "server_id", unique = true, nullable = false)
    private String serverId;

    @Column(name = "block_profane_words", nullable = false)
    private boolean blockProfaneWords;

    @Column(name = "block_links", nullable = false)
    private boolean blockLinks;

    @Column(name = "block_invites", nullable = false)
    private boolean blockInvites;

    @Column(name = "warning_type", nullable = false)
    private String warningType; // "dm", "public", "both"

    @Column(name = "admin_only_commands", nullable = false)
    private boolean adminOnlyCommands;

    @Column(name = "auto_delete_warnings", nullable = false)
    private boolean autoDeleteWarnings;

    @Column(name = "warning_delete_delay", nullable = false)
    private int warningDeleteDelay; // Em segundos

    @Column(name = "log_violations", nullable = false)
    private boolean logViolations;

    // Construtor padrão exigido pelo JPA
    public ServerSettings() {
        // Inicializa com valores padrão sensatos
        this.blockProfaneWords = true;
        this.blockLinks = true;
        this.blockInvites = true;
        this.warningType = "both";
        this.adminOnlyCommands = true;
        this.autoDeleteWarnings = true;
        this.warningDeleteDelay = 60;
        this.logViolations = true;
    }

    // Construtor para criar uma nova configuração com ID de servidor
    public ServerSettings(String serverId) {
        this(); // Chama o construtor padrão para inicializar valores
        this.serverId = serverId;
    }

    // Getters e Setters
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isBlockProfaneWords() {
        return blockProfaneWords;
    }

    public void setBlockProfaneWords(boolean blockProfaneWords) {
        this.blockProfaneWords = blockProfaneWords;
    }

    public boolean isBlockLinks() {
        return blockLinks;
    }

    public void setBlockLinks(boolean blockLinks) {
        this.blockLinks = blockLinks;
    }

    public boolean isBlockInvites() {
        return blockInvites;
    }

    public void setBlockInvites(boolean blockInvites) {
        this.blockInvites = blockInvites;
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public boolean isAdminOnlyCommands() {
        return adminOnlyCommands;
    }

    public void setAdminOnlyCommands(boolean adminOnlyCommands) {
        this.adminOnlyCommands = adminOnlyCommands;
    }

    public boolean isAutoDeleteWarnings() {
        return autoDeleteWarnings;
    }

    public void setAutoDeleteWarnings(boolean autoDeleteWarnings) {
        this.autoDeleteWarnings = autoDeleteWarnings;
    }

    public int getWarningDeleteDelay() {
        return warningDeleteDelay;
    }

    public void setWarningDeleteDelay(int warningDeleteDelay) {
        this.warningDeleteDelay = warningDeleteDelay;
    }

    public boolean isLogViolations() {
        return logViolations;
    }

    public void setLogViolations(boolean logViolations) {
        this.logViolations = logViolations;
    }

    @Override
    public String toString() {
        return "ServerSettings{" +
               "serverId='" + serverId + '\'' +
               ", blockProfaneWords=" + blockProfaneWords +
               ", blockLinks=" + blockLinks +
               ", blockInvites=" + blockInvites +
               ", warningType='" + warningType + '\'' +
               ", adminOnlyCommands=" + adminOnlyCommands +
               ", autoDeleteWarnings=" + autoDeleteWarnings +
               ", warningDeleteDelay=" + warningDeleteDelay +
               ", logViolations=" + logViolations +
               '}';
    }
}
