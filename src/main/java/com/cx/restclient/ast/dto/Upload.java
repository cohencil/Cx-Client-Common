package com.cx.restclient.ast.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Upload {
    private String URL = null;
    private List<Error> errors = null;

    public Upload URL(String URL) {
        this.URL = URL;
        return this;
    }

    /**
     * Get URL
     * @return URL
     **/
    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Upload errors(List<Error> errors) {
        this.errors = errors;
        return this;
    }

    public Upload addErrorsItem(Error errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList<Error>();
        }
        this.errors.add(errorsItem);
        return this;
    }

    /**
     * Get errors
     * @return errors
     **/
    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Upload upload = (Upload) o;
        return Objects.equals(this.URL, upload.URL) &&
                Objects.equals(this.errors, upload.errors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Upload {\n");

        sb.append("    URL: ").append(toIndentedString(URL)).append("\n");
        sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
