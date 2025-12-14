package com.example.demo.domain;

import com.example.demo.domain.enumeration.Estado;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Asiento.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Asiento_ {

	public static final String COLUMNA = "columna";
	public static final String ESTADO = "estado";
	public static final String VENTA = "venta";
	public static final String PERSONA = "persona";
	public static final String FILA = "fila";
	public static final String ID = "id";

	
	/**
	 * @see com.example.demo.domain.Asiento#columna
	 **/
	public static volatile SingularAttribute<Asiento, Integer> columna;
	
	/**
	 * @see com.example.demo.domain.Asiento#estado
	 **/
	public static volatile SingularAttribute<Asiento, Estado> estado;
	
	/**
	 * @see com.example.demo.domain.Asiento#venta
	 **/
	public static volatile SingularAttribute<Asiento, Venta> venta;
	
	/**
	 * @see com.example.demo.domain.Asiento#persona
	 **/
	public static volatile SingularAttribute<Asiento, String> persona;
	
	/**
	 * @see com.example.demo.domain.Asiento#fila
	 **/
	public static volatile SingularAttribute<Asiento, Integer> fila;
	
	/**
	 * @see com.example.demo.domain.Asiento#id
	 **/
	public static volatile SingularAttribute<Asiento, Long> id;
	
	/**
	 * @see com.example.demo.domain.Asiento
	 **/
	public static volatile EntityType<Asiento> class_;

}

