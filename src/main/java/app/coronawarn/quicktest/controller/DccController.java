package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.service.DccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/dcc")
@RequiredArgsConstructor
public class DccController {
    private final DccService dccService;


}
