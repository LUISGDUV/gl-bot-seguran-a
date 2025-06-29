package com.glsecurity.bot.dao;

import com.glsecurity.bot.model.ServerSettings;
import com.glsecurity.bot.model.Violation;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static SessionFactory sessionFactory;

    public DatabaseManager() {
        if (sessionFactory == null) {
            try {
                // Carrega a configuração do Hibernate a partir de hibernate.cfg.xml
                Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

                // Adiciona as classes de entidade
                configuration.addAnnotatedClass(ServerSettings.class);
                configuration.addAnnotatedClass(Violation.class);

                sessionFactory = configuration.buildSessionFactory();
                logger.info("SessionFactory do Hibernate inicializada com sucesso.");
            } catch (Exception e) {
                logger.error("Erro ao inicializar a SessionFactory do Hibernate: " + e.getMessage(), e);
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    /**
     * Retorna uma sessão do Hibernate.
     * @return Uma nova sessão do Hibernate.
     */
    public Session getSession() {
        return sessionFactory.openSession();
    }

    /**
     * Salva ou atualiza as configurações de um servidor.
     * @param settings O objeto ServerSettings a ser salvo.
     */
    public void saveOrUpdateServerSettings(ServerSettings settings) {
        Session session = getSession();
        session.beginTransaction();
        try {
            session.merge(settings); // Usa merge para salvar ou atualizar
            session.getTransaction().commit();
            logger.info("Configurações do servidor {} salvas/atualizadas com sucesso.", settings.getServerId());
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Erro ao salvar/atualizar configurações do servidor {}: {}", settings.getServerId(), e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    /**
     * Obtém as configurações de um servidor pelo ID. Se não existirem, cria e salva configurações padrão.
     * @param serverId O ID do servidor.
     * @return As configurações do servidor, novas ou existentes.
     */
    public ServerSettings getServerSettings(String serverId) {
        Session session = getSession();
        try {
            ServerSettings settings = session.createQuery("FROM ServerSettings WHERE serverId = :serverId", ServerSettings.class)
                                            .setParameter("serverId", serverId)
                                            .uniqueResult();
            if (settings == null) {
                logger.info("Configurações não encontradas para o servidor {}. Criando configurações padrão.", serverId);
                settings = new ServerSettings(serverId);
                saveOrUpdateServerSettings(settings); // Salva as configurações padrão
            }
            return settings;
        } catch (NoResultException e) {
            // Isso não deve acontecer com uniqueResult se o resultado for null, mas para robustez
            logger.info("Configurações não encontradas para o servidor {}. Criando configurações padrão.", serverId);
            ServerSettings settings = new ServerSettings(serverId);
            saveOrUpdateServerSettings(settings);
            return settings;
        } catch (Exception e) {
            logger.error("Erro ao buscar configurações do servidor {}: {}", serverId, e.getMessage(), e);
            // Retorna configurações padrão em caso de erro grave no DB
            return new ServerSettings(serverId);
        } finally {
            session.close();
        }
    }

    /**
     * Salva uma nova violação no banco de dados.
     * @param violation O objeto Violation a ser salvo.
     */
    public void saveViolation(Violation violation) {
        Session session = getSession();
        session.beginTransaction();
        try {
            session.persist(violation); // Usa persist para salvar uma nova entidade
            session.getTransaction().commit();
            logger.info("Violação registrada para o usuário {} no servidor {}.", violation.getUserName(), violation.getServerName());
        } catch (Exception e) {
            session.getTransaction().rollback();
            logger.error("Erro ao salvar violação para o usuário {} no servidor {}: {}", violation.getUserName(), violation.getServerName(), e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    /**
     * Retorna uma lista das últimas N violações.
     * @param limit O número máximo de violações a serem retornadas.
     * @return Uma lista de objetos Violation.
     */
    public List<Violation> getRecentViolations(int limit) {
        Session session = getSession();
        try {
            return session.createQuery("FROM Violation ORDER BY timestamp DESC", Violation.class)
                          .setMaxResults(limit)
                          .getResultList();
        } catch (Exception e) {
            logger.error("Erro ao buscar violações recentes: {}", e.getMessage(), e);
            return List.of(); // Retorna lista vazia em caso de erro
        } finally {
            session.close();
        }
    }

    /**
     * Retorna a contagem total de violações.
     * @return O número total de violações.
     */
    public long getTotalViolationsCount() {
        Session session = getSession();
        try {
            return session.createQuery("SELECT COUNT(*) FROM Violation", Long.class)
                          .uniqueResult();
        } catch (Exception e) {
            logger.error("Erro ao contar violações: {}", e.getMessage(), e);
            return 0;
        } finally {
            session.close();
        }
    }

    /**
     * Fecha a SessionFactory do Hibernate quando o aplicativo é encerrado.
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("SessionFactory do Hibernate fechada.");
        }
    }
}
