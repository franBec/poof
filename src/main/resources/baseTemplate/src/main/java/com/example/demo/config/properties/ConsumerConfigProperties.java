package /*group*/./*artifact*/.config.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "/*consumerName*/")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class /*ConsumerName*/ConfigProperties {
    String baseUrl;
}
