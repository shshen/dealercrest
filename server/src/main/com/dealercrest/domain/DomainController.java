package com.dealercrest.domain;

import com.dealercrest.http.HttpResult;
import com.dealercrest.http.Multipart;
import com.dealercrest.http.QueryRequest;
import com.dealercrest.rest.Route;

public class DomainController {

    // Dealer connects their custom domain
    @Route(path = "/api/dealer/domain", method = "POST")
    public HttpResult connectDomain(Multipart multipart) {
        return null;
    }

    // Dealer checks their domain + cert status
    @Route(path = "/api/dealer/domain/status", method = "GET")
    public HttpResult domainStatus(QueryRequest request) {
        return null;
    }
    
}
