package com.exploringspatial.tcx;

/**
 * Created by IntelliJ IDEA.
 * User: mitchellst
 * Date: 11/15/12
 * Time: 3:55 PM
 *
 */
public class ObjectFactorySchema<ObjectFactory extends Object> {

	private final Class<ObjectFactory> objectFactoryClass;
	private final String xsdLocation;

	public ObjectFactorySchema(Class<ObjectFactory> objectFactoryClass, String xsdLocation) {
		this.objectFactoryClass = objectFactoryClass;
		this.xsdLocation = xsdLocation;
	}

	public Class<ObjectFactory> getObjectFactoryClass() {
		return objectFactoryClass;
	}

	public String getXsdLocation() {
		return xsdLocation;
	}
}
