package com.reactor.reactorWebFlux.reactorWebFlux.models.dao;

import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {
}
