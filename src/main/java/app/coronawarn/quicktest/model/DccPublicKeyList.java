package app.coronawarn.quicktest.model;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class DccPublicKeyList {
    @NotNull
    @NotEmpty
    private List<@Valid DccPublicKey> publicKeys;

    public DccPublicKeyList setPublicKeys(List<DccPublicKey> publicKeys) {
        this.publicKeys = publicKeys;
        return this;
    }
}
