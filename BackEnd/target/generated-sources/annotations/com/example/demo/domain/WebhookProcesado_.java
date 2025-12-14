package com.example.demo.domain;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.Instant;

@StaticMetamodel(WebhookProcesado.class)
@Generated("org.hibernate.processor.HibernateProcessor")
public abstract class WebhookProcesado_ {

	public static final String OFFSET_NUM = "offsetNum";
	public static final String IDEMPOTENCY_KEY = "idempotencyKey";
	public static final String PROCESSED_AT = "processedAt";
	public static final String TOPIC = "topic";
	public static final String ID = "id";
	public static final String PARTITION_NUM = "partitionNum";

	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#offsetNum
	 **/
	public static volatile SingularAttribute<WebhookProcesado, Long> offsetNum;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#idempotencyKey
	 **/
	public static volatile SingularAttribute<WebhookProcesado, String> idempotencyKey;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#processedAt
	 **/
	public static volatile SingularAttribute<WebhookProcesado, Instant> processedAt;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#topic
	 **/
	public static volatile SingularAttribute<WebhookProcesado, String> topic;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#id
	 **/
	public static volatile SingularAttribute<WebhookProcesado, Long> id;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado
	 **/
	public static volatile EntityType<WebhookProcesado> class_;
	
	/**
	 * @see com.example.demo.domain.WebhookProcesado#partitionNum
	 **/
	public static volatile SingularAttribute<WebhookProcesado, Integer> partitionNum;

}

