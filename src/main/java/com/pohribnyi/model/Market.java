package com.pohribnyi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Market(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("runners") List<Runner> runners
) {
}
