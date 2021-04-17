package app.coronawarn.quicktest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class SinglePageApplicationConfiguration implements WebMvcConfigurer {
   @Override
    public void addViewControllers(ViewControllerRegistry registry){
       registry.addViewController("/api/**");
       registry.addViewController("/**").setViewName("forward:/index.html");
   }
}
