package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.repository.QuickTestRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuickTestService {

  private final QuickTestRepository quickTestRepository;

  public QuickTestService(@NonNull QuickTestRepository quickTestRepository){
    this.quickTestRepository = quickTestRepository;
  }

}
