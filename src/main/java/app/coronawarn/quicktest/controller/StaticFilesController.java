package app.coronawarn.quicktest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving static files.
 * This workaround is needed to enable React Router.
 */
@Controller
public class StaticFilesController {

    @GetMapping(value = "/**/{[path:[^.]*}")
    public String redirect() {
        return "forward:/";
    }

}
