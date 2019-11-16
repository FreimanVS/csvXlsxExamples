package com.scvtoxlsx;

import java.util.Objects;

public class Data {
    private String id;
    private String  value1;
    private String  value2;
    private String  value3;

    public Data() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(id, data.id) &&
                Objects.equals(value1, data.value1) &&
                Objects.equals(value2, data.value2) &&
                Objects.equals(value3, data.value3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value1, value2, value3);
    }

    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", value1='" + value1 + '\'' +
                ", value2='" + value2 + '\'' +
                ", value3='" + value3 + '\'' +
                '}';
    }
}
