package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Integrante.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Integrante_ {

	public static final String APELLIDO = "apellido";
	public static final String IDENTIFICACION = "identificacion";
	public static final String EVENTOS = "eventos";
	public static final String ID = "id";
	public static final String NOMBRE = "nombre";

	
	/**
	 * @see com.example.demo.domain.Integrante#apellido
	 **/
	public static volatile SingularAttribute<Integrante, String> apellido;
	
	/**
	 * @see com.example.demo.domain.Integrante#identificacion
	 **/
	public static volatile SingularAttribute<Integrante, String> identificacion;
	
	/**
	 * @see com.example.demo.domain.Integrante#eventos
	 **/
	public static volatile SetAttribute<Integrante, Evento> eventos;
	
	/**
	 * @see com.example.demo.domain.Integrante#id
	 **/
	public static volatile SingularAttribute<Integrante, Long> id;
	
	/**
	 * @see com.example.demo.domain.Integrante
	 **/
	public static volatile EntityType<Integrante> class_;
	
	/**
	 * @see com.example.demo.domain.Integrante#nombre
	 **/
	public static volatile SingularAttribute<Integrante, String> nombre;

}

