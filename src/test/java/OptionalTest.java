import org.junit.jupiter.api.Test;

import java.util.Optional;

public class OptionalTest {

    @Test
    public void howOptionalWorks() {
        String name = "dinusha";
        Optional<String> optName = Optional.ofNullable(name);
        System.out.println(optName.orElse("IJSE").toUpperCase());
    }
}
