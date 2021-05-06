package app.coronawarn.quicktest.migration.liquibase;

import liquibase.integration.spring.SpringLiquibase;
import lombok.ToString;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;

@ToString
public class BeanAwareSpringLiquibase extends SpringLiquibase {
    private static ResourceLoader applicationContext;

    public BeanAwareSpringLiquibase() {
    }

    /**
     * Get Bean for migration in liquibase.
     *
     * @param beanClass class of the bean
     * @return Bean
     * @throws Exception if bean not exist
     */
    public static final <T> T getBean(Class<T> beanClass) throws Exception {
        if (ApplicationContext.class.isInstance(applicationContext)) {
            return ((ApplicationContext) applicationContext).getBean(beanClass);
        } else {
            throw new Exception("Resource loader is not an instance of ApplicationContext");
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        applicationContext = resourceLoader;
    }
}
