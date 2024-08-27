package su.lasteris.dto;

import java.util.Collections;
import java.util.List;

public class ErrorResponse {

    private List<String> errors;

    public List<String> getErrors() {
        if (errors == null)
            return Collections.emptyList();

        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return String.join(";", getErrors());
    }
}
