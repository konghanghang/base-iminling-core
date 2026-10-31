package com.iminling.core.config.argument;

import tools.jackson.databind.JsonNode;

public class RequestDataWrapper {
    private JsonNode params;
    private boolean hasParams;
    private boolean canRead;

    public RequestDataWrapper(boolean canRead) {
        this.canRead = canRead;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public void setHasParams(boolean hasParams) {
        this.hasParams = hasParams;
    }

    public boolean getHasParams() {
        return hasParams;
    }

    public boolean getCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public void parseJsonNode(JsonNode params) {
        this.params = params;
        hasParams = params != null && !params.isNull() && !params.isMissingNode();
    }

    @Override
    public String toString() {
        return String.valueOf(params);
    }
}
