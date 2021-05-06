package app.coronawarn.quicktest.migration.v001tov002;

import static org.hibernate.jpa.AvailableSettings.PERSISTENCE_UNIT_NAME;

import app.coronawarn.quicktest.dbencryption.DbEncryptionService;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.migration.liquibase.BeanAwareSpringLiquibase;
import app.coronawarn.quicktest.migration.v001tov002.dbencryption.DbEncryptionServiceMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestArchiveRepositoryMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestRepositoryMigrationV001;
import app.coronawarn.quicktest.model.SecurityAuditListenerQuickTest;
import app.coronawarn.quicktest.model.SecurityAuditListenerQuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@RequiredArgsConstructor
public class MigrateDataFromV001toV002 implements CustomTaskChange {

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;
    private DbEncryptionService dbEncryptionService;
    private DbEncryptionServiceMigrationV001 dbEncryptionServiceMigrationV001;
    private QuickTestRepository quickTestRepository;
    private QuickTestRepositoryMigrationV001 quickTestRepositoryMigrationV001;
    private QuickTestArchiveRepository quickTestArchiveRepository;
    private QuickTestArchiveRepositoryMigrationV001 quickTestArchiveRepositoryMigrationV001;
    private ModelMapper modelMapper;

    private SecurityAuditListenerQuickTest securityAuditListenerQuickTest;
    private SecurityAuditListenerQuickTestArchive securityAuditListenerQuickTestArchive;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            dbEncryptionServiceMigrationV001 = BeanAwareSpringLiquibase.getBean(DbEncryptionServiceMigrationV001.class);
            quickTestRepositoryMigrationV001 = BeanAwareSpringLiquibase.getBean(QuickTestRepositoryMigrationV001.class);
            quickTestArchiveRepositoryMigrationV001 =
                BeanAwareSpringLiquibase.getBean(QuickTestArchiveRepositoryMigrationV001.class);

            securityAuditListenerQuickTest = BeanAwareSpringLiquibase.getBean(SecurityAuditListenerQuickTest.class);
            securityAuditListenerQuickTestArchive =
                BeanAwareSpringLiquibase.getBean(SecurityAuditListenerQuickTestArchive.class);
            dbEncryptionService = BeanAwareSpringLiquibase.getBean(DbEncryptionService.class);
            quickTestRepository = BeanAwareSpringLiquibase.getBean(QuickTestRepository.class);
            quickTestArchiveRepository = BeanAwareSpringLiquibase.getBean(QuickTestArchiveRepository.class);


            SingleConnectionDataSource singleConnectionDataSource =
              new SingleConnectionDataSource(((JdbcConnection)database.getConnection()).getUnderlyingConnection(),
                true);
            singleConnectionDataSource.setAutoCommit(true);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(singleConnectionDataSource);
            quickTestRepositoryMigrationV001.findAll().forEach(
                quickTestMigrationV001 -> {
                    quickTestRepository.saveAndFlush(modelMapper.map(quickTestMigrationV001, QuickTest.class));
                    jdbcTemplate.update("update quick_test set version = ? where hashed_guid = ?",
                        quickTestMigrationV001.getVersion(), quickTestMigrationV001.getHashedGuid());
                }
            );

            quickTestArchiveRepositoryMigrationV001.findAll().forEach(
                quickTestArchiveMigrationV001 -> {
                    quickTestArchiveRepository
                        .saveAndFlush(modelMapper.map(quickTestArchiveMigrationV001, QuickTestArchive.class));
                    jdbcTemplate.update("update quick_test_archive set version = ? where hashed_guid = ?",
                        quickTestArchiveMigrationV001.getVersion(), quickTestArchiveMigrationV001.getHashedGuid());
                }
            );
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Migration V001 -> V002 done";
    }

    @Override
    public void setUp() throws SetupException {
        try {
            modelMapper = BeanAwareSpringLiquibase.getBean(ModelMapper.class);
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

}
