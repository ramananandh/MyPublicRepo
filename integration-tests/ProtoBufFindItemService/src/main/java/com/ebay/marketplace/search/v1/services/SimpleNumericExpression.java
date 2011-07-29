//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.07 at 12:06:52 PM GMT+05:30 
//


package com.ebay.marketplace.search.v1.services;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This type define the simple numeric expression
 * 						in
 * 						voyager, e.g ADD(NOW,10).
 * 					
 * 
 * <p>Java class for SimpleNumericExpression complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleNumericExpression">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ebay.com/marketplace/search/v1/services}NumericExpressionBase">
 *       &lt;sequence>
 *         &lt;element name="operator" type="{http://www.ebay.com/marketplace/search/v1/services}SimpleNumericOperator"/>
 *         &lt;element name="operand" type="{http://www.ebay.com/marketplace/search/v1/services}Operand" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleNumericExpression", propOrder = {
    "operator",
    "operand"
})
public class SimpleNumericExpression
    extends NumericExpressionBase
{

    @XmlElement(required = true)
    protected SimpleNumericOperator operator;
    @XmlElement(required = true)
    protected List<Operand> operand;

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleNumericOperator }
     *     
     */
    public SimpleNumericOperator getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleNumericOperator }
     *     
     */
    public void setOperator(SimpleNumericOperator value) {
        this.operator = value;
    }

    /**
     * Gets the value of the operand property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operand property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperand().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Operand }
     * 
     * 
     */
    public List<Operand> getOperand() {
        if (operand == null) {
            operand = new ArrayList<Operand>();
        }
        return this.operand;
    }

}
