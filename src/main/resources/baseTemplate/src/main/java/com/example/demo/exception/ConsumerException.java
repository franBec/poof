package /*group*/./*artifact*/.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class /*Consumer*/Exception extends RuntimeException {
    //feel free to improve here by replacing the String with whatever class that best represents the error you get
    private final String error;
}
