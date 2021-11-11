package app.coronawarn.quicktest.model.demis;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DemisToken {

    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private Instant refreshTokenExpiresAt;

    public boolean expiresSoon() {
        return expiresAt.minus(1, ChronoUnit.MINUTES).isBefore(Instant.now());
    }

    public boolean isRefreshable() {
        return refreshTokenExpiresAt.isAfter(Instant.now());
    }
}
