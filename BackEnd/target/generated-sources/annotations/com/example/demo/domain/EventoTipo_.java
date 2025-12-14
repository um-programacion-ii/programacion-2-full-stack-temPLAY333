package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EventoTipo.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class EventoTipo_ {

	public static final String DESCRIPCION = "descripcion";
	public static final String EVENTO = "evento";
	public static final String ID = "id";
	public static final String NOMBRE = "nombre";

	
	/**
	 * @see com.example.demo.domain.EventoTipo#descripcion
	 **/
	public static volatile SingularAttribute<EventoTipo, String> descripcion;
	
	/**
	 * @see com.example.demo.domain.EventoTipo#evento
	 **/
	public static volatile SingularAttribute<EventoTipo, Evento> evento;
	
	/**
	 * @see com.example.demo.domain.EventoTipo#id
	 **/
	public static volatile SingularAttribute<EventoTipo, Long> id;
	
	/**
	 * @see com.example.demo.domain.EventoTipo
	 **/
	public static volatile EntityType<EventoTipo> class_;
	
	/**
	 * @see com.example.demo.domain.EventoTipo#nombre
	 **/
	public static volatile SingularAttribute<EventoTipo, String> nombre;

}

