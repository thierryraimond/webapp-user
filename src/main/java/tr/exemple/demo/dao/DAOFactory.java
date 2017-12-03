package tr.exemple.demo.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DAOFactory {

    // private static final String FICHIER_PROPERTIES = "/tr/exemple/demo/dao/dao.properties";
    private static final String FICHIER_PROPERTIES = "dao.properties";
    private static final String PROPERTY_URL = "url";
    private static final String PROPERTY_DRIVER = "driver";
    private static final String PROPERTY_NOM_UTILISATEUR = "nomutilisateur";
    private static final String PROPERTY_MOT_DE_PASSE = "motdepasse";

    final static Logger log = Logger.getLogger(DAOFactory.class);

    /* package */BoneCP connectionPool = null;

    /* package */ DAOFactory(BoneCP connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Méthode chargée de récupérer les informations de connexion à la base de données, charger le driver JDBC et
     * retourner une instance de la Factory
     * 
     * @return une instance de la Factory
     * @throws DAOConfigurationException
     */
    public static DAOFactory getInstance() throws DAOConfigurationException {

        log.trace("Initialisation du DAOFactory.");

        // Hashmap du couple (clé, valeur) du fichier properties
        HashMap<String, String> hmap = new HashMap<String, String>();

        // Connexion Pool de BoneCP initialisée à null
        BoneCP connectionPool = null;

        InputStream fichierProperties = getInputStream(FICHIER_PROPERTIES);
        // FileInputStream fichierProperties = getFileInputStream(FICHIER_PROPERTIES);

        if (fichierProperties == null) {
            throw new DAOConfigurationException("Le fichier properties " + FICHIER_PROPERTIES + " est introuvable.");
        } else {
            log.info("Le fichier " + FICHIER_PROPERTIES + " a été correctement chargé.");

            // Charge la liste des propriétés du fichier properties dans la map
            hmap = getPropertiesKeysValues(fichierProperties);
        }

        // Connexion au driver jdbc
        connexionDriverJdbc(hmap);

        /*
         * Création d'une configuration de pool de connexions via l'objet BoneCPConfig et les différents setters
         * associés.
         */
        connectionPool = connexionBoneCP(hmap);

        /*
         * Enregistrement du pool créé dans une variable d'instance via un appel au constructeur de DAOFactory
         */
        DAOFactory instance = new DAOFactory(connectionPool);
        log.info("Instanciation du DAOFactory : " + instance);
        return instance;
    }

    /**
     * Méthode chargée de fournir une connexion à la base de données
     * 
     * @return BoneCP connectionPool
     * @throws SQLException
     */
    /* package */Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    /*
     * Méthodes de récupération de l'implémentation des différents DAO (un seul pour le moment)
     */
    // public UtilisateurDao getUtilisateurDao() {
    // return new UtilisateurDaoImpl(this);
    // }

    /**
     * Retourne InputStream qui prouve l'existence du fichier Properties dans le ClassLoader ici src/main/resources
     * 
     * @param cheminFichierProperties
     *            chemin du fichier Properties
     * @return InputStream
     */
    protected static InputStream getInputStream(String cheminFichierProperties) {
        // gestion de notre fichier properties
        log.trace("Ouverture du fichier " + cheminFichierProperties);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream fichierProperties = classLoader.getResourceAsStream(cheminFichierProperties);
        // InputStream fichierProperties = DAOFactory.class.getResourceAsStream(cheminFichierProperties);
        return fichierProperties;
    }

    /**
     * Méthode qui retourne un flux de données à partir d'un chemin de fichier cité en paramètre
     * 
     * @param cheminFichierProperties
     *            Chemin du fichier Properties
     * @return FileInputStream flux de données - exemple : java.io.FileInputStream@5f205aa
     */
    protected static FileInputStream getFileInputStream(String cheminFichierProperties) {
        FileInputStream fichierProperties = null;
        try {
            log.trace("Tentative de récupération du fichier : " + cheminFichierProperties);
            fichierProperties = new FileInputStream(cheminFichierProperties);
            log.info("SUCCESS : le fichier " + cheminFichierProperties + " a bien été trouvé.");
        } catch (FileNotFoundException e) {
            throw new DAOConfigurationException(
                    "Le fichier properties " + cheminFichierProperties + " est introuvable.", e);
        }
        return fichierProperties;
    }

    /**
     * Retourne les données du fichier properties cités en paramètre sous la forme d'une HashMap
     * 
     * @param fichierProperties
     *            Flux de données du fichier properties de type FileInputStream
     * @return HashMap Retourne les données du fichier properties sous la forme d'une HashMap
     */
    protected static HashMap<String, String> getPropertiesKeysValues(InputStream fichierProperties) {
        // Déclaration de la map
        HashMap<String, String> hmap = new HashMap<String, String>();
        // Déclaration de l'objet Properties
        Properties properties = new Properties();

        try {
            /* Récupération des champs du fichier properties */
            log.trace("Récupération de chaque champ du fichier properties.");
            properties.load(fichierProperties);

            /* Ajoute le couple (clé, valeur) de chaque ligne dans la HashMap */
            hmap.put(PROPERTY_URL, properties.getProperty(PROPERTY_URL)); // url
            hmap.put(PROPERTY_DRIVER, properties.getProperty(PROPERTY_DRIVER)); // driver
            hmap.put(PROPERTY_NOM_UTILISATEUR, properties.getProperty(PROPERTY_NOM_UTILISATEUR)); // nomUtilisateur
            hmap.put(PROPERTY_MOT_DE_PASSE, properties.getProperty(PROPERTY_MOT_DE_PASSE)); // motDePasse
            log.info("Tous les champs ont été récupérés.");
            fichierProperties.close();
            log.trace("Fermeture de l'input stream " + fichierProperties);
        } catch (FileNotFoundException e) {
            throw new DAOConfigurationException("Le fichier properties " + FICHIER_PROPERTIES + " est introuvable.", e);
        } catch (IOException e) {
            throw new DAOConfigurationException("Impossible de charger le fichier properties " + FICHIER_PROPERTIES, e);
        }
        return hmap;
    }

    /**
     * Connexion au driver jdbc
     * 
     * @param hmap
     */
    protected static void connexionDriverJdbc(HashMap<String, String> hmap) {
        // Connexion au driver jdbc
        try {
            Class.forName(hmap.get(PROPERTY_DRIVER));
            log.trace("Connexion au driver jdbc : \"" + hmap.get(PROPERTY_DRIVER) + "\" avec succès");
        } catch (ClassNotFoundException e) {
            throw new DAOConfigurationException("Le driver est introuvable dans le classpath.", e);
        }
    }

    /**
     * Création d'une configuration de pool de connexions via l'objet BoneCPConfig et les différents setters associés.
     * 
     * @param hmap
     *            Contient les données du fichier properties
     * @return BoneCP Création du pool à partir de la configuration, via l'objet BoneCP
     */
    protected static BoneCP connexionBoneCP(HashMap<String, String> hmap) {
        try {
            log.trace("Création d'une configuration de pool de connexions via l'objet BoneCPConfig "
                    + "et les différents setters associés.");
            BoneCPConfig config = new BoneCPConfig();
            /* Mise en place de l'URL, du nom et du mot de passe */
            config.setJdbcUrl(hmap.get(PROPERTY_URL));
            config.setUsername(hmap.get(PROPERTY_NOM_UTILISATEUR));
            config.setPassword(hmap.get(PROPERTY_MOT_DE_PASSE));
            /* Paramétrage de la taille du pool */
            config.setMinConnectionsPerPartition(5);
            config.setMaxConnectionsPerPartition(10);
            config.setPartitionCount(2);
            /*
             * Création du pool à partir de la configuration, via l'objet BoneCP
             */
            return new BoneCP(config);
        } catch (SQLException e) {
            throw new DAOConfigurationException("Erreur de configuration du pool de connexions.", e);
        }
    }
}
