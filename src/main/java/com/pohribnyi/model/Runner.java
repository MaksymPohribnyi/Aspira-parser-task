package com.pohribnyi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Runner(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("price") double price
) {
}
