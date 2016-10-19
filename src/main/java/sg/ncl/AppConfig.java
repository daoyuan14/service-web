package sg.ncl;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author by Te Ye
 */
@Configuration("sg.ncl.AppConfig")
public class AppConfig {
    @Autowired
    private ErrorAttributes errorAttributes;

    @Bean
    public AppErrorController appErrorController(){return new AppErrorController(errorAttributes);}

    // thymleaf-layout-dialect
    // for fragments
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
