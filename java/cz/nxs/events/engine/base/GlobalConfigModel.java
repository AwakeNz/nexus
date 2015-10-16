/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

public class GlobalConfigModel {
    private String category;
    private String key;
    private String value;
    private String description;
    private int inputType;

    public GlobalConfigModel(String category, String key, String value, String desc, int input) {
        this.category = category;
        this.key = key;
        this.value = value;
        this.description = desc;
        this.inputType = input;
    }

    public String getCategory() {
        return this.category;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return this.description;
    }

    public int getInputType() {
        return this.inputType;
    }
}

