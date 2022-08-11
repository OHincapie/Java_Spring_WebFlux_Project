package com.reactor.reactorWebFlux.reactorWebFlux.controlllers;

import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Producto;
import com.reactor.reactorWebFlux.reactorWebFlux.models.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/productos")
public class ProductController {

    @Autowired
    private ProductoService productoService;

    @Value("${config.uploads.path}")
    private String path;


    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }
        producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));
        return file.transferTo(new File(path + producto.getFoto())).then(productoService.save(producto))
                .map(producto1 ->
                        ResponseEntity.created(URI.create("/api/productos/".concat(producto1.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(producto1));
    }

    @PostMapping("/upload/{id}")
    private Mono<ResponseEntity<Producto>> uploadFile(@PathVariable String id, @RequestPart FilePart file) {
        return productoService.findById(id).flatMap(producto -> {
                    producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", ""));
                    return file.transferTo(new File(path + producto.getFoto())).then(productoService.save(producto));
                }).map(producto -> ResponseEntity.ok(producto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> listar() {
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productoService.findAll())
        );
    }

    @GetMapping(value = "/{id}")
    public Mono<ResponseEntity<Producto>> findById(@PathVariable String id) {
        return productoService.findById(id).map(producto -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(producto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
        Map<String, Object> resultado = new HashMap<String, Object>();
        return monoProducto.flatMap(producto -> {
                    if (producto.getCreateAt() == null) {
                        producto.setCreateAt(new Date());
                    }
                    return productoService.save(producto).map(producto1 -> {
                        resultado.put("producto", producto1);
                        resultado.put("mensaje", "Producto creado con exito");
                        resultado.put("timestamp", new Date());
                        return ResponseEntity.created(URI.create("/api/productos/".concat(producto1.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(resultado);
                    });

                })
                .onErrorResume(throwable -> Mono.just(throwable).cast(WebExchangeBindException.class)
                        .flatMap(e -> Mono.just(e.getFieldErrors()))
                        .flatMapMany(Flux::fromIterable)
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .log()
                        .collectList()
                        .flatMap(list -> {
                            resultado.put("errors", list);
                            resultado.put("timestamp", new Date());
                            resultado.put("status", HttpStatus.BAD_REQUEST.value());
                            return Mono.just(ResponseEntity.badRequest().body(resultado));
                        }));

    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<Producto>> actualizar(@RequestBody Producto producto, @PathVariable String id) {
        return productoService.findById(id).flatMap(producto1 -> {
                    producto1.setNombre(producto.getNombre());
                    producto1.setPrecio(producto.getPrecio());
                    producto1.setCategoria(producto.getCategoria());
                    return productoService.save(producto1);
                }).map(producto1 -> ResponseEntity.created(URI.create("/api/productos/".concat(producto1.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(producto1))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return productoService.findById(id).flatMap(producto ->
                productoService.delete(producto).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
        ).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}
