package com.bol.katalog;

import feign.Response;
import feign.codec.ErrorDecoder;

class KatalogErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) throw new NotFoundException();
        throw new ServerException("There was an error contacting the Katalog server, error code: " + response.status());
    }
}
