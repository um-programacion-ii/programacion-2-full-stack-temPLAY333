package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import java.time.Instant;

@StaticMetamodel(Evento.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Evento_ {

	public static final String DESCRIPCION = "descripcion";
	public static final String DIRECCION = "direccion";
	public static final String TITULO = "titulo";
	public static final String IMAGEN = "imagen";
	public static final String RESUMEN = "resumen";
	public static final String COLUMN_ASIENTOS = "columnAsientos";
	public static final String PRECIO_ENTRADA = "precioEntrada";
	public static final String FECHA = "fecha";
	public static final String EVENTO_TIPO = "eventoTipo";
	public static final String FILA_ASIENTOS = "filaAsientos";
	public static final String ID = "id";
	public static final String INTEGRANTES = "integrantes";

	
	/**
	 * @see com.example.demo.domain.Evento#descripcion
	 **/
	public static volatile SingularAttribute<Evento, String> descripcion;
	
	/**
	 * @see com.example.demo.domain.Evento#direccion
	 **/
	public static volatile SingularAttribute<Evento, String> direccion;
	
	/**
	 * @see com.example.demo.domain.Evento#titulo
	 **/
	public static volatile SingularAttribute<Evento, String> titulo;
	
	/**
	 * @see com.example.demo.domain.Evento#imagen
	 **/
	public static volatile SingularAttribute<Evento, String> imagen;
	
	/**
	 * @see com.example.demo.domain.Evento#resumen
	 **/
	public static volatile SingularAttribute<Evento, String> resumen;
	
	/**
	 * @see com.example.demo.domain.Evento#columnAsientos
	 **/
	public static volatile SingularAttribute<Evento, Integer> columnAsientos;
	
	/**
	 * @see com.example.demo.domain.Evento#precioEntrada
	 **/
	public static volatile SingularAttribute<Evento, BigDecimal> precioEntrada;
	
	/**
	 * @see com.example.demo.domain.Evento#fecha
	 **/
	public static volatile SingularAttribute<Evento, Instant> fecha;
	
	/**
	 * @see com.example.demo.domain.Evento#eventoTipo
	 **/
	public static volatile SingularAttribute<Evento, EventoTipo> eventoTipo;
	
	/**
	 * @see com.example.demo.domain.Evento#filaAsientos
	 **/
	public static volatile SingularAttribute<Evento, Integer> filaAsientos;
	
	/**
	 * @see com.example.demo.domain.Evento#id
	 **/
	public static volatile SingularAttribute<Evento, Long> id;
	
	/**
	 * @see com.example.demo.domain.Evento
	 **/
	public static volatile EntityType<Evento> class_;
	
	/**
	 * @see com.example.demo.domain.Evento#integrantes
	 **/
	public static volatile SetAttribute<Evento, Integrante> integrantes;

}

