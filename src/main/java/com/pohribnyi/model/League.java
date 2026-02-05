package com.pohribnyi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record League(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("top") boolean top,
        @JsonProperty("prematch") int prematch
) {
}
