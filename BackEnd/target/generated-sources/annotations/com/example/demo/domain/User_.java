package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.Instant;

@StaticMetamodel(User.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class User_ extends com.example.demo.domain.AbstractAuditingEntity_ {

	public static final String LAST_NAME = "lastName";
	public static final String RESET_DATE = "resetDate";
	public static final String LOGIN = "login";
	public static final String ACTIVATION_KEY = "activationKey";
	public static final String RESET_KEY = "resetKey";
	public static final String AUTHORITIES = "authorities";
	public static final String FIRST_NAME = "firstName";
	public static final String PASSWORD = "password";
	public static final String LANG_KEY = "langKey";
	public static final String IMAGE_URL = "imageUrl";
	public static final String ID = "id";
	public static final String EMAIL = "email";
	public static final String ACTIVATED = "activated";

	
	/**
	 * @see com.example.demo.domain.User#lastName
	 **/
	public static volatile SingularAttribute<User, String> lastName;
	
	/**
	 * @see com.example.demo.domain.User#resetDate
	 **/
	public static volatile SingularAttribute<User, Instant> resetDate;
	
	/**
	 * @see com.example.demo.domain.User#login
	 **/
	public static volatile SingularAttribute<User, String> login;
	
	/**
	 * @see com.example.demo.domain.User#activationKey
	 **/
	public static volatile SingularAttribute<User, String> activationKey;
	
	/**
	 * @see com.example.demo.domain.User#resetKey
	 **/
	public static volatile SingularAttribute<User, String> resetKey;
	
	/**
	 * @see com.example.demo.domain.User#authorities
	 **/
	public static volatile SetAttribute<User, Authority> authorities;
	
	/**
	 * @see com.example.demo.domain.User#firstName
	 **/
	public static volatile SingularAttribute<User, String> firstName;
	
	/**
	 * @see com.example.demo.domain.User#password
	 **/
	public static volatile SingularAttribute<User, String> password;
	
	/**
	 * @see com.example.demo.domain.User#langKey
	 **/
	public static volatile SingularAttribute<User, String> langKey;
	
	/**
	 * @see com.example.demo.domain.User#imageUrl
	 **/
	public static volatile SingularAttribute<User, String> imageUrl;
	
	/**
	 * @see com.example.demo.domain.User#id
	 **/
	public static volatile SingularAttribute<User, Long> id;
	
	/**
	 * @see com.example.demo.domain.User
	 **/
	public static volatile EntityType<User> class_;
	
	/**
	 * @see com.example.demo.domain.User#email
	 **/
	public static volatile SingularAttribute<User, String> email;
	
	/**
	 * @see com.example.demo.domain.User#activated
	 **/
	public static volatile SingularAttribute<User, Boolean> activated;

}

