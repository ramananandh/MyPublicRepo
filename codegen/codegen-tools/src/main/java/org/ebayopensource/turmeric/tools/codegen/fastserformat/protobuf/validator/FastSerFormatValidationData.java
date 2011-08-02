/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.validator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatType;
import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatValidationError;
import org.ebayopensource.turmeric.runtime.codegen.common.FastSerFormatValidationTemplate;
import org.ebayopensource.turmeric.runtime.codegen.common.ValidationRule;
import org.ebayopensource.turmeric.runtime.codegen.common.ValidationRule2FormatMap;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.tools.codegen.util.CodeGenUtil;

/**
 * This class is represents the template data file ValidationData.xml.
 * It gives apis to 
 * 1. find out the rules applicable for a given fast ser format.
 * 2. find out fast ser formats for a given rule.
 * 3. template data for each rule (used maily to get description)
 * 
 * @author rkulandaivel
 * 
 */
public class FastSerFormatValidationData {
	private static Logger s_logger = LogManager
			.getInstance(FastSerFormatValidationData.class);

	private static Logger getLogger() {
		return s_logger;
	}

	private static final String VALIDATION_TEMPLATE_FILE_LOCATION = "META-INF/soa/data/fastserformat/ValidationData.xml";

	private static FastSerFormatValidationData s_instance = new FastSerFormatValidationData();

	private Map<FastSerFormatType, List<ValidationRule>> m_validationRulesMap = new HashMap<FastSerFormatType, List<ValidationRule>>();
	private Map<ValidationRule, FastSerFormatValidationError> m_validationErrorMap = new HashMap<ValidationRule, FastSerFormatValidationError>();
	private Map<ValidationRule, List<FastSerFormatType>> m_format2RulesMap = new HashMap<ValidationRule, List<FastSerFormatType>>();

	private FastSerFormatValidationData() {
		init();
	}

	public static FastSerFormatValidationData getInstance(){
		return s_instance;
	}
	private void init() {
		InputStream inputStream = CodeGenUtil
				.getInputStreamForAFileFromClasspath(
						VALIDATION_TEMPLATE_FILE_LOCATION, FastSerFormatValidationData.class.getClassLoader());
		
		
		if (inputStream == null) {
			getLogger().warning("Could not load Validation Template file");
		} else {
			getLogger().info("Succesfully loaded the file");
			FastSerFormatValidationTemplate validationData = JAXB.unmarshal(
					inputStream, FastSerFormatValidationTemplate.class);

			for( ValidationRule2FormatMap map : validationData.getRule2Formats() ){
				for( FastSerFormatType format : map.getFormat() ){
					List<ValidationRule> rulesForFormat = m_validationRulesMap.get(format);
					if( rulesForFormat == null){
						rulesForFormat = new ArrayList<ValidationRule>();
						m_validationRulesMap.put(format, rulesForFormat);
					}
					rulesForFormat.add( map.getRule() );
				}
				m_format2RulesMap.put( map.getRule(), map.getFormat() );
			}

			for(FastSerFormatValidationError error : validationData.getErrorData().getErrorList() ){
				m_validationErrorMap.put( error.getError() , error );
			}
			getLogger().info("Succesfully populated the template data");
		}
	}
	
	/**
	 * Returns validation rules for a given format.
	 * 
	 * @param format
	 * @return
	 */
	public List<ValidationRule> getRulesForFormat(FastSerFormatType format){
		return m_validationRulesMap.get(format);
	}

	/**
	 * Returns error data for a given rule.
	 * @param rule
	 * @return
	 */
	public FastSerFormatValidationError getTemplateData( ValidationRule rule ){
		if( rule == null ){
			throw new NullPointerException("Rule id is null");
		}
		FastSerFormatValidationError error = m_validationErrorMap.get(rule);
		if( error == null ){
			throw new IllegalArgumentException("Error is null. This could be due to invalid configuration in ValidationData.xml");
		}
		return error;
	}

	/**
	 * Returns the applicable formats for a given rule.
	 * @param rule
	 * @return
	 */
	public List<FastSerFormatType> getApplicableFormats( ValidationRule rule ){
		return m_format2RulesMap.get(rule);
	}
}
