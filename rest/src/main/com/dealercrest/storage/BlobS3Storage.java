package com.dealercrest.storage;

public class BlobS3Storage extends Storage {

    @Override
    public void put(String path, byte[] data) {
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public String getUrl(String path) {
        throw new UnsupportedOperationException("Unimplemented method 'getUrl'");
    }
    
}
