package com.glsecurity.bot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotConfig {
    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);
    private static final String CONFIG_FILE_PATH = "bot_config.json";
    private static ConfigData configData;

    // Classe interna para representar a estrutura do JSON
    private static class ConfigData {
        private List<String> profane_words;
        private boolean block_links;
        private boolean block_invites; // Novo campo
        private String warning_type; // dm, public, both
        private boolean admin_only_commands;
        private boolean auto_delete_warnings;
        private int warning_delete_delay; // Em segundos
        private boolean log_violations; // Novo campo para logging de violações no DB

        public ConfigData() {
            // Valores padrão
            this.profane_words = Arrays.asList(
                "puta", "caralho", "fodase", "arrombado", "viado", "cuzao",
                "vadia", "merda", "desgraça", "inferno", "corno", "buceta",
                "filho da puta", "porra", "cuzinho", "maldito", "idiota", "retardado",
                "cabra da peste", "putz", "cacete", "bosta", "poha", "pqp",
                "foda", "vtnc", "vsf", "krl", "fdp", "gay", "lésbica", "bicha",
                "traveco", "sapatao", "mongolóide", "deficiente", "aleijado",
                "lixo" // Adicionei 'lixo'
            );
            this.block_links = true;
            this.block_invites = true; // Valor padrão
            this.warning_type = "both";
            this.admin_only_commands = true;
            this.auto_delete_warnings = true;
            this.warning_delete_delay = 60; // 60 segundos
            this.log_violations = true; // Valor padrão
        }
    }

    // Inicializa a configuração (chamado uma vez no início)
    static {
        loadConfig();
    }

    public static void loadConfig() {
        Gson gson = new Gson();
        File configFile = new File(CONFIG_FILE_PATH);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                configData = gson.fromJson(reader, ConfigData.class);
                // Garantir que novos campos tenham valores padrão se não estiverem no JSON existente
                if (configData.block_invites == null) configData.block_invites = new ConfigData().block_invites;
                if (configData.log_violations == null) configData.log_violations = new ConfigData().log_violations;
                logger.info("Configuração carregada de '{}'.", CONFIG_FILE_PATH);
            } catch (IOException e) {
                logger.error("Erro ao carregar configuração de '{}'. Usando valores padrão. Erro: {}", CONFIG_FILE_PATH, e.getMessage());
                configData = new ConfigData(); // Usar padrões em caso de erro de leitura
                saveConfig(); // Tenta salvar para criar um arquivo válido
            }
        } else {
            logger.warn("Arquivo de configuração '{}' não encontrado. Criando com valores padrão.", CONFIG_FILE_PATH);
            configData = new ConfigData();
            saveConfig(); // Salva os valores padrão em um novo arquivo
        }
    }

    public static void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(CONFIG_FILE_PATH)) {
            gson.toJson(configData, writer);
            logger.info("Configuração salva em '{}'.", CONFIG_FILE_PATH);
        } catch (IOException e) {
            logger.error("Erro ao salvar configuração em '{}'. Erro: {}", CONFIG_FILE_PATH, e.getMessage());
        }
    }

    // Métodos Getters
    public static List<String> getProfaneWords() {
        return configData.profane_words;
    }

    public static boolean isBlockLinks() {
        return configData.block_links;
    }

    public static boolean isBlockInvites() { // Getter para block_invites
        return configData.block_invites;
    }

    public static String getWarningType() {
        return configData.warning_type;
    }

    public static boolean isAdminOnlyCommands() {
        return configData.admin_only_commands;
    }

    public static boolean isAutoDeleteWarnings() {
        return configData.auto_delete_warnings;
    }

    public static int getWarningDeleteDelay() {
        return configData.warning_delete_delay;
    }

    public static boolean isLogViolations() { // Getter para log_violations
        return configData.log_violations;
    }


    // Métodos Setters (para atualização via painel web, por exemplo)
    public static void updateProfaneWords(List<String> words) {
        configData.profane_words = words;
    }

    public static void setBlockLinks(boolean blockLinks) {
        configData.block_links = blockLinks;
    }

    public static void setBlockInvites(boolean blockInvites) { // Setter para block_invites
        configData.block_invites = blockInvites;
    }

    public static void setWarningType(String warningType) {
        if (Arrays.asList("dm", "public", "both").contains(warningType)) {
            configData.warning_type = warningType;
        } else {
            logger.warn("Tipo de aviso inválido: {}. Mantendo o tipo atual.", warningType);
        }
    }

    public static void setAdminOnlyCommands(boolean adminOnlyCommands) {
        configData.admin_only_commands = adminOnlyCommands;
    }

    public static void setAutoDeleteWarnings(boolean autoDeleteWarnings) {
        configData.auto_delete_warnings = autoDeleteWarnings;
    }

    public static void setWarningDeleteDelay(int warningDeleteDelay) {
        if (warningDeleteDelay > 0) {
            configData.warning_delete_delay = warningDeleteDelay;
        } else {
            logger.warn("Atraso para deletar aviso inválido: {}. Deve ser maior que 0. Mantendo o valor atual.", warningDeleteDelay);
        }
    }

    public static void setLogViolations(boolean logViolations) { // Setter para log_violations
        configData.log_violations = logViolations;
    }
}
