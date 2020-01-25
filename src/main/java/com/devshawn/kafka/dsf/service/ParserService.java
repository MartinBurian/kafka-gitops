package com.devshawn.kafka.dsf.service;

import com.devshawn.kafka.dsf.domain.state.DesiredState;
import com.devshawn.kafka.dsf.exception.ValidationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ParserService {

    private static Logger log = LoggerFactory.getLogger(ParserService.class);

    private final ObjectMapper objectMapper;

    private final File file;

    public ParserService(File file) {
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        this.file = file;
    }

    public DesiredState parseStateFile() {
        // TODO: Read from file
        log.info("Parsing desired state file...");

        try {
            return objectMapper.readValue(file, DesiredState.class);
        } catch (ValueInstantiationException ex) {
            List<String> fields = getYamlFields(ex);
            String joinedFields = String.join(" -> ", fields);
            throw new ValidationException(String.format("%s in state file definition: %s", ex.getCause().getMessage(), joinedFields));
        } catch (UnrecognizedPropertyException ex) {
            List<String> fields = getYamlFields(ex);
            String joinedFields = String.join(" -> ", fields.subList(0, fields.size() - 1));
            throw new ValidationException(String.format("Unrecognized field: [%s] in state file definition: %s", ex.getPropertyName(), joinedFields));
        } catch (InvalidFormatException ex) {
            List<String> fields = getYamlFields(ex);
            String value = ex.getValue().toString();
            String propertyName = fields.get(fields.size() - 1);
            String joinedFields = String.join(" -> ", fields.subList(0, fields.size() - 1));
            throw new ValidationException(String.format("Value '%s' is not a valid format for: [%s] in state file definition: %s", value, propertyName, joinedFields));
        } catch (JsonMappingException ex) {
            List<String> fields = getYamlFields(ex);
            String joinedFields = String.join(" -> ", fields);
            throw new ValidationException(String.format("%s in state file definition: %s", ex.getCause().getMessage().split("\n")[0], joinedFields));
        } catch (IOException ex) {
            throw new ValidationException(String.format("Invalid state file. Unknown error: %s", ex.getMessage()));
        }
    }

    private List<String> getYamlFields(JsonMappingException ex) {
        return ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.toList());
    }
}
