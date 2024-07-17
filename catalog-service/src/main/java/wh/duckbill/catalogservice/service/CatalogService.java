package wh.duckbill.catalogservice.service;

import wh.duckbill.catalogservice.jpa.CatalogEntity;

public interface CatalogService {
    Iterable<CatalogEntity> getAllCatalogs();
}
