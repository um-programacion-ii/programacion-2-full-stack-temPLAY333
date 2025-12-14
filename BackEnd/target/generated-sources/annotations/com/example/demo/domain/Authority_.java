package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Authority.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class Authority_ {

	public static final String NAME = "name";

	
	/**
	 * @see com.example.demo.domain.Authority#name
	 **/
	public static volatile SingularAttribute<Authority, String> name;
	
	/**
	 * @see com.example.demo.domain.Authority
	 **/
	public static volatile EntityType<Authority> class_;

}

