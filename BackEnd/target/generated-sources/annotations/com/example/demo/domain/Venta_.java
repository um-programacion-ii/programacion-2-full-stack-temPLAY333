package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.Instant;

@StaticMetamodel(Venta.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Venta_ {

	public static final String DESCRIPCION = "descripcion";
	public static final String EVENTO = "evento";
	public static final String RESULTADO = "resultado";
	public static final String VENTA_ID = "ventaId";
	public static final String USUARIO = "usuario";
	public static final String ID = "id";
	public static final String PRECIO_VENTA = "precioVenta";
	public static final String FECHA_VENTA = "fechaVenta";

	
	/**
	 * @see com.example.demo.domain.Venta#descripcion
	 **/
	public static volatile SingularAttribute<Venta, String> descripcion;
	
	/**
	 * @see com.example.demo.domain.Venta#evento
	 **/
	public static volatile SingularAttribute<Venta, Evento> evento;
	
	/**
	 * @see com.example.demo.domain.Venta#resultado
	 **/
	public static volatile SingularAttribute<Venta, Boolean> resultado;
	
	/**
	 * @see com.example.demo.domain.Venta#ventaId
	 **/
	public static volatile SingularAttribute<Venta, Long> ventaId;
	
	/**
	 * @see com.example.demo.domain.Venta#usuario
	 **/
	public static volatile SingularAttribute<Venta, User> usuario;
	
	/**
	 * @see com.example.demo.domain.Venta#id
	 **/
	public static volatile SingularAttribute<Venta, Long> id;
	
	/**
	 * @see com.example.demo.domain.Venta#precioVenta
	 **/
	public static volatile SingularAttribute<Venta, BigDecimal> precioVenta;
	
	/**
	 * @see com.example.demo.domain.Venta
	 **/
	public static volatile EntityType<Venta> class_;
	
	/**
	 * @see com.example.demo.domain.Venta#fechaVenta
	 **/
	public static volatile SingularAttribute<Venta, Instant> fechaVenta;

}

