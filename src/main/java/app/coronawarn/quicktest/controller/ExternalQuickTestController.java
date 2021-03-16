package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.TestResultService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/version/v1")
@Validated
@Profile("external")
public class ExternalQuickTestController {
  public static final String QUICK_TEST_ROUTE = "/quickTest";
  public static final String VALIDATION_ROUTE = "/validate";

  @NonNull
  private final QuickTestService quickTestService;

  @NonNull
  private final TestResultService testResultService;



}
