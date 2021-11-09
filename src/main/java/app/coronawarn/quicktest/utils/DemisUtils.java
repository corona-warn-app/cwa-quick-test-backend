package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.QuickTest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class DemisUtils {

    private static final String URN = "urn:uuid:";
    public static final String COUNTRY_DE = "20422";
    public static final String BSNR_SYSTEM = "https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR";

    private static final SimpleDateFormat birthdayFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Create a new UUID String.
     *
     * @return UUID as String
     */
    public static String createId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create a reference link to a given resource.
     * @param resource the target resource
     * @param <T> the target resource type
     * @return the reference
     */
    public static <T extends Resource> Reference createResourceReference(T resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource for Reference is null");
        } else {
            return new Reference(URN + resource.getId());
        }
    }

    /**
     * Create a resources Meta information.
     * @param profile the profile to use
     * @return the Meta
     */
    public static Meta createMetaInformation(String profile) {
        return new Meta().addProfile(profile);
    }

    /**
     * Create an address from poc details from Keycloak.
     * @param pocInformation the details
     * @return the address
     */
    public static Optional<Address> createAddress(List<String> pocInformation) {
        Pattern zipLine = Pattern.compile("^[1-9]{1}\\d{4} \\w+");
        final Optional<String> zipAndStreet = pocInformation.stream()
          .map(String::trim)
          .filter(zipLine.asPredicate())
          .findFirst();

        if (zipAndStreet.isPresent()) {
            Address result = new Address();
            result.setCity(zipAndStreet.get().substring(5).trim());
            result.setPostalCode(zipAndStreet.get().substring(0, 5));
            result.addLine(pocInformation.get(1));
            result.setCountry(COUNTRY_DE);

            return Optional.of(result);
        }
        return Optional.empty();

    }

    /**
     * Create an address from quicktest.
     * @param quickTest the quicktest
     * @return the address
     */
    public static Address createAddress(QuickTest quickTest) {
        Address result = new Address();
        result.setCity(quickTest.getCity());
        result.setPostalCode(quickTest.getZipCode());
        result.addLine(quickTest.getStreet());
        result.setCountry(COUNTRY_DE);
        return result;
    }

    /**
     * Parse quicktest birthday format.
     * @param birthday the string
     * @return the date
     */
    public static Date parseBirthday(String birthday) {
        Date result = new Date();
        try {
            result = birthdayFormat.parse(birthday);
        } catch (ParseException e) {
            log.error("Can not parse birthday: {}", birthday);
        }
        return result;
    }

    /**
     * Get Parameter by name.
     * @param params the parameters
     * @param name the name of the parameter to retrieve
     * @return the Parameter
     */
    public static Parameters.ParametersParameterComponent retrieveParameter(Parameters params, String name) {
        Iterator<Parameters.ParametersParameterComponent> it = params.getParameter().iterator();
        Parameters.ParametersParameterComponent match = null;

        while (it.hasNext()) {
            Parameters.ParametersParameterComponent p = it.next();
            if (p.getName().equals(name)) {
                match = p;
                break;
            }
        }

        if (match == null) {
            log.error("did not find a parameter with name {}", name);
            return null;
        } else if (match.isEmpty()) {
            log.error("ParameterComponent {} with empty value, returning null", name);
            return null;
        } else {
            return match;
        }
    }
}
