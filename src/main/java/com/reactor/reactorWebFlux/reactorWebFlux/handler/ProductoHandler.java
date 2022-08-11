package com.reactor.reactorWebFlux.reactorWebFlux.handler;

import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Producto;
import com.reactor.reactorWebFlux.reactorWebFlux.models.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService productoService;

    public Mono<ServerResponse> listar(ServerRequest request){
        return ServerResponse.ok().body(productoService.findAll(), Producto.class);
    }
}
