package com.reactor.reactorWebFlux.reactorWebFlux;

import com.reactor.reactorWebFlux.reactorWebFlux.models.dao.ProductoDAO;
import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Categoria;
import com.reactor.reactorWebFlux.reactorWebFlux.models.documents.Producto;
import com.reactor.reactorWebFlux.reactorWebFlux.models.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class ReactorWebFluxApplication implements CommandLineRunner {

	@Autowired
	private ProductoService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(ReactorWebFluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ReactorWebFluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
/*
		reactiveMongoTemplate.dropCollection("productos").subscribe();
		Flux.just(new Producto("TV", 500.0),
				new Producto("Iphone", 1000.0),
				new Producto("Tennis", 50.0),
				new Producto("PC", 2000.0),
				new Producto("Dress", 30.0)
		)
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return dao.save(producto);
				})
				.subscribe(producto -> LOGGER.info("Insert: " + producto.getId() + " " + producto.getNombre()));*/

		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion, muebles)
				.flatMap(service::saveCategoria)
				.doOnNext(c ->{
					log.info("Categoria creada: " + c.getNombre() + ", Id: " + c.getId());
				}).thenMany(
						Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
										new Producto("Sony Camara HD Digital", 177.89, electronico),
										new Producto("Apple iPod", 46.89, electronico),
										new Producto("Sony Notebook", 846.89, computacion),
										new Producto("Hewlett Packard Multifuncional", 200.89, computacion),
										new Producto("Bianchi Bicicleta", 70.89, deporte),
										new Producto("HP Notebook Omen 17", 2500.89, computacion),
										new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
										new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico)
								)
								.flatMap(producto -> {
									producto.setCreateAt(new Date());
									return service.save(producto);
								})
				)
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}
}
