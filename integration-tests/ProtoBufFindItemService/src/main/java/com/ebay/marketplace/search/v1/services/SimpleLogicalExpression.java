//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.07 at 12:06:52 PM GMT+05:30 
//


package com.ebay.marketplace.search.v1.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This type defines the function with the format
 * 						Member Operator Operand, e.g EQ(StartTime, NOW).
 * 					
 * 
 * <p>Java class for SimpleLogicalExpression complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleLogicalExpression">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ebay.com/marketplace/search/v1/services}LogicalExpressionBase">
 *       &lt;sequence>
 *         &lt;element name="operator" type="{http://www.ebay.com/marketplace/search/v1/services}AtomicOperator"/>
 *         &lt;element name="member" type="{http://www.ebay.com/marketplace/search/v1/services}Member"/>
 *         &lt;choice>
 *           &lt;element name="longOperand" type="{http://www.ebay.com/marketplace/search/v1/services}LongOperand" minOccurs="0"/>
 *           &lt;element name="doubleOperand" type="{http://www.ebay.com/marketplace/search/v1/services}DoubleOperand" minOccurs="0"/>
 *           &lt;element name="moneyOperand" type="{http://www.ebay.com/marketplace/search/v1/services}MoneyOperand" minOccurs="0"/>
 *           &lt;element name="tokenOperand" type="{http://www.ebay.com/marketplace/search/v1/services}TokenOperand" minOccurs="0"/>
 *           &lt;element name="dateOperand" type="{http://www.ebay.com/marketplace/search/v1/services}DateOperand" minOccurs="0"/>
 *           &lt;element name="expressionOperand" type="{http://www.ebay.com/marketplace/search/v1/services}SimpleNumericExpression" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleLogicalExpression", propOrder = {
    "operator",
    "member",
    "longOperand",
    "doubleOperand",
    "moneyOperand",
    "tokenOperand",
    "dateOperand",
    "expressionOperand"
})
public class SimpleLogicalExpression
    extends LogicalExpressionBase
{

    @XmlElement(required = true)
    protected AtomicOperator operator;
    @XmlElement(required = true)
    protected Member member;
    protected LongOperand longOperand;
    protected DoubleOperand doubleOperand;
    protected MoneyOperand moneyOperand;
    protected TokenOperand tokenOperand;
    protected DateOperand dateOperand;
    protected SimpleNumericExpression expressionOperand;

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link AtomicOperator }
     *     
     */
    public AtomicOperator getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtomicOperator }
     *     
     */
    public void setOperator(AtomicOperator value) {
        this.operator = value;
    }

    /**
     * Gets the value of the member property.
     * 
     * @return
     *     possible object is
     *     {@link Member }
     *     
     */
    public Member getMember() {
        return member;
    }

    /**
     * Sets the value of the member property.
     * 
     * @param value
     *     allowed object is
     *     {@link Member }
     *     
     */
    public void setMember(Member value) {
        this.member = value;
    }

    /**
     * Gets the value of the longOperand property.
     * 
     * @return
     *     possible object is
     *     {@link LongOperand }
     *     
     */
    public LongOperand getLongOperand() {
        return longOperand;
    }

    /**
     * Sets the value of the longOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link LongOperand }
     *     
     */
    public void setLongOperand(LongOperand value) {
        this.longOperand = value;
    }

    /**
     * Gets the value of the doubleOperand property.
     * 
     * @return
     *     possible object is
     *     {@link DoubleOperand }
     *     
     */
    public DoubleOperand getDoubleOperand() {
        return doubleOperand;
    }

    /**
     * Sets the value of the doubleOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link DoubleOperand }
     *     
     */
    public void setDoubleOperand(DoubleOperand value) {
        this.doubleOperand = value;
    }

    /**
     * Gets the value of the moneyOperand property.
     * 
     * @return
     *     possible object is
     *     {@link MoneyOperand }
     *     
     */
    public MoneyOperand getMoneyOperand() {
        return moneyOperand;
    }

    /**
     * Sets the value of the moneyOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link MoneyOperand }
     *     
     */
    public void setMoneyOperand(MoneyOperand value) {
        this.moneyOperand = value;
    }

    /**
     * Gets the value of the tokenOperand property.
     * 
     * @return
     *     possible object is
     *     {@link TokenOperand }
     *     
     */
    public TokenOperand getTokenOperand() {
        return tokenOperand;
    }

    /**
     * Sets the value of the tokenOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link TokenOperand }
     *     
     */
    public void setTokenOperand(TokenOperand value) {
        this.tokenOperand = value;
    }

    /**
     * Gets the value of the dateOperand property.
     * 
     * @return
     *     possible object is
     *     {@link DateOperand }
     *     
     */
    public DateOperand getDateOperand() {
        return dateOperand;
    }

    /**
     * Sets the value of the dateOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateOperand }
     *     
     */
    public void setDateOperand(DateOperand value) {
        this.dateOperand = value;
    }

    /**
     * Gets the value of the expressionOperand property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleNumericExpression }
     *     
     */
    public SimpleNumericExpression getExpressionOperand() {
        return expressionOperand;
    }

    /**
     * Sets the value of the expressionOperand property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleNumericExpression }
     *     
     */
    public void setExpressionOperand(SimpleNumericExpression value) {
        this.expressionOperand = value;
    }

}
