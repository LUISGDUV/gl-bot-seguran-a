package com.glsecurity.bot;

import com.glsecurity.bot.config.BotConfig;
import com.glsecurity.bot.dao.DatabaseManager;
import com.glsecurity.bot.listener.MessageListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class GLSecurityBot {

    private static final Logger logger = LoggerFactory.getLogger(GLSecurityBot.class);

    public static void main(String[] args) {
        // Carrega o token do bot de um arquivo .env
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.load();
            logger.info("Arquivo .env carregado com sucesso.");
        } catch (io.github.cdimascio.dotenv.DotenvException e) {
            logger.error("Arquivo .env não encontrado ou inválido. Certifique-se de que ele existe na raiz do projeto e contém DISCORD_BOT_TOKEN. Erro: {}", e.getMessage());
            System.exit(1); // Encerra o programa se o .env não for encontrado
        }

        String botToken = dotenv.get("DISCORD_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            logger.error("O token do bot Discord não foi encontrado no arquivo .env. Por favor, defina DISCORD_BOT_TOKEN.");
            System.exit(1);
        }

        // Inicializa o DatabaseManager
        DatabaseManager dbManager = new DatabaseManager();

        // Carrega as configurações globais do bot
        BotConfig.loadConfig();

        // Define os intents necessários para o bot
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,          // Para receber eventos de mensagem em guilds
                GatewayIntent.MESSAGE_CONTENT,         // **Essencial** para acessar o conteúdo das mensagens
                GatewayIntent.GUILD_MEMBERS            // Para acessar informações de membros (permissões, etc.)
        );

        try {
            JDA jda = JDABuilder.createDefault(botToken)
                    .enableIntents(intents)
                    .addEventListeners(new MessageListener(dbManager)) // Adiciona o listener de mensagens
                    .build();

            // Espera até que o bot esteja pronto (conectado ao Discord)
            jda.awaitReady();
            logger.info("Bot GL Security online e conectado ao Discord!");

            // Adiciona um shutdown hook para fechar a SessionFactory do Hibernate
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando o bot... Fechando recursos do banco de dados.");
                DatabaseManager.shutdown();
            }));

        } catch (InterruptedException e) {
            logger.error("O processo de conexão do bot foi interrompido: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Ocorreu um erro ao iniciar o bot: {}", e.getMessage(), e);
            logger.error("Verifique se o token está correto e se as Intents Privilegiadas (Message Content Intent, Server Members Intent) estão ativadas no Portal do Desenvolvedor do Discord.");
        }
    }
}
