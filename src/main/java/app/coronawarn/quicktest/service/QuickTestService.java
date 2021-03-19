package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Test;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuickTestService {

  @NonNull
  private final QuickTestRepository quickTestRepository;

  @NonNull
  private final TestResultService testResultService;

  public void saveQuickTest(QuickTest quickTest){}
  public void updateQuickTest(QuickTest quickTest){}
  public QuickTest updateQuickTest(String uuid, QuickTest quickTest){
    return null;
  }
  public TestResult getTestResult(String uuid){
    return null;
  }



}
