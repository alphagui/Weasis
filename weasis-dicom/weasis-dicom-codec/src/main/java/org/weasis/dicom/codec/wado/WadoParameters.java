/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.dicom.codec.wado;

import org.weasis.core.api.media.data.TagElement;
import org.weasis.dicom.codec.Messages;

public class WadoParameters {

    public static final String TAG_DOCUMENT_ROOT = "wado_query"; //$NON-NLS-1$
    public static final String TAG_WADO_URL = "wadoURL"; //$NON-NLS-1$
    public static final String TAG_WADO_ONLY_SOP_UID = "requireOnlySOPInstanceUID"; //$NON-NLS-1$
    public static final String TAG_WADO_ADDITIONNAL_PARAMETERS = "additionnalParameters"; //$NON-NLS-1$
    public static final String TAG_WADO_OVERRIDE_TAGS = "overrideDicomTagsList"; //$NON-NLS-1$
    public static final String TAG_WADO_WEB_LOGIN = "webLogin"; //$NON-NLS-1$

    private final String wadoURL;
    private final boolean requireOnlySOPInstanceUID;
    private final String additionnalParameters;
    private final int[] overrideDicomTagIDList;
    private final String webLogin;

    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID, String additionnalParameters,
        String overrideDicomTagsList, String webLogin) {
        if (wadoURL == null) {
            throw new IllegalArgumentException("wadoURL cannot be null"); //$NON-NLS-1$
        }
        this.wadoURL = wadoURL;
        this.webLogin = webLogin == null ? null : webLogin.trim();
        this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
        this.additionnalParameters = additionnalParameters == null ? "" : additionnalParameters; //$NON-NLS-1$
        if (overrideDicomTagsList != null && !"".equals(overrideDicomTagsList.trim())) { //$NON-NLS-1$
            String[] val = overrideDicomTagsList.split(","); //$NON-NLS-1$
            overrideDicomTagIDList = new int[val.length];
            for (int i = 0; i < val.length; i++) {
                try {
                    overrideDicomTagIDList[i] = Integer.decode(val[i].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            overrideDicomTagIDList = null;
        }
    }

    public String getWebLogin() {
        return webLogin;
    }

    public String getWadoURL() {
        return wadoURL;
    }

    public boolean isRequireOnlySOPInstanceUID() {
        return requireOnlySOPInstanceUID;
    }

    public String getAdditionnalParameters() {
        return additionnalParameters;
    }

    public int[] getOverrideDicomTagIDList() {
        return overrideDicomTagIDList;
    }

    public boolean isOverrideTag(TagElement tagElement) {
        if (overrideDicomTagIDList != null) {
            int tagID = tagElement.getId();
            for (int overTag : overrideDicomTagIDList) {
                if (tagID == overTag) {
                    return true;
                }
            }
        }
        return false;
    }
}
