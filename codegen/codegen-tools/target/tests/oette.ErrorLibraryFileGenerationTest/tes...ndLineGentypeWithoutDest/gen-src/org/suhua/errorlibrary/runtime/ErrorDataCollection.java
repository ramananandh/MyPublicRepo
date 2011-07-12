
package org.suhua.errorlibrary.runtime;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorCategory;
import org.ebayopensource.turmeric.common.v1.types.ErrorSeverity;

public class ErrorDataCollection {

    private final static String ORGANIZATION = "eBay";
    public final static CommonErrorData new_error1 = createCommonErrorData(1002L, (ErrorSeverity.ERROR), (ErrorCategory.SYSTEM), "new_error1", "runtime", "System", (null));
    public final static CommonErrorData new_error2 = createCommonErrorData(1003L, (ErrorSeverity.WARNING), (ErrorCategory.APPLICATION), "new_error2", "runtime", "System", (null));

    private static CommonErrorData createCommonErrorData(long errorId, ErrorSeverity severity, ErrorCategory category, String errorName, String domain, String subDomain, String errorGroup) {
        CommonErrorData errorData = new CommonErrorData();
        errorData.setErrorId(errorId);
        errorData.setSeverity(severity);
        errorData.setCategory(category);
        errorData.setSubdomain(subDomain);
        errorData.setDomain(domain);
        errorData.setErrorGroups(errorGroup);
        errorData.setErrorName(errorName);
        errorData.setOrganization(ORGANIZATION);
        return errorData;
    }

}
