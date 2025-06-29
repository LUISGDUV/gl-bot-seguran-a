package com.glsecurity.bot.listener;

import com.glsecurity.bot.config.BotConfig;
import com.glsecurity.bot.dao.DatabaseManager;
import com.glsecurity.bot.model.ServerSettings;
import com.glsecurity.bot.model.Violation;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final DatabaseManager dbManager;

    // Padrão para convites do Discord (discord.gg, discord.com/invite, discordapp.com/invite)
    private static final Pattern DISCORD_INVITE_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?(?:discord\\.(?:gg|io|me|li)|discordapp\\.com/invite|discord\\.com/invite)/([a-zA-Z0-9]+)"
    );

    public MessageListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignorar mensagens de bots e mensagens fora de um servidor (DM)
        if (event.getAuthor().isBot() || !event.isFromGuild()) {
            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw().toLowerCase(); // Conteúdo da mensagem em minúsculas
        String serverId = event.getGuild().getId();
        String serverName = event.getGuild().getName();
        String userId = event.getAuthor().getId();
        String userName = event.getAuthor().getName(); // ou getEffectiveName()

        logger.debug("Mensagem recebida de {}({}) no servidor {}({}): {}", userName, userId, serverName, serverId, message.getContentDisplay());

        // Carregar configurações específicas do servidor
        ServerSettings serverSettings = dbManager.getServerSettings(serverId);

        // Se o autor da mensagem for um administrador do servidor, ignorar a moderação para ele
        // `MANAGE_SERVER` é uma boa permissão para identificar administradores ou moderadores
        if (event.getMember() != null && event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            logger.debug("Usuário {} é administrador. Ignorando moderação.", userName);
            return;
        }

        // --- Verificação de Palavras Ofensivas ---
        if (serverSettings.isBlockProfaneWords()) {
            for (String word : BotConfig.getProfaneWords()) { // Usar palavras do BotConfig.java (config global)
                if (content.contains(word)) {
                    logger.info("Palavra ofensiva detectada: '{}' na mensagem de {}.", word, userName);
                    handleViolation(message, serverSettings,
                            "PROFANE_WORD", "Uso de palavra ofensiva: '" + word + "'", message.getContentDisplay());
                    return; // Uma violação por mensagem é suficiente
                }
            }
        }

        // --- Verificação de Links ---
        if (serverSettings.isBlockLinks()) {
            // Expressão regular simples para URLs (pode ser mais complexa se necessário)
            String urlRegex = "(http|https)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,3}(/\\S*)?";
            if (Pattern.compile(urlRegex).matcher(content).find()) {
                logger.info("Link detectado na mensagem de {}.", userName);
                handleViolation(message, serverSettings,
                        "LINK", "Envio de link não permitido", message.getContentDisplay());
                return;
            }
        }

        // --- Verificação de Convites do Discord ---
        if (serverSettings.isBlockInvites()) {
            Matcher matcher = DISCORD_INVITE_PATTERN.matcher(content);
            if (matcher.find()) {
                String inviteCode = matcher.group(1);
                // Opcional: verificar se o convite é para o próprio servidor (permitir nesse caso)
                if (!event.getGuild().retrieveInvites().complete().stream()
                        .anyMatch(invite -> invite.getCode().equals(inviteCode))) {
                    logger.info("Convite do Discord detectado: '{}' na mensagem de {}.", inviteCode, userName);
                    handleViolation(message, serverSettings,
                            "INVITE", "Envio de convite do Discord não permitido", message.getContentDisplay());
                    return;
                }
            }
        }
    }

    private void handleViolation(Message message, ServerSettings serverSettings, String violationType, String reason, String messageContent) {
        // Deletar a mensagem ofensiva
        message.delete().queue(
                success -> logger.info("Mensagem deletada de {}: '{}'.", message.getAuthor().getName(), message.getContentDisplay()),
                error -> logger.error("Falha ao deletar mensagem de {}: {}.", message.getAuthor().getName(), error.getMessage())
        );

        // Notificar o usuário
        String warningMessage = String.format("❌ **GL Security Bot** ❌\n" +
                "Sua mensagem foi deletada no servidor **%s** devido a: **%s**.",
                message.getGuild().getName(), reason);

        switch (serverSettings.getWarningType().toLowerCase()) {
            case "dm":
                message.getAuthor().openPrivateChannel().queue(
                        channel -> channel.sendMessage(warningMessage).queue(
                                success -> logger.info("Aviso enviado por DM para {}.", message.getAuthor().getName()),
                                error -> logger.error("Falha ao enviar aviso por DM para {}: {}.", message.getAuthor().getName(), error.getMessage())
                        )
                );
                break;
            case "public":
                if (message.getChannelType() == ChannelType.TEXT || message.getChannelType() == ChannelType.NEWS) {
                    GuildMessageChannel guildChannel = (GuildMessageChannel) message.getChannel();
                    guildChannel.sendMessage(String.format("%s, %s", message.getAuthor().getAsMention(), warningMessage)).queue(
                            msg -> {
                                logger.info("Aviso enviado publicamente no canal para {}.", message.getAuthor().getName());
                                if (serverSettings.isAutoDeleteWarnings()) {
                                    msg.delete().queueAfter(serverSettings.getWarningDeleteDelay(), TimeUnit.SECONDS,
                                            s -> logger.info("Aviso público deletado automaticamente."),
                                            e -> logger.error("Falha ao deletar aviso público automaticamente: {}", e.getMessage())
                                    );
                                }
                            },
                            error -> logger.error("Falha ao enviar aviso público para {}: {}.", message.getAuthor().getName(), error.getMessage())
                    );
                }
                break;
            case "both":
                // Enviar DM
                message.getAuthor().openPrivateChannel().queue(
                        channel -> channel.sendMessage(warningMessage).queue(
                                success -> logger.info("Aviso enviado por DM para {}.", message.getAuthor().getName()),
                                error -> logger.error("Falha ao enviar aviso por DM para {}: {}.", message.getAuthor().getName(), error.getMessage())
                        )
                );
                // Enviar público (se o canal for de texto)
                if (message.getChannelType() == ChannelType.TEXT || message.getChannelType() == ChannelType.NEWS) {
                    GuildMessageChannel guildChannel = (GuildMessageChannel) message.getChannel();
                    guildChannel.sendMessage(String.format("%s, %s", message.getAuthor().getAsMention(), warningMessage)).queue(
                            msg -> {
                                logger.info("Aviso enviado publicamente no canal para {}.", message.getAuthor().getName());
                                if (serverSettings.isAutoDeleteWarnings()) {
                                    msg.delete().queueAfter(serverSettings.getWarningDeleteDelay(), TimeUnit.SECONDS,
                                            s -> logger.info("Aviso público deletado automaticamente."),
                                            e -> logger.error("Falha ao deletar aviso público automaticamente: {}", e.getMessage())
                                    );
                                }
                            },
                            error -> logger.error("Falha ao enviar aviso público para {}: {}.", message.getAuthor().getName(), error.getMessage())
                    );
                }
                break;
        }

        // Registrar a violação no banco de dados se a opção estiver ativada
        if (serverSettings.isLogViolations()) {
            Violation violation = new Violation(
                    message.getGuild().getId(),
                    message.getGuild().getName(),
                    message.getAuthor().getId(),
                    message.getAuthor().getName(), // Ou getEffectiveName()
                    violationType,
                    reason,
                    messageContent // Conteúdo original da mensagem para registro
            );
            dbManager.saveViolation(violation);
        }
    }
}
