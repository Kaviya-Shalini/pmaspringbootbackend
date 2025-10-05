package com.example.personalmemory.model;

public class DecryptedFile {
    private final byte[] data;
    private final String filename;

    public DecryptedFile(byte[] data, String filename) {
        this.data = data;
        this.filename = filename;
    }

    public byte[] getData() { return data; }
    public String getFilename() { return filename; }
}