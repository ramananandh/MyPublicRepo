//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.07 at 12:06:52 PM GMT+05:30 
//


package com.ebay.marketplace.search.v1.services;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompositeOperator.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CompositeOperator">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AND"/>
 *     &lt;enumeration value="OR"/>
 *     &lt;enumeration value="NOT"/>
 *     &lt;enumeration value="IF"/>
 *     &lt;enumeration value="SIMILAR"/>
 *     &lt;enumeration value="SET_FIND"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CompositeOperator")
@XmlEnum
public enum CompositeOperator {


    /**
     * and
     * 
     */
    AND,

    /**
     * or
     * 
     */
    OR,

    /**
     * not
     * 
     */
    NOT,

    /**
     * if
     * 
     */
    IF,

    /**
     * similar(aspect expression1, aspect
     * 								expression2,...,weight,keyword1,keyword2,...)
     * 
     */
    SIMILAR,

    /**
     * SetFindSetFind(AND(EQUAL(Make,"Honda,fit"),EQUAL(Model,"Civic,fit"))))
     * 							
     * 
     */
    SET_FIND;

    public String value() {
        return name();
    }

    public static CompositeOperator fromValue(String v) {
        return valueOf(v);
    }

}
