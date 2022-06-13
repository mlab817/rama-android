package com.example.rama;

/**
 * Created by Eduard Pogi My ABs
 */

public class Name {
    private String name;
    private String detail;
    private int status;

    public Name(String name, String detail, int status) {
        this.name = name;
        this.detail = detail;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }
}
