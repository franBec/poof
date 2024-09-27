package /*group*/./*artifact*/.errordecoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import /*group*/./*artifact*/.exception./*Consumer*/Exception;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class /*Consumer*/ErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        try (InputStream body = response.body().asInputStream()) {
            return new /*Consumer*/Exception(new String(body.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new Default().decode(s, response);
        }
    }
}