package app.coronawarn.quicktest.migration.v001tov002;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.migration.liquibase.BeanAwareSpringLiquibase;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestArchiveRepositoryMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestRepositoryMigrationV001;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.time.LocalDateTime;
import java.util.List;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EntityScan("app.coronawarn.quicktest")
public class MigrateDataFromV001toV002 implements CustomTaskChange {

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;
    private QuickTestRepository quickTestRepository;
    private QuickTestRepositoryMigrationV001 quickTestRepositoryMigrationV001;
    private QuickTestArchiveRepository quickTestArchiveRepository;
    private QuickTestArchiveRepositoryMigrationV001 quickTestArchiveRepositoryMigrationV001;
    private ModelMapper modelMapper;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            quickTestRepositoryMigrationV001 = BeanAwareSpringLiquibase.getBean(QuickTestRepositoryMigrationV001.class);
            quickTestArchiveRepository = BeanAwareSpringLiquibase.getBean(QuickTestArchiveRepository.class);
            quickTestArchiveRepositoryMigrationV001 =
                BeanAwareSpringLiquibase.getBean(QuickTestArchiveRepositoryMigrationV001.class);
            quickTestRepository = BeanAwareSpringLiquibase.getBean(QuickTestRepository.class);


            LocalDateTime time = LocalDateTime.now();
            quickTestRepositoryMigrationV001.findAllByCreatedAtBefore(time).forEach(
                quickTestMigrationV002 -> {
                    quickTestRepository.save(modelMapper.map(quickTestMigrationV002, QuickTest.class));
                }
            );
            List<QuickTest> afterRunQuickTest = quickTestRepositoryMigrationV001.findAllByCreatedAtAfter(time);
            while (afterRunQuickTest.size() > 0) {
                time = LocalDateTime.now();
                afterRunQuickTest.forEach(
                    quickTestMigrationV002 -> {
                        quickTestRepository.save(modelMapper.map(quickTestMigrationV002, QuickTest.class));
                    }
                );
                afterRunQuickTest = quickTestRepositoryMigrationV001.findAllByCreatedAtAfter(time);
            }


            time = LocalDateTime.now();
            quickTestArchiveRepositoryMigrationV001.findAllByCreatedAtBefore(time).forEach(
                quickTestArchiveMigrationV002 -> {
                    quickTestArchiveRepository
                        .save(modelMapper.map(quickTestArchiveMigrationV002, QuickTestArchive.class));
                }
            );
            List<QuickTestArchive> afterRunQuickTestArchive =
                quickTestArchiveRepositoryMigrationV001.findAllByCreatedAtAfter(time);
            while (afterRunQuickTestArchive.size() > 0) {
                time = LocalDateTime.now();
                afterRunQuickTestArchive.forEach(
                    quickTestMigrationV002 -> {
                        quickTestRepository.save(modelMapper.map(quickTestMigrationV002, QuickTest.class));
                    }
                );
                afterRunQuickTestArchive = quickTestArchiveRepositoryMigrationV001.findAllByCreatedAtAfter(time);
            }
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
