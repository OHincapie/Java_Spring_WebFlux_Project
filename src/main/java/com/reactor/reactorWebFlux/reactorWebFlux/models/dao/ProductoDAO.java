package com.reactor.reactorWebFlux.reactorWebFlux.models.dao;

import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDAO extends ReactiveMongoRepository<Producto, String> {
}
