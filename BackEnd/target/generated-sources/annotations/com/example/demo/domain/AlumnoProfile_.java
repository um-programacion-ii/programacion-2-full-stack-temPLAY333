package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(AlumnoProfile.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class AlumnoProfile_ {

	public static final String NOMBRE_ALUMNO = "nombreAlumno";
	public static final String DESCRIPCION_PROYECTO = "descripcionProyecto";
	public static final String ID = "id";
	public static final String USER = "user";

	
	/**
	 * @see com.example.demo.domain.AlumnoProfile#nombreAlumno
	 **/
	public static volatile SingularAttribute<AlumnoProfile, String> nombreAlumno;
	
	/**
	 * @see com.example.demo.domain.AlumnoProfile#descripcionProyecto
	 **/
	public static volatile SingularAttribute<AlumnoProfile, String> descripcionProyecto;
	
	/**
	 * @see com.example.demo.domain.AlumnoProfile#id
	 **/
	public static volatile SingularAttribute<AlumnoProfile, Long> id;
	
	/**
	 * @see com.example.demo.domain.AlumnoProfile
	 **/
	public static volatile EntityType<AlumnoProfile> class_;
	
	/**
	 * @see com.example.demo.domain.AlumnoProfile#user
	 **/
	public static volatile SingularAttribute<AlumnoProfile, User> user;

}

